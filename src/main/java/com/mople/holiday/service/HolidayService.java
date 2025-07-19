package com.mople.holiday.service;

import com.mople.dto.client.HolidayClientResponse;
import com.mople.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayRepository holidayRepository;

    public List<HolidayClientResponse> getHolidayMonth(String year) {

        return holidayRepository
                .getHolidayByYear(year)
                .stream()
                .map(HolidayClientResponse::from)
                .toList();
    }
}
