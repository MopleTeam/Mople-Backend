package com.mople.global.utils.cursor;

import com.mople.core.exception.custom.CursorException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.mople.global.enums.ExceptionReturnCode.INVALID_CURSOR;

public class CursorUtils {

    public static String encode(String rawCursor) {
        return Base64.getEncoder()
                .encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encodedCursor) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCursor);

            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
