package com.groupMeeting.global.utils.version;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VersionUtils {

    public static int convertToVersionCode(String version) {
        String[] parts = version.split("\\.");

        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        return major * 10000 + minor * 100 + patch;
    }
}
