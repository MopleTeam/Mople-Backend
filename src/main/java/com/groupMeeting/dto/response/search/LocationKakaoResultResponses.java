package com.groupMeeting.dto.response.search;

import java.util.List;

public record LocationKakaoResultResponses(
        List<LocationKakaoResultResponse> searchResult,
        int page,
        Boolean isEnd
) {
    public LocationKakaoResultResponses(LocationKakaoSearchResponses searchResponses) {
        this(
                searchResponses.documents().stream().map(LocationKakaoResultResponse::new).toList(),
                searchResponses.meta().pageable_count(),
                searchResponses.meta().is_end()
        );
    }

    public record LocationKakaoResultResponse(
            String title,
            String distance,
            String address,
            String roadAddress,
            String x,
            String y
    ) {

        public LocationKakaoResultResponse(LocationKakaoSearchResponses.LocationKakaoSearchResponse find) {
            this(
                    find.place_name(),
                    find.distance(),
                    find.road_address_name().isBlank() ? "" : find.road_address_name().split(" ")[0] + " " + find.road_address_name().split(" ")[1],
                    find.road_address_name(),
                    find.x(),
                    find.y()
            );
        }
    }
}
