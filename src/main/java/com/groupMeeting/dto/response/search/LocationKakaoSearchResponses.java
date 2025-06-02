package com.groupMeeting.dto.response.search;

import java.util.List;

public record LocationKakaoSearchResponses(
        List<LocationKakaoSearchResponse> documents,
        Pageable meta
) {
    public record LocationKakaoSearchResponse(
            String place_name,
            String distance,
            String place_url,
            String category_name,
            String address_name,
            String road_address_name,
            String x,
            String y
    ) {
    }

    public record Pageable(
            Boolean is_end,
            int pageable_count
    ) {
    }
}
