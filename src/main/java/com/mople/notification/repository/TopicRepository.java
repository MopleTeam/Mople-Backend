package com.mople.notification.repository;

import com.mople.entity.notification.Topic;
import com.mople.global.enums.PushTopic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    @Query("select t from Topic t where t.userId = :userId")
    List<Topic> findAllUserIdTopic(Long userId);

    @Query("select t from Topic t where t.userId = :userId and t.topic in :topic")
    List<Topic> findAllUserTopic(Long userId, List<PushTopic> topic);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from Topic t " +
            "      where t.userId = :userId "
    )
    void deleteByUserId(Long userId);
}
