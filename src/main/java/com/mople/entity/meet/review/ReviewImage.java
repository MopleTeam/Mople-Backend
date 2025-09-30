package com.mople.entity.meet.review;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "review_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "image")
    private String reviewImage;

    @Builder
    public ReviewImage(Long reviewId, String reviewImage) {
        this.reviewId = reviewId;
        this.reviewImage = reviewImage;
    }
}