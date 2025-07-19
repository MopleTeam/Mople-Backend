package com.mople.dto.response.meet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

public record UserAllDateResponse(
        List<LocalDate> dates
) {
    public UserAllDateResponse(List<LocalDateTime> planDate, List<LocalDateTime> reviewDate) {
        this(Stream.concat(
                                planDate.stream().map(LocalDateTime::toLocalDate),
                                reviewDate.stream().map(LocalDateTime::toLocalDate)
                        )
                        .distinct()
                        .sorted()
                        .toList()
        );
    }
}
