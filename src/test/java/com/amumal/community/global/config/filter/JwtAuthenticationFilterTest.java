package com.amumal.community.global.config.filter;

import com.amumal.community.global.config.security.CustomUserDetailsService;
import com.amumal.community.global.util.JwtUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private final String TEST_EMAIL = "test@example.com";
    private final Long TEST_USER_ID = 123L;
    private final String TEST_TOKEN = "testJwtToken";
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 테스트 전에 SecurityContextHolder 초기화
        SecurityContextHolder.clearContext();

        // HTTP 요청, 응답 및 필터 체인 모의 객체 생성
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        // 테스트용 UserDetails 생성
        userDetails = new User(TEST_EMAIL, "password", Collections.emptyList());
    }

    @Test
    @DisplayName("유효한 JWT 토큰이 있는 경우 인증이 성공해야 함")
    void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
        // Given
        // Authorization 헤더에 Bearer 토큰 설정
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // JWT 유틸 모의 설정
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtUtil.validateToken(TEST_TOKEN, TEST_EMAIL)).thenReturn(true);
        when(jwtUtil.extractUserId(TEST_TOKEN)).thenReturn(TEST_USER_ID);

        // UserDetailsService 모의 설정
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에 인증 객체가 설정되었는지 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(TEST_EMAIL, authentication.getName());

        // userId가 request 속성으로 추가되었는지 확인
        assertEquals(TEST_USER_ID, request.getAttribute("userId"));

        // 필터 체인이 계속 진행되었는지 확인
        verify(userDetailsService).loadUserByUsername(TEST_EMAIL);
        verify(jwtUtil).validateToken(TEST_TOKEN, TEST_EMAIL);
        verify(jwtUtil).extractUserId(TEST_TOKEN);
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 경우 인증을 시도하지 않아야 함")
    void doFilterInternal_WithoutAuthHeader_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        // Authorization 헤더 없음

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에 인증 객체가 설정되지 않았는지 확인
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // UserDetailsService나 JwtUtil이 호출되지 않았는지 확인
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 Authorization 헤더의 경우 인증을 시도하지 않아야 함")
    void doFilterInternal_WithNonBearerToken_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        // Bearer가 아닌 다른 형식의 Authorization 헤더
        request.addHeader("Authorization", "Basic " + TEST_TOKEN);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에 인증 객체가 설정되지 않았는지 확인
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // UserDetailsService나 JwtUtil이 호출되지 않았는지 확인
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("JWT 토큰 처리 중 예외 발생 시 인증을 시도하지 않아야 함")
    void doFilterInternal_WhenExceptionInTokenProcessing_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        // Authorization 헤더에 Bearer 토큰 설정
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // JWT 추출 시 예외 발생하도록 설정
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenThrow(new RuntimeException("토큰 처리 오류"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에 인증 객체가 설정되지 않았는지 확인
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // 예외가 발생했지만 필터 체인은 계속 진행되어야 함
        verify(jwtUtil).extractUsername(TEST_TOKEN);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("토큰 검증 실패 시 인증을 설정하지 않아야 함")
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 이메일 추출은 성공하지만 토큰 검증은 실패하도록 설정
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtUtil.validateToken(TEST_TOKEN, TEST_EMAIL)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에 인증 객체가 설정되지 않았는지 확인
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // 토큰은 검증되었지만 인증이 설정되지 않아야 함
        verify(jwtUtil).extractUsername(TEST_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_EMAIL);
        verify(jwtUtil).validateToken(TEST_TOKEN, TEST_EMAIL);
        verify(jwtUtil, never()).extractUserId(anyString());
    }

    @Test
    @DisplayName("이미 인증된 상태에서는 토큰 처리를 시도하지 않아야 함")
    void doFilterInternal_WhenAlreadyAuthenticated_ShouldNotProcessToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 이미 인증된 상태로 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // UserDetailsService가 호출되지 않았는지 확인
        verify(jwtUtil).extractUsername(TEST_TOKEN);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }
}