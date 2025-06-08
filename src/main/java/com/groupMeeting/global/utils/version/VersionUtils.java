package com.groupMeeting.global.utils.version;

import com.groupMeeting.core.exception.custom.VersionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.groupMeeting.global.enums.ExceptionReturnCode.UNSUPPORTED_VERSION;

@Component
@RequiredArgsConstructor
public class VersionUtils {
    private static final String VERSION_DELIMITER = "\\.";

    public static int convertToVersionCode(String version) {
        String[] parts = version.split("\\.");

        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        return major * 10000 + minor * 100 + patch;
    }

    public static String convertToVersion(int versionCode) {
        int major = versionCode / 10000;
        int minor = (versionCode % 10000) / 100;
        int patch = versionCode % 100;

        return major + "." + minor + "." + patch;
    }

    public static void validateVersionFormat(String version) {
        String[] versionParts = version.split(VERSION_DELIMITER);
        for (String part : versionParts) {
            try {
                Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new VersionException(UNSUPPORTED_VERSION);
            }
        }
    }
}
