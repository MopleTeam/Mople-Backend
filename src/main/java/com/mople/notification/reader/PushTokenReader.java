package com.mople.notification.reader;

import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.QFirebaseToken;
import com.mople.entity.notification.QTopic;
import com.mople.global.enums.PushTopic;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PushTokenReader {
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
}
