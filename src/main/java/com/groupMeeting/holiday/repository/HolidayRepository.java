package com.groupMeeting.holiday.repository;

import com.groupMeeting.entity.holiday.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> getHolidayByYear(String year);
    List<Holiday> getHolidayByYearAndMonth(String year, String month);
}
