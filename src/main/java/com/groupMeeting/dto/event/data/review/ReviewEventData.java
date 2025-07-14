package com.groupMeeting.dto.event.data.review;

import com.groupMeeting.dto.event.data.EventData;

public interface ReviewEventData extends EventData {

    Long getMeetId();

    Long getReviewId();
}
