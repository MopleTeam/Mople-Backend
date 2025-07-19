package com.mople.dto.response.search;

import java.util.List;

public record LocationSearchResponses(
        List<LocationSearchResponse> items
) {
    public record LocationSearchResponse (
            String title,
            String link,
            String category,
            String description,
            String address,
            String roadAddress,
            int mapx,
            int mapy
    ){}
}
