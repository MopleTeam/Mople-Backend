package com.mople.entity.notification;

import com.mople.global.enums.PushTopic;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "topic")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "topic_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PushTopic topic;

    public Topic(Long userId, PushTopic topic) {
        this.userId = userId;
        this.topic = topic;
    }

    public static List<Topic> ofNew(Long userId) {
        return List.of(
                new Topic(userId, PushTopic.MEET),
                new Topic(userId, PushTopic.PLAN),
                new Topic(userId, PushTopic.REPLY),
                new Topic(userId, PushTopic.MENTION)
        );
    }
}