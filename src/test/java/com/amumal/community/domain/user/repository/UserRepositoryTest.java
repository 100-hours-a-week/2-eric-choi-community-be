package com.amumal.community.domain.user.repository;

import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 유저를 찾을 수 있다")
    void findByEmail() {
        //given
        String email = "test@test.com";
        User user = User.builder()
                .email(email)
                .nickname("tester")
                .password("1234qwer")
                .build();

        userRepository.save(user);

        //when
        Optional<User> findByEmail = userRepository.findByEmail(email);

        //then
        assertThat(findByEmail).isPresent();
        assertThat(findByEmail.get().getEmail()).isEqualTo(email);
        assertThat(findByEmail.get().getNickname()).isEqualTo("tester");

    }

    @Test
    @DisplayName("이메일로 존재 여부 확인한다.")
    void existsByEmail() {
        //given
        String email = "test@test.com";
        User user = User.builder()
                .email(email)
                .nickname("User")
                .password("1234qwer")
                .build();

        userRepository.save(user);

        //when, then
        assertThat(userRepository.existsByEmail(email)).isTrue();
        assertThat(userRepository.existsByEmail("noemail@sadf.com")).isFalse();
    }

    @Test
    @DisplayName("닉네임으로 존재 여부 확인한다.")
    void existsByNickname() {
        //given
        String nickname = "tester";
        User user = User.builder()
                .nickname(nickname)
                .email("test@test.com")
                .password("1234qwer")
                .build();

        userRepository.save(user);

        //when,then
        assertThat(userRepository.existsByNickname(nickname)).isTrue();
        assertThat(userRepository.existsByNickname("nonickname")).isFalse();
    }
}