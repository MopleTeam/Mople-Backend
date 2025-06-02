//package com.groupMeeting.notification.repository;
//
//import com.groupMeeting.entity.notification.Notification;
//import com.groupMeeting.entity.notification.QNotification;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//@RequiredArgsConstructor
//public class NotificationRepositorySupport {
//    private final JPAQueryFactory queryFactory;
//
//    public List<Notification> findAllNeedToBeSendFromNow(){
//        var notification = QNotification.notification;
//        return queryFactory
//                .selectFrom(notification)
//                .where(notification.scheduledAt.before(LocalDateTime.now())
//                        .and(notification.readAt.isNull())
//                        .and(notification.sendAt.isNull())
//                )
//                .fetch();
//    }
//}
