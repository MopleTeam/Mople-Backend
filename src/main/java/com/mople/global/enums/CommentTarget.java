package com.mople.global.enums;

import com.mople.global.enums.event.AggregateType;

public enum CommentTarget {
    POST(AggregateType.POST),
    NOTICE(AggregateType.NOTICE);

    private final AggregateType aggregateType;

    CommentTarget(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public AggregateType toAggregateType() {
        return aggregateType;
    }
}
