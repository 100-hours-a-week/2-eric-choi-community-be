package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.UserRequestDto;
import com.amumal.community.domain.user.dto.response.UserResponseDto;
import com.amumal.community.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/new")
    public UserResponseDto registerUser(@RequestBody UserRequestDto userRequestDto) {
        return userService.registerUser(userRequestDto);
    }

    @PostMapping("/auth")
    public UserResponseDto loginUser(@RequestBody UserRequestDto userRequestDto) {
        return userService.loginUser(userRequestDto);
    }

    // 이메일 중복 체크
    @GetMapping("/duplication-email")
    public boolean checkEmailDuplication(@RequestParam String email) {
        return userService.isEmailDuplicate(email);
    }

    // 닉네임 중복 체크
    @GetMapping("/duplication-nickname")
    public boolean checkNicknameDuplication(@RequestParam String nickname) {
        return userService.isNicknameDuplicate(nickname);
    }
}