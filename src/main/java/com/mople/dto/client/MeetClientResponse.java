package com.mople.dto.client;

import com.mople.dto.response.meet.MeetInfoResponse;
import com.mople.dto.response.meet.MeetListResponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import java.util.List;

@Builder
@Getter
public class MeetClientResponse {
    private final Long meetId;
    private final Long version;
    private final String meetName;
    private final String meetImage;
    private final Long sinceDays;
    private final Long creatorId;
    private final int memberCount;
    private final LocalDateTime lastPlanDay;

    public static List<MeetClientResponse> ofListMeets(List<MeetListResponse> listResponses) {
        return listResponses.stream().map(MeetClientResponse::ofListMeet).toList();
    }

    private static MeetClientResponse ofListMeet(MeetListResponse listResponse) {
        return MeetClientResponse.builder()
                .meetId(listResponse.meetId())
                .version(listResponse.version())
                .meetName(listResponse.meetName())
                .meetImage(listResponse.meetImage())
                .memberCount(listResponse.memberCount())
                .lastPlanDay(listResponse.lastPlanDays())
                .build();
    }

    public static MeetClientResponse ofMeet(MeetInfoResponse infoResponse) {
        return MeetClientResponse.builder()
                .meetId(infoResponse.meetId())
                .version(infoResponse.version())
                .meetName(infoResponse.meetName())
                .meetImage(infoResponse.meetImage())
                .creatorId(infoResponse.creatorId())
                .sinceDays(infoResponse.meetStartDate())
                .memberCount(infoResponse.memberCount())
                .build();
    }
}
