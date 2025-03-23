package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.domain.user.service.UserQueryService;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
    }
}
