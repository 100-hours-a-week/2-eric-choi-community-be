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
    private final PostRepository postRepository;

    @Override
    public void addLike(Long postId, LikeRequest request, User currentUser) {
        // 이미 좋아요를 눌렀는지 확인
        if (likesRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            removeLike(postId, currentUser.getId(), currentUser);
            return;
        }

        // 새로운 좋아요 추가
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

        Optional<Likes> likeOpt = likesRepository.findByPostIdAndUserId(postId, userId);
        Likes like = likeOpt.orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        likesRepository.delete(like);
    }
}