package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public Long signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // 암호화 없이 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // 비밀번호 암호화 제거
                .nickname(request.getNickname())
                .profileImage(request.getProfileImage())
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Override
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호 오류"));

        // 암호화 없이 비교
        if (!request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호 오류");
        }
        return user;
    }
}
