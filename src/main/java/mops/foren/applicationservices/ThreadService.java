package mops.foren.applicationservices;

import mops.foren.domain.model.Thread;
import mops.foren.domain.model.*;
import mops.foren.domain.model.paging.ThreadPage;
import mops.foren.domain.repositoryabstraction.IThreadRepository;

@ApplicationService
public class ThreadService {

    private IThreadRepository threadRepository;

    public ThreadService(IThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public ThreadPage getThreadPageWithTopicId(TopicId topicId, Integer page) {
        return this.threadRepository.getThreadPageFromDB(topicId, page);
    }

    public Thread getThreadById(ThreadId threadId) {
        return this.threadRepository.getThreadById(threadId);
    }


    public void addPostInThread(ThreadId threadId, Post post) {
        this.threadRepository.addPostInThread(threadId, post);
    }


    /**
     * Method to add a thread.
     *
     * @param thread The thread to add
     * @param user   The user that wants to create the thread
     * @param id     The topicId the thread belongs to
     */
    public void addThread(Thread thread, User user, ForumId id) {
        if (user.checkPermission(id, Permission.CREATE_THREAD)) {
            // ADD
        }
    }
}
