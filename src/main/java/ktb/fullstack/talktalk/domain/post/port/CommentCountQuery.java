package ktb.fullstack.talktalk.domain.post.port;

import java.util.List;
import java.util.Map;

public interface CommentCountQuery {

    int getCountByPostId(Long postId);

    Map<Long, Integer> getCountsByPostIds(List<Long> postIds);
}
