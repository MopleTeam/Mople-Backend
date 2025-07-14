package com.groupMeeting.dto.event.data.review.impl;

import com.groupMeeting.dto.event.data.review.ReviewEventData;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class ReviewRemindEventData implements ReviewEventData {

    private final Long meetId;
    private final String meetName;
    private final Long reviewId;
    private final String reviewName;
    private final Long creatorId;

    @Override
    public String getTitle() {
        return meetName + "의 일정은 어떠셨나요?";
    }

    @Override
    public String getBody() {
        return reviewName + "의 사진을 공유해보세요";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("reviewId", reviewId.toString());
    }

    @Override
    public Long triggeredBy() {
        return creatorId;
    }
}
