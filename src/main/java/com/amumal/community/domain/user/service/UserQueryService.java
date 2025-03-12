package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;

public interface UserQueryService {
    UserInfoResponse getUserInfoById(Long id);
    boolean isEmailDuplicate(String email);
    boolean isNicknameDuplicate(String nickname);
    User getUserByEmail(String email);
}
