package com.mople.notification.reader;

import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.QFirebaseToken;
import com.mople.entity.notification.QNotification;
import com.mople.entity.notification.QTopic;
import com.mople.global.enums.PushTopic;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationTokenReader {

    private final JPAQueryFactory queryFactory;

    public List<FirebaseToken> findTokensWithPushTopic(List<Long> userIds, PushTopic pushTopic) {
        QTopic topic = QTopic.topic1;

        List<Long> subscribedUserIds = queryFactory
                .select(topic.userId)
                .from(topic)
                .where(topic.userId.in(userIds), topic.topic.eq(pushTopic))
                .fetch();

        return findPushTokens(subscribedUserIds);
    }

    private List<FirebaseToken> findPushTokens(List<Long> userIds) {
        QFirebaseToken token = QFirebaseToken.firebaseToken;

        return queryFactory
                .select(token)
                .from(token)
                .where(token.userId.in(userIds), token.active.isTrue())
                .fetch();
    }

    public Map<Long, Long> getBadgeMap(List<Long> userIds) {
        QNotification notification = QNotification.notification;

        LocalDateTime now = LocalDateTime.now();

        List<Tuple> rows = queryFactory
                .select(notification.userId, notification.count())
                .from(notification)
                .where(
                        notification.userId.in(userIds),
                        notification.expiredAt.after(now),
                        notification.readAt.isNull()
                )
                .groupBy(notification.userId)
                .fetch();

        return rows.stream()
                .collect(Collectors.toMap(
                        r -> r.get(notification.userId),
                        r -> Optional.ofNullable(r.get(notification.count())).orElse(0L)
                ));
    }
}
