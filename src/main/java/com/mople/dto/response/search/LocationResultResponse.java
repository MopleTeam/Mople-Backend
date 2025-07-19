package com.mople.dto.response.search;

public record LocationResultResponse (
        String title,
        String roadAddress,
        int mapx,
        int mapy
){
    public LocationResultResponse(LocationSearchResponses.LocationSearchResponse find){
        this(
                find.title().replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""),
                find.roadAddress(),
                find.mapx(),
                find.mapy()
        );
    }
}
