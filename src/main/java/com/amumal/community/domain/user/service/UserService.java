package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.UserRequestDto;
import com.amumal.community.domain.user.dto.response.UserResponseDto;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        if (isEmailDuplicate(userRequestDto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        if (isNicknameDuplicate(userRequestDto.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = new User();
        user.setEmail(userRequestDto.getEmail());
        user.setPassword(userRequestDto.getPassword()); // 비밀번호는 암호화 처리
        user.setNickname(userRequestDto.getNickname());
        user.setProfileImage(userRequestDto.getProfileImage());
        userRepository.save(user);

        return new UserResponseDto(user.getId(), "register_success");
    }

    // 이메일 중복 검사
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 닉네임 중복 검사
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    // 로그인
    public UserResponseDto loginUser(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.checkPassword(userRequestDto.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new UserResponseDto(user.getId(), "login_success");
    }
}
