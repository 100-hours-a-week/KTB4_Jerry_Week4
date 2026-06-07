package ktb.fullstack.talktalk.domain.post.port;

public interface CommentCountQuery {

    int getCountByPostId(Long postId);
}
