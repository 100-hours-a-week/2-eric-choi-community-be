package com.amumal.community.domain.post.service.likes.impl;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.domain.post.service.post.PostCommandService;
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
    private final PostCommandService postCommandService; // 필요 시 Post 조회

    @Override
    public void addLike(Long postId, LikeRequest request, User currentUser) {
        if (likesRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
        Likes like = Likes.builder()
                .post(postCommandService.findById(postId))
                .user(currentUser)
                .build();
        likesRepository.save(like);
    }

    @Override
    public void removeLike(Long postId, Long userId, User currentUser) {
        if (!currentUser.getId().equals(userId)) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
        Optional<Likes> likeOpt = likesRepository.findByPostIdAndUserId(postId, userId);
        Likes like = likeOpt.orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        likesRepository.delete(like);
    }
}
