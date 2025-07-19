package com.mople.holiday.schedule;

import com.mople.dto.response.holiday.HolidayResponse;
import com.mople.entity.holiday.Holiday;
import com.mople.global.client.HolidayClient;
import com.mople.holiday.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
public class HolidayScheduler {
    private final HolidayRepository holidayRepository;
    private final HolidayClient holidayClient;
    private final String requestKey;

    public HolidayScheduler(
            HolidayRepository holidayRepository,
            HolidayClient holidayClient,
            @Value("${kakao.admin}") String requestKey
    ) {
        this.holidayRepository = holidayRepository;
        this.holidayClient = holidayClient;
        this.requestKey = requestKey;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void holidaySchedule() {
        LocalDate today = LocalDate.now();

        HolidayResponse holiday = holidayClient.getHolidayKakao(
                requestKey,
                today.with(TemporalAdjusters.firstDayOfMonth())
                        .atStartOfDay()
                        .atZone(ZoneId.of("UTC"))
                        .toInstant(),
                today.with(TemporalAdjusters.lastDayOfMonth())
                        .atStartOfDay()
                        .atZone(ZoneId.of("UTC"))
                        .toInstant()
        );

        String date = today.toString();
        holidayRepository.deleteAll(holidayRepository.getHolidayByYearAndMonth(date.substring(0, 4), date.substring(5, 7)));

        List<Holiday> holidays = holiday.events()
                .stream()
                .filter(HolidayResponse.HolidayInfo::holiday)
                .map(h -> {

                    Instant instant = Instant.parse(h.time().startAt());

                    return Holiday.builder()
                            .title(h.title())
                            .date(LocalDate.ofInstant(instant, ZoneId.of("UTC")))
                            .year(h.time().startAt().substring(0, 4))
                            .month(h.time().startAt().substring(5, 7))
                            .build();
                })
                .toList();

        holidayRepository.saveAll(holidays);
    }
}
