package com.amumal.community.global.util;

import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;

public class SessionUtil {
    private static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);
    private static final String USER_ID_KEY = "USER_ID";
    private static final String USER_DATA_KEY = "USER_DATA";

    /**
     * 세션에 저장할 사용자 기본 정보 클래스
     */
    public static class UserSessionData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Long id;
        private final String email;
        private final String nickname;

        public UserSessionData(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.nickname = user.getNickname();
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getNickname() {
            return nickname;
        }
    }

    public static Long getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 기존 세션 가져오기 (없으면 null)
        if (session == null) {
            logger.debug("세션이 존재하지 않음");
            return null;
        }

        Long userId = (Long) session.getAttribute(USER_ID_KEY);
        if (userId == null) {
            logger.debug("세션에 사용자 ID가 없음");
        } else {
            logger.debug("세션에서 사용자 ID 로드: {}", userId);
        }

        return userId;
    }

    public static void setLoggedInUser(HttpServletRequest request, User user) {
        if (user == null) {
            logger.warn("null 사용자 객체를 세션에 저장 시도");
            return;
        }

        HttpSession session = request.getSession(); // 새로운 세션 생성 (없으면 자동 생성)
        session.setAttribute(USER_ID_KEY, user.getId());

        // 경량화된 사용자 정보만 세션에 저장 (프로필 이미지 제외)
        UserSessionData sessionData = new UserSessionData(user);
        session.setAttribute(USER_DATA_KEY, sessionData);

        logger.info("세션에 사용자 정보 저장 완료: {}, 세션 ID: {}", user.getEmail(), session.getId());
    }

    public static void setLoggedInUserId(HttpServletRequest request, Long userId) {
        if (userId == null) {
            logger.warn("null 사용자 ID를 세션에 저장 시도");
            return;
        }

        HttpSession session = request.getSession(); // 새로운 세션 생성 (없으면 자동 생성)
        session.setAttribute(USER_ID_KEY, userId);
        logger.info("세션에 사용자 ID 저장 완료: {}, 세션 ID: {}", userId, session.getId());
    }


    public static UserSessionData getLoggedInUserData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (UserSessionData) session.getAttribute(USER_DATA_KEY);
    }


    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("세션 무효화: {}", session.getId());
            session.invalidate(); // 세션 삭제
        } else {
            logger.debug("무효화할 세션이 없음");
        }
    }


    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }


    public static User getCurrentUser(HttpServletRequest request, UserService userService) {
        Long userId = getLoggedInUserId(request);
        if (userId == null) {
            return null;
        }
        return userService.findById(userId);
    }
}

