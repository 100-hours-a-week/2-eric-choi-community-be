package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import java.util.List;

public interface PostCustomRepository {
    /**
     * 게시글 상세 정보를 조회하여 DTO로 반환합니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보 DTO
     */
    PostDetailResponse getPostDetailInfoById(Long postId);

    /**
     * 커서 기반 페이지네이션을 이용하여 게시글 간단 정보 목록을 조회합니다.
     *
     * @param cursor 마지막으로 조회된 게시글 ID (null이면 첫 페이지)
     * @param pageSize 한 페이지에 조회할 게시글 수
     * @return 게시글 간단 정보 목록 DTO
     */
    List<PostResponse.PostSimpleInfo> getPostSimpleInfo(Long cursor, int pageSize);
}
