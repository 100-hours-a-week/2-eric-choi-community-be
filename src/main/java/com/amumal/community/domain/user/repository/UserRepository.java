package com.amumal.community.domain.user.repository;

import com.amumal.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);

    // 삭제되지 않은 사용자 찾기
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // 삭제되지 않은 사용자 찾기
    Optional<User> findByNicknameAndDeletedAtIsNull(String nickname);

    // 삭제되지 않은 사용자만 조회
    List<User> findByDeletedAtIsNull();

    // 특정 사용자의 삭제 상태 변경
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
