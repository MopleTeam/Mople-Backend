package com.mople.meet.mapper;

import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.entity.meet.review.ReviewImage;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    @Named("getReviewImage")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image.reviewImage", target = "reviewImg")
    ReviewImageListResponse getReviewImage(ReviewImage image);

    @IterableMapping(qualifiedByName = "getReviewImage")
    List<ReviewImageListResponse> getReviewImages(List<ReviewImage> images);
}
