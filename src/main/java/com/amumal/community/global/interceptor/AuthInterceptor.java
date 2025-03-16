package com.amumal.community.global.interceptor;

import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 로그인이 필요없는 경로는 제외 (OPTIONS, 로그인, 회원가입 등)
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (method.equals("OPTIONS") ||
                path.equals("/users/new") ||
                path.equals("/users/auth") ||
                (path.startsWith("/posts") && method.equals("GET"))) {
            return true;
        }

        // 세션에서 사용자 ID 확인
        Long userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        return true;
    }
}