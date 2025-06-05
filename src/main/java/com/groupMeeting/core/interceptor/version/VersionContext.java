package com.groupMeeting.core.interceptor.version;

import com.groupMeeting.dto.version.VersionInfo;

public class VersionContext {
    private static final ThreadLocal<VersionInfo> versionHolder = new ThreadLocal<>();

    public static void set(VersionInfo versionInfo) {
        versionHolder.set(versionInfo);
    }

    public static VersionInfo get() {
        return versionHolder.get();
    }

    public static void clear() {
        versionHolder.remove();
    }
}
