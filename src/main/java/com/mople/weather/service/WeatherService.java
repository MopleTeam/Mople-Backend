package com.mople.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherListResponse;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoResponse;

import okhttp3.*;

import org.jetbrains.annotations.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

@Service
public class WeatherService {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String ID;
    private final String URL;
    private final String MULTI_URL;

    public WeatherService(
            OkHttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${weather.key}") String id,
            @Value("${weather.url}") String url,
            @Value("${weather.multi-url}") String multiUrl
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.ID = id;
        this.URL = url;
        this.MULTI_URL = multiUrl;
    }

    @Async
    public CompletableFuture<WeatherInfoResponse> getClosestWeatherInfoFromDateTime(
            CoordinateRequest coordinate,
            LocalDateTime planTime
    ) {
        return getWeatherListByLocation(coordinate)
                .thenApply(response -> {
                    var weatherList = response.list();

                    double temperature = weatherList.get(0).main().temp();
                    double pop = weatherList.get(0).pop();
                    String weatherIcon = weatherList.get(0).weather().get(0).icon();

                    var time = planTime.atZone(ZoneId.systemDefault()).toEpochSecond();

                    for (var i = 0; i < weatherList.size() - 1; i++) {
                        var before = weatherList.get(i);
                        var after = weatherList.get(i + 1);

                        if (time <= after.dt() && time >= before.dt()) {
                            OpenWeatherListResponse.WeatherDetail target;
                            if ((after.dt() - time) > (time - before.dt())) {
                                target = before;
                            } else {
                                target = after;
                            }

                            temperature = target.main().temp();
                            pop = target.pop();
                            weatherIcon = target.weather().get(0).icon();

                            break;
                        }
                    }

                    return new WeatherInfoResponse(
                            temperature,
                            pop,
                            weatherIcon
                    );
                });
    }

    public CompletableFuture<OpenWeatherListResponse> getWeatherListByLocation(CoordinateRequest request) {
        CompletableFuture<OpenWeatherListResponse> future = new CompletableFuture<>();

        HttpUrl.Builder urlBuilder = getClient(MULTI_URL);

        urlBuilder.addQueryParameter("lat", request.latitude().toString());
        urlBuilder.addQueryParameter("lon", request.longitude().toString());
        urlBuilder.addQueryParameter("appid", ID);
        urlBuilder.addQueryParameter("units", "metric");
        urlBuilder.addQueryParameter("cnt", "40");

        httpClient
                .newCall(
                        new Request.Builder()
                                .url(urlBuilder.build().toString())
                                .build()
                )
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                future.completeExceptionally(e);
                                call.cancel();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    future.completeExceptionally(new IOException("Unexpected code " + response));
                                }

                                var resBody = response.body();

                                if (isNull(resBody)) {
                                    throw new IOException("No response body found");
                                }
                                future.complete(objectMapper.readValue(resBody.string(), OpenWeatherListResponse.class));

                                response.close();
                            }
                        });
        return future;
    }

    public CompletableFuture<OpenWeatherResponse> getWeatherInfoByLocation(CoordinateRequest request) {
        CompletableFuture<OpenWeatherResponse> future = new CompletableFuture<>();

        HttpUrl.Builder urlBuilder = getClient(URL);

        urlBuilder.addQueryParameter("lat", request.latitude().toString());
        urlBuilder.addQueryParameter("lon", request.longitude().toString());
        urlBuilder.addQueryParameter("appid", ID);
        urlBuilder.addQueryParameter("units", "metric");

        httpClient
                .newCall(
                        new Request.Builder()
                                .url(urlBuilder.build().toString())
                                .build()
                )
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                future.completeExceptionally(e);
                                call.cancel();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    future.completeExceptionally(new IOException("Unexpected code " + response));
                                }

                                var resBody = response.body();

                                if (isNull(resBody)) {
                                    throw new IOException("No response body found");
                                }
                                future.complete(objectMapper.readValue(resBody.string(), OpenWeatherResponse.class));

                                response.close();
                            }
                        });
        return future;
    }

    private HttpUrl.Builder getClient(String url) {
        return Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
    }
}
