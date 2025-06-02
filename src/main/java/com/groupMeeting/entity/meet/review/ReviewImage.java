package com.groupMeeting.entity.meet.review;

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

    @Column(name = "image", length = 100)
    private String reviewImage;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private PlanReview review;

    @Builder
    public ReviewImage(String reviewImage, PlanReview review) {
        this.reviewImage = reviewImage;
        this.review = review;
    }

    public void updateReview(PlanReview planReview) {
        review = planReview;
    }
}