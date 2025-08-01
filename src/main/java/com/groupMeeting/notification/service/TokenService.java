package com.groupMeeting.notification.service;

import com.groupMeeting.dto.request.notification.token.TokenCreateRequest;
import com.groupMeeting.entity.notification.FirebaseToken;
import com.groupMeeting.entity.notification.Topic;
import com.groupMeeting.notification.repository.FirebaseTokenRepository;
import com.groupMeeting.notification.repository.TopicRepository;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final FirebaseTokenRepository tokenRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public void saveFcmToken(Long userId, TokenCreateRequest request) {
        Optional<FirebaseToken> token = tokenRepository.findByUserId(userId);

        if (token.isEmpty()) {
            tokenRepository.save(
                    FirebaseToken.builder()
                            .token(request.token())
                            .userId(userId)
                            .build()
            );

            if (request.subscribe()) {
                topicRepository.saveAll(Topic.ofNew(userId));
            }

            return;
        }

        token.get().updateToken(request.token());
    }

    @Transactional(readOnly = true)
    public List<String> getMemberTokens(@NotNull List<Long> membersId) {
        return tokenRepository.findMemberTokens(membersId);
    }
//
//    @Transactional
//    public void unsubscribeTopic(Long userId, PushTopic topic) {
//        topicRepository.delete();
//    }
}
