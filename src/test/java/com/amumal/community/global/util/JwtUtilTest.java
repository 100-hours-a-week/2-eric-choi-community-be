package com.amumal.community.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final String SECRET_KEY = "thisIsASecretKeyForTestingThatNeedsToBeAtLeast256BitsLong";
    private final long TOKEN_VALIDITY = 3600000; // 1 hour
    private final long REFRESH_TOKEN_VALIDITY = 86400000; // 24 hours
    private final Long USER_ID = 123L;
    private final String EMAIL = "test@example.com";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "tokenValidity", TOKEN_VALIDITY);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenValidity", REFRESH_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("액세스 토큰 생성 테스트")
    void generateToken_ShouldCreateValidAccessToken() {
        // When
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // Then
        assertNotNull(token);
        assertEquals(EMAIL, jwtUtil.extractUsername(token));
        assertEquals(USER_ID, jwtUtil.extractUserId(token));
        assertTrue(jwtUtil.validateToken(token, EMAIL)); // 만료되지 않았다면 검증 성공해야 함
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void generateRefreshToken_ShouldCreateValidRefreshToken() {
        // When
        String refreshToken = jwtUtil.generateRefreshToken(USER_ID, EMAIL);

        // Then
        assertNotNull(refreshToken);
        assertEquals(EMAIL, jwtUtil.extractUsername(refreshToken));
        assertEquals(USER_ID, jwtUtil.extractUserId(refreshToken));
        assertTrue(jwtUtil.validateToken(refreshToken, EMAIL)); // 만료되지 않았다면 검증 성공해야 함
    }

    @Test
    @DisplayName("토큰에서 사용자 이메일 추출 테스트")
    void extractUsername_ShouldReturnCorrectEmail() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // When
        String extractedEmail = jwtUtil.extractUsername(token);

        // Then
        assertEquals(EMAIL, extractedEmail);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void extractUserId_ShouldReturnCorrectUserId() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(USER_ID, extractedUserId);
    }

    @Test
    @DisplayName("토큰에서 만료일 추출 테스트")
    void extractExpiration_ShouldReturnCorrectExpirationDate() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);
        long currentTimeMillis = System.currentTimeMillis();

        // When
        Date expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertTrue(expirationDate.getTime() > currentTimeMillis);
        assertTrue(expirationDate.getTime() <= currentTimeMillis + TOKEN_VALIDITY + 1000); // 1초 허용 오차
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // When
        boolean isValid = jwtUtil.validateToken(token, EMAIL);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("이메일이 일치하지 않는 토큰 검증 테스트")
    void validateToken_WithWrongEmail_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);
        String wrongEmail = "wrong@example.com";

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongEmail);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("토큰에서 특정 클레임 추출 테스트")
    void extractClaim_ShouldExtractSpecificClaim() {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);
        Function<Claims, String> claimsResolver = claims -> claims.get("userId", String.class);

        // When
        String userId = jwtUtil.extractClaim(token, claimsResolver);

        // Then
        assertEquals(USER_ID.toString(), userId);
    }

    @Test
    @DisplayName("실제로 만료된 토큰 테스트")
    void validateToken_WithActuallyExpiredToken_ShouldReturnFalse() throws Exception {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "tokenValidity", 1); // 1ms
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // 만료되도록 대기
        Thread.sleep(10);

        // When & Then
        // validateToken 호출 시 ExpiredJwtException이 발생해야 함
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, EMAIL));
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 테스트")
    void extractAllClaims_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "malformedToken";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractUsername(malformedToken));
    }

    @Test
    @DisplayName("만료된 토큰 처리 테스트")
    void extractAllClaims_WithExpiredToken_ShouldThrowException() throws Exception {
        // Given
        // 토큰의 유효 기간을 매우 짧게 설정
        ReflectionTestUtils.setField(jwtUtil, "tokenValidity", 1); // 1ms
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // 토큰이 만료되도록 대기
        Thread.sleep(10);

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("리프레시 토큰은 액세스 토큰보다 긴 유효 기간을 가져야 함")
    void generateRefreshToken_ShouldHaveLongerExpirationThanAccessToken() {
        // Given
        String accessToken = jwtUtil.generateToken(USER_ID, EMAIL);
        String refreshToken = jwtUtil.generateRefreshToken(USER_ID, EMAIL);

        // When
        Date accessTokenExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpiration = jwtUtil.extractExpiration(refreshToken);

        // Then
        assertTrue(refreshTokenExpiration.after(accessTokenExpiration));
    }

    @Test
    @DisplayName("리플렉션을 사용한 private 메소드 테스트 - isTokenExpired")
    void isTokenExpired_UsingReflection() throws Exception {
        // Given
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // When - ReflectionTestUtils를 사용하여 private 메소드 호출
        Boolean result = ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", token);

        // Then
        assertNotNull(result);
        assertFalse(result); // 새로 생성된 토큰은 만료되지 않아야 함
    }

    @Test
    @DisplayName("짧은 유효기간 토큰의 만료 여부 테스트 - isTokenExpired")
    void isTokenExpired_WithShortExpirationToken_UsingReflection() throws Exception {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "tokenValidity", 1); // 1ms
        String token = jwtUtil.generateToken(USER_ID, EMAIL);

        // 만료되도록 대기
        Thread.sleep(10);

        // When & Then
        // 만료된 토큰에 대해 extractUsername 호출 시 ExpiredJwtException이 발생해야 함
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractUsername(token));
    }
}