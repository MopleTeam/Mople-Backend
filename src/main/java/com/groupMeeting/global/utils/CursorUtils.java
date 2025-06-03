package com.groupMeeting.global.utils;

import com.groupMeeting.core.exception.custom.CursorException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.groupMeeting.global.enums.ExceptionReturnCode.INVALID_CURSOR;

@Component
public class CursorUtils {

    public static String encode(Long rawCursor) {
        return Base64.getEncoder()
                .encodeToString(String.valueOf(rawCursor).getBytes(StandardCharsets.UTF_8));
    }

    public static Long decode(String encodedCursor) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCursor);
            String rawCursor = new String(decodedBytes, StandardCharsets.UTF_8);

            return Long.parseLong(rawCursor);
        } catch (Exception e) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
