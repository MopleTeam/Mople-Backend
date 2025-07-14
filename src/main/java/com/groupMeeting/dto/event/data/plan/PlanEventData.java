package com.groupMeeting.dto.event.data.plan;

import com.groupMeeting.dto.event.data.EventData;

public interface PlanEventData extends EventData {

    Long getMeetId();

    Long getPlanId();
}
