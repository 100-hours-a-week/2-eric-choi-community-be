package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.post.PostQueryService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;

    @Override
    public PostResponse getPostSimpleInfo(Long cursor, int pageSize) {
        List<PostSimpleInfo> simpleInfos = postRepository.getPostSimpleInfo(cursor, pageSize);
        Long nextCursor = simpleInfos.isEmpty() ? null : simpleInfos.get(simpleInfos.size() - 1).postId();
        return PostResponse.builder()
                .postSimpleInfos(simpleInfos)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    @Transactional
    public PostDetailResponse getPostDetailInfoById(Long postId, Boolean incrementView) {
        // incrementView 파라미터가 true일 때만 조회수 증가
        if (incrementView != null && incrementView) {
            // Post 엔티티에서 조회수 증가 메서드 호출
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
            post.incrementViewCount();
            postRepository.save(post);

            // 또는 직접 update 쿼리를 실행하는 경우:
            // postRepository.incrementViewCount(postId);
        }

        // 게시글 상세 정보 조회
        PostDetailResponse detailResponse = postRepository.getPostDetailInfoById(postId);
        if(detailResponse == null) {
            throw new CustomException(CustomResponseStatus.NOT_FOUND);
        }
        return detailResponse;
    }
}