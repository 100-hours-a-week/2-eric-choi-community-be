package com.amumal.community.global.util;

import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;

/**
 * 세션 관리를 위한 유틸리티 클래스
 */
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

    /**
     * 현재 로그인된 사용자 ID를 세션에서 가져오는 메서드
     * @param request HttpServletRequest 객체
     * @return 로그인된 사용자 ID (없으면 null 반환)
     */
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

    /**
     * 사용자 ID와 기본 정보를 세션에 저장하는 메서드
     * @param request HttpServletRequest 객체
     * @param user 로그인한 사용자 객체
     */
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

    /**
     * 사용자 ID를 세션에 저장하는 메서드 (기존 메서드 유지)
     * @param request HttpServletRequest 객체
     * @param userId 로그인한 사용자의 ID
     */
    public static void setLoggedInUserId(HttpServletRequest request, Long userId) {
        if (userId == null) {
            logger.warn("null 사용자 ID를 세션에 저장 시도");
            return;
        }

        HttpSession session = request.getSession(); // 새로운 세션 생성 (없으면 자동 생성)
        session.setAttribute(USER_ID_KEY, userId);
        logger.info("세션에 사용자 ID 저장 완료: {}, 세션 ID: {}", userId, session.getId());
    }

    /**
     * 세션에서 사용자 기본 정보 가져오기
     * @param request HttpServletRequest 객체
     * @return 사용자 기본 정보 (없으면 null)
     */
    public static UserSessionData getLoggedInUserData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (UserSessionData) session.getAttribute(USER_DATA_KEY);
    }

    /**
     * 현재 사용자의 세션을 무효화(로그아웃)하는 메서드
     * @param request HttpServletRequest 객체
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("세션 무효화: {}", session.getId());
            session.invalidate(); // 세션 삭제
        } else {
            logger.debug("무효화할 세션이 없음");
        }
    }

    /**
     * 현재 사용자가 로그인되어 있는지 확인하는 메서드
     * @param request HttpServletRequest 객체
     * @return 로그인 여부
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedInUserId(request) != null;
    }

    /**
     * 현재 세션 ID를 가져오는 메서드 (디버깅용)
     * @param request HttpServletRequest 객체
     * @return 세션 ID (세션이 없으면 null)
     */
    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }

    /**
     * 현재 로그인된 사용자 정보를 가져오는 메서드
     * @param request HttpServletRequest 객체
     * @param userService UserService 객체
     * @return 로그인된 사용자 객체 (없으면 null)
     */
    public static User getCurrentUser(HttpServletRequest request, UserService userService) {
        Long userId = getLoggedInUserId(request);
        if (userId == null) {
            return null;
        }
        return userService.findById(userId);
    }
}

//하이