package com.mople.meet.repository;

import com.mople.entity.meet.Meet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeetRepository extends JpaRepository<Meet, Long> {
    @Query("select m from Meet m join fetch m.members join fetch m.creator where m.id = :meetId")
    Optional<Meet> findMeetAll(Long meetId);

    @Query(value = "select m from Meet m where m.creator.id = :userId")
    List<Meet> findMeetByUserId(Long userId);

    @Query(value = "select m from Meet m left join fetch m.reviews where m.id = :meetId")
    Optional<Meet> findMeetAndReviews(Long meetId);

    @Query(value = "select m from Meet m where m.id = :meetId")
    Optional<Meet> findMeetById(Long meetId);
}
