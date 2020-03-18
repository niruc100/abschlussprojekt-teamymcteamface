package mops.foren.infrastructure.persistence.repositories;

import mops.foren.domain.model.ForumId;
import mops.foren.domain.model.Topic;
import mops.foren.domain.model.TopicId;
import mops.foren.domain.repositoryabstraction.ITopicRepository;
import mops.foren.infrastructure.persistence.dtos.TopicDTO;
import mops.foren.infrastructure.persistence.mapper.TopicMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TopicRepositoryImpl implements ITopicRepository {
    private TopicJpaRepository topicRepository;

    public TopicRepositoryImpl(TopicJpaRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * This method get the topics of an specific forum from the db.
     *
     * @param forumId - id of the requested forum
     * @return a list of topics from the user.
     */
    @Override
    public List<Topic> getTopicsFromDB(ForumId forumId) {
        List<TopicDTO> topicDtos = this.topicRepository.findAllByForum_Id(forumId.getId());
        return getAllTopics(topicDtos);
    }


    private List<Topic> getAllTopics(List<TopicDTO> topicDtos) {
        return topicDtos.stream()
                .map(TopicMapper::mapTopicDtoToTopic)
                .collect(Collectors.toList());
    }

    @Override
    public Topic getOneTopicFromDB(TopicId topicId) {
        TopicDTO topicDto = this.topicRepository.findById(topicId.getId()).get();
        return TopicMapper.mapTopicDtoToTopic(topicDto);
    }
}
