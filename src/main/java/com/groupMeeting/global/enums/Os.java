package com.groupMeeting.global.enums;

import java.util.Arrays;

public enum Os {
    ANDROID("android"),
    IOS("iOS"),
    UNKNOWN("unknown");

    private final String value;

    Os(String value) {
        this.value = value;
    }

    public static Os from(String osHeader) {
        if (osHeader == null) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(os -> os.value.equalsIgnoreCase(osHeader))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getValue() {
        return value;
    }
}
