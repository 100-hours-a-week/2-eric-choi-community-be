package com.amumal.community.domain.post.service.post;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostSimpleInfo(Long cursor, int pageSize) {
        List<PostSimpleInfo> simpleInfos = postRepository.getPostSimpleInfo(cursor, pageSize);
        Long nextCursor = simpleInfos.isEmpty() ? null : simpleInfos.get(simpleInfos.size() - 1).postId();
        return PostResponse.builder()
                .postSimpleInfos(simpleInfos)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetailInfoById(Long postId) {
        PostDetailResponse detailResponse = postRepository.getPostDetailInfoById(postId);
        if (detailResponse == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST); // 혹은 NOT_FOUND 등 적절한 상태로
        }
        return detailResponse;
    }

    @Override
    public Long createPost(PostRequest request, User currentUser) {
        Post post = Post.builder()
                .user(currentUser)
                .title(request.title())
                .content(request.content())
                .image(request.image())
                .viewCount(0)
                .build();
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Override
    public void updatePost(Long postId, PostRequest request, User currentUser) {
        Post post = findById(postId);
        post.update(request, currentUser);
        // JPA가 관리하는 엔티티이므로 별도 save 호출 없이 트랜잭션 종료 시 반영됨.
    }

    @Override
    public void deletePost(Long postId, User currentUser) {
        Post post = findById(postId);
        post.safeDelete(currentUser);
    }

    @Override
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST));
    }
}