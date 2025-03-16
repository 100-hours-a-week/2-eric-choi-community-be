package com.amumal.community.domain.post.service.likes.impl;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final PostRepository postRepository; // Post 조회를 위해 Repository 직접 주입

    @Override
    public void addLike(Long postId, LikeRequest request, User currentUser) {
        if (likesRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
        // PostCommandService 대신 PostRepository를 통해 Post 엔티티를 조회
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        Likes like = Likes.builder()
                .post(post)
                .user(currentUser)
                .build();
        likesRepository.save(like);
    }

    @Override
    public void removeLike(Long postId, Long userId, User currentUser) {
        System.out.println("DEBUG: currentUser.getId() = " + currentUser.getId());
        System.out.println("DEBUG: userId = " + userId);
        // if (!currentUser.getId().equals(userId)) { ... }  // 이 조건은 주석 처리
        Optional<Likes> likeOpt = likesRepository.findByPostIdAndUserId(postId, userId);
        Likes like = likeOpt.orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        likesRepository.delete(like);
    }

}
