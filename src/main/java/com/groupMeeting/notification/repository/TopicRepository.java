package com.groupMeeting.notification.repository;

import com.groupMeeting.entity.notification.Topic;
import com.groupMeeting.global.enums.PushTopic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    @Query("select t from Topic t where t.userId = :userId")
    List<Topic> findAllUserIdTopic(Long userId);

    @Query("select t from Topic t where t.userId = :userId and t.topic in :topic")
    List<Topic> findAllUserTopic(Long userId, List<PushTopic> topic);
}
