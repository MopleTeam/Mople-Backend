package com.mople.entity.holiday;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "holiday", indexes = {
        @Index(name = "idx_holiday_year_month", columnList = "year, month"),
        @Index(name = "idx_holiday_year", columnList = "year")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;
    @Column(length = 10)
    private String year;
    private String month;
    private LocalDate date;

    @Builder
    public Holiday(String title, String year, String month, LocalDate date) {
        this.title = title;
        this.year = year;
        this.month = month;
        this.date = date;
    }
}
