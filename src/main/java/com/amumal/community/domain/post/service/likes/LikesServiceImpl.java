package com.amumal.community.domain.post.service.likes;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.post.service.post.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final PostService postService; // to fetch Post entity if needed

    @Override
    public void addLike(Long postId, LikeRequest request, User currentUser) {
        // 예시: 중복 좋아요 체크 (repository 메서드 existsByPostIdAndUserId 사용)
        if (likesRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST); // 이미 좋아요 한 경우, 적절한 예외 처리
        }
        Likes like = Likes.builder()
                .post(postService.findById(postId))
                .user(currentUser)
                .build();
        likesRepository.save(like);
    }

    @Override
    public void removeLike(Long postId, Long userId, User currentUser) {
        // currentUser와 userId 일치 여부를 확인
        if (!currentUser.getId().equals(userId)) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
        Optional<Likes> likeOpt = likesRepository.findByPostIdAndUserId(postId, userId);
        Likes like = likeOpt.orElseThrow(() -> new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST));
        likesRepository.delete(like);
    }
}