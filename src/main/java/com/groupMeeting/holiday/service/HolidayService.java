package com.groupMeeting.holiday.service;

import com.groupMeeting.dto.client.HolidayClientResponse;
import com.groupMeeting.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayRepository holidayRepository;

    public List<HolidayClientResponse> getYearHolidays(String year) {

        return holidayRepository
                .getHolidayByYear(year)
                .stream()
                .map(HolidayClientResponse::from)
                .toList();
    }
}
