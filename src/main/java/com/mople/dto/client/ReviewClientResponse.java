package com.mople.dto.client;

import com.mople.dto.response.meet.review.PlanReviewDetailResponse;
import com.mople.dto.response.meet.review.PlanReviewInfoResponse;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewClientResponse {
    private final Long version;
    private final Long meetId;
    private final Long reviewId;
    private final Long postId;
    private final Long creatorId;
    private final String reviewName;
    private final String address;
    private final String title;
    private final LocalDateTime reviewTime;
    private final String meetName;
    private final String meetImg;
    private final BigDecimal lat;
    private final BigDecimal lot;
    private final int participantsCount;
    private final boolean isRegister;
    private final List<PlanReviewDetailResponse.ReviewImageResponse> images;
    private final Integer commentCount;

    public static List<ReviewClientResponse> ofInfos(List<PlanReviewInfoResponse> infoResponses){
        return infoResponses.stream().map(ReviewClientResponse::ofInfo).toList();
    }

    private static ReviewClientResponse ofInfo(PlanReviewInfoResponse infoResponse){
        return ReviewClientResponse.builder()
                .reviewId(infoResponse.reviewId())
                .version(infoResponse.version())
                .reviewName(infoResponse.reviewName())
                .reviewTime(infoResponse.reviewDateTime())
                .creatorId(infoResponse.creatorId())
                .participantsCount(infoResponse.participantsCount())
                .images(infoResponse.images())
                .build();
    }

    public static ReviewClientResponse ofDetail(PlanReviewDetailResponse detailResponse, Integer commentCount){
        return ReviewClientResponse.builder()
                .version(detailResponse.version())
                .meetId(detailResponse.meetId())
                .meetName(detailResponse.meetName())
                .meetImg(detailResponse.meetImage())
                .creatorId(detailResponse.creatorId())
                .postId(detailResponse.postId())
                .reviewId(detailResponse.reviewId())
                .reviewName(detailResponse.reviewName())
                .address(detailResponse.address())
                .title(detailResponse.title())
                .lat(detailResponse.lat())
                .lot(detailResponse.lot())
                .reviewTime(detailResponse.reviewDateTime())
                .participantsCount(detailResponse.participantsCount())
                .isRegister(detailResponse.register())
                .images(detailResponse.images())
                .commentCount(commentCount)
                .build();
    }
}
