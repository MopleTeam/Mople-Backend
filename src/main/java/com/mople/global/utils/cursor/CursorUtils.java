package com.mople.global.utils.cursor;

import com.mople.core.exception.custom.CursorException;
import com.mople.dto.response.pagination.CursorPage;
import com.mople.dto.response.pagination.CursorPageResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.mople.global.enums.ExceptionReturnCode.INVALID_CURSOR;

public class CursorUtils {

    private static final String SEPARATOR = "|";

    public static <T, R> CursorPageResponse<R> buildCursorPage(
            List<T> items,
            int size,
            Function<T, String[]> cursorExtractor,
            Function<List<T>, List<R>> converter
    ) {
        boolean hasNext = items.size() > size;
        List<T> pageItems = hasNext ? items.subList(0, size) : items;

        String nextCursor = hasNext && !pageItems.isEmpty()
                ? CursorUtils.encode(cursorExtractor.apply(pageItems.get(pageItems.size() - 1)))
                : null;

        CursorPage page = CursorPage.builder()
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(pageItems.size())
                .build();

        return CursorPageResponse.of(converter.apply(pageItems), page);
    }

    private static String encode(String... parts) {
        return Base64.getEncoder()
                .encodeToString(createRawCursor(parts).getBytes(StandardCharsets.UTF_8));
    }

    private static String createRawCursor(String... parts) {
        return String.join(SEPARATOR, parts);
    }

    public static String[] decode(String encodedCursor) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCursor);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

            return decoded.split(Pattern.quote(SEPARATOR));
        } catch (Exception e) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
