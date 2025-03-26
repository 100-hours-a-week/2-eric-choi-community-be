package com.amumal.community.domain.user.repository;

import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    // 테스트 상수
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_NICKNAME = "tester";
    private static final String USER_PASSWORD = "1234qwer";
    private static final String NON_EXISTENT_EMAIL = "noemail@sadf.com";
    private static final String NON_EXISTENT_NICKNAME = "nonickname";

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("이메일 관련 테스트")
    class EmailTests {

        @Test
        @DisplayName("이메일로 유저를 찾을 수 있다")
        void findByEmail_userExists_returnsUser() {
            //Given
            User user = User.builder()
                    .email(USER_EMAIL)
                    .nickname(USER_NICKNAME)
                    .password(USER_PASSWORD)
                    .build();

            userRepository.save(user);

            //When
            Optional<User> findByEmail = userRepository.findByEmail(USER_EMAIL);

            //Then
            assertThat(findByEmail).isPresent();
            assertThat(findByEmail.get().getEmail()).isEqualTo(USER_EMAIL);
            assertThat(findByEmail.get().getNickname()).isEqualTo(USER_NICKNAME);
        }

        @Test
        @DisplayName("이메일로 존재 여부 확인 가능하다")
        void existsByEmail_returnsCorrectBoolean() {
            //Given
            User user = User.builder()
                    .email(USER_EMAIL)
                    .nickname(USER_NICKNAME)
                    .password(USER_PASSWORD)
                    .build();

            userRepository.save(user);

            //When & Then
            assertThat(userRepository.existsByEmail(USER_EMAIL)).isTrue();
            assertThat(userRepository.existsByEmail(NON_EXISTENT_EMAIL)).isFalse();
        }
    }

    @Nested
    @DisplayName("닉네임 관련 테스트")
    class NicknameTests {

        @Test
        @DisplayName("닉네임으로 존재 여부 확인 가능하다")
        void existsByNickname_returnsCorrectBoolean() {
            //Given
            User user = User.builder()
                    .nickname(USER_NICKNAME)
                    .email(USER_EMAIL)
                    .password(USER_PASSWORD)
                    .build();

            userRepository.save(user);

            //When & Then
            assertThat(userRepository.existsByNickname(USER_NICKNAME)).isTrue();
            assertThat(userRepository.existsByNickname(NON_EXISTENT_NICKNAME)).isFalse();
        }
    }
}