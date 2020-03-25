package mops.foren.infrastructure.persistence.repositories;

import mops.foren.domain.model.Post;
import mops.foren.domain.model.PostId;
import mops.foren.domain.model.ThreadId;
import mops.foren.domain.model.User;
import mops.foren.domain.model.paging.PostPage;
import mops.foren.domain.repositoryabstraction.IPostRepository;
import mops.foren.infrastructure.persistence.dtos.PostDTO;
import mops.foren.infrastructure.persistence.dtos.UserDTO;
import mops.foren.infrastructure.persistence.mapper.PostMapper;
import mops.foren.infrastructure.persistence.mapper.PostPageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryImpl implements IPostRepository {

    private static final int PAGE_SIZE = 10;
    private PostJpaRepository postRepository;


    public PostRepositoryImpl(PostJpaRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostPage getPostPageFromDB(ThreadId threadId, Integer page) {
        Page<PostDTO> dtoPage = this.postRepository
                .findPostPageByThread_Id(threadId.getId(), PageRequest.of(page, PAGE_SIZE));

        return PostPageMapper.toPostPage(dtoPage, page);
    }

    /**
     * This method get a Post with the given id.
     *
     * @param postId the id of the post, which is wanted.
     * @return the wanted post.
     */
    @Override
    public Post getPostById(PostId postId) {
        PostDTO postDTO = this.postRepository.findById(postId.getId()).get();
        return PostMapper.mapPostDtoToPost(postDTO);
    }

    /**
     * This method gets all Post from an user.
     *
     * @param user the user, where the posts are wanted.
     * @return a list of all post from the user.
     */
    @Override
    public List<Post> getPostsFromUser(User user) {
        List<PostDTO> postByAuthor = this.postRepository
                .findPostListByAuthor_Username(user.getName());
        return postByAuthor.stream()
                .map(PostMapper::mapPostDtoToPost)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getAllPostsByThreadId(ThreadId id) {
        return this.postRepository.findPostDTOByThread_Id(id.getId()).stream()
                .map(PostMapper::mapPostDtoToPost)
                .collect(Collectors.toList());
    }


    @Override
    public void deletePostById(PostId postId) {
        PostDTO postDTO = this.postRepository.findPostById(postId.getId());
        UserDTO defaultDeletedUserDTO = UserDTO.builder().username("Unbekannt").build();

        postDTO.setAuthor(defaultDeletedUserDTO);
        postDTO.setText("Dieser Beitrag wurde entfernt.");

        this.postRepository.save(postDTO);
    }
}
