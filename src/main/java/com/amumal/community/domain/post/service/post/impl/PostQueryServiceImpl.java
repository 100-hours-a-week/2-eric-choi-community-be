package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
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
    @Transactional // 중요: 수정 작업이므로 트랜잭션 필요
    public PostDetailResponse getPostDetailInfoById(Long postId) {
        // 조회수 증가
        postRepository.incrementViewCount(postId);

        // 게시글 상세 정보 조회
        PostDetailResponse detailResponse = postRepository.getPostDetailInfoById(postId);
        if(detailResponse == null) {
            throw new CustomException(CustomResponseStatus.NOT_FOUND);
        }
        return detailResponse;
    }
}