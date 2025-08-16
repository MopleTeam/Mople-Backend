package com.mople.user.service;

import com.mople.core.exception.custom.AuthException;
import com.mople.dto.request.user.RandomNicknameRequest;
import com.mople.dto.request.user.UserInfoRequest;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.image.service.ImageService;
import com.mople.user.repository.UserRepository;

import com.mople.user.repository.UserRepositorySupport;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RandomNicknameRequest randomNickname = new RandomNicknameRequest();
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final UserRepositorySupport userRepositorySupport;

    @Transactional(readOnly = true)
    public UserInfo getInfo(Long id) {
        return UserInfo.from(findUser(id));
    }

    @Transactional
    public UserInfo updateInfo(Long id, UserInfoRequest updateInfo) {
        User user = findUser(id);

        if (user.imageValid()) {
            imageService.deleteImage(user.getProfileImg());
        }

        user.updateImageAndNickname(updateInfo.image(), updateInfo.nickname());

        return UserInfo.from(user);
    }

    @Transactional
    public void removeUser(final Long id) {
        userRepositorySupport.removeUser(id);
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

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new AuthException(ExceptionReturnCode.NOT_USER)
        );
    }
}
