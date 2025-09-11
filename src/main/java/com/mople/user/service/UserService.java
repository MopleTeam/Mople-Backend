package com.mople.user.service;

import com.mople.core.exception.custom.AsyncException;
import com.mople.dto.client.UserClientResponse;
import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.dto.event.data.domain.user.UserImageChangedEvent;
import com.mople.dto.request.user.RandomNicknameRequest;
import com.mople.dto.request.user.UserDeleteRequest;
import com.mople.dto.request.user.UserInfoRequest;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.reader.EntityReader;
import com.mople.notification.repository.FirebaseTokenRepository;
import com.mople.notification.repository.NotificationRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.global.enums.event.AggregateType.USER;
import static com.mople.global.enums.event.EventTypeNames.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DELETED_USER_NICKNAME = "탈퇴한 사용자";

    private final RandomNicknameRequest randomNickname = new RandomNicknameRequest();
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EntityReader reader;
    private final OutboxService outboxService;
    private final FirebaseTokenRepository firebaseTokenRepository;

    @Transactional(readOnly = true)
    public UserClientResponse getInfo(Long id) {
        User user = reader.findUser(id);

        boolean isExistBadgeCount = notificationRepository.countBadgeCount(user.getId(), Action.PUBLISHED) > 0;

        return UserClientResponse.builder()
                .userId(user.getId())
                .version(user.getVersion())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .isExistBadgeCount(isExistBadgeCount)
                .build();
    }

    @Transactional
    public UserClientResponse updateInfo(Long id, UserInfoRequest updateInfo) {
        User user = reader.findUser(id);

        if (!user.getVersion().equals(updateInfo.version())) {
            throw new AsyncException(ExceptionReturnCode.REQUEST_CONFLICT);
        }

        if (user.imageValid() && !user.getProfileImg().equals(updateInfo.image())) {
            UserImageChangedEvent changedEvent = UserImageChangedEvent.builder()
                    .userId(id)
                    .imageUrl(user.getProfileImg())
                    .imageDeletedBy(id)
                    .build();

            outboxService.save(USER_IMAGE_CHANGED, USER, id, changedEvent);
        }

        user.updateImageAndNickname(updateInfo.image(), updateInfo.nickname());

        boolean isExistBadgeCount = notificationRepository.countBadgeCount(user.getId(), Action.PUBLISHED) > 0;

        return UserClientResponse.builder()
                .userId(user.getId())
                .version(user.getVersion())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .isExistBadgeCount(isExistBadgeCount)
                .build();
    }

    @Transactional
    public void removeUser(final Long id, UserDeleteRequest request) {
        User user = reader.findUser(id);

        if (!user.getVersion().equals(request.version())) {
            throw new AsyncException(ExceptionReturnCode.REQUEST_CONFLICT);
        }

        String userProfileImg = user.getProfileImg();

        userRepository.removeUser(DELETED_USER_NICKNAME, id);
        firebaseTokenRepository.deleteByUserId(id);

        UserDeletedEvent deletedEvent = UserDeletedEvent.builder()
                .userId(id)
                .userProfileImg(userProfileImg)
                .build();

        outboxService.save(USER_DELETED, USER, id, deletedEvent);
    }

    @Transactional(readOnly = true)
    public String randomUserNickname() {
        String nickName;

        do {
            nickName = randomNickname.getRandomName();
        } while (duplicateNickname(nickName));

        return nickName;
    }

    @Transactional(readOnly = true)
    public Boolean duplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
