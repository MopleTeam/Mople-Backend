package com.groupMeeting.dto.event.data.comment;

import com.groupMeeting.dto.event.data.EventData;

public interface CommentEventData extends EventData {

    Long getPostId();

    Long getCommentId();
}
