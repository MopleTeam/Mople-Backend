package com.mople.dto.client;

import com.mople.entity.holiday.Holiday;

import java.time.LocalDate;

public record HolidayClientResponse(
        String title,
        LocalDate date
) {

    public static HolidayClientResponse from(Holiday holiday) {
        return new HolidayClientResponse(holiday.getTitle(), holiday.getDate());
    }
}
