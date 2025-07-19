package com.mople.holiday.repository;

import com.mople.entity.holiday.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> getHolidayByYear(String year);
    List<Holiday> getHolidayByYearAndMonth(String year, String month);
}
