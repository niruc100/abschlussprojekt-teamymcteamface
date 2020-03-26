package mops.foren.domain.repositoryabstraction;

import mops.foren.domain.model.Post;
import mops.foren.domain.model.PostId;
import mops.foren.domain.model.ThreadId;
import mops.foren.domain.model.User;
import mops.foren.domain.model.paging.PostPage;

import java.util.List;

public interface IPostRepository {
    PostPage getPostPageFromDB(ThreadId threadId, Integer page);

    Post getPostById(PostId postId);

    List<Post> getPostsFromUser(User user);

    List<Post> getAllPostsByThreadId(ThreadId id);

    void deletePostById(PostId postId);
}
