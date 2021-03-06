package mops.foren.infrastructure.web;

import lombok.Value;
import mops.foren.domain.model.ForumId;
import mops.foren.domain.model.Topic;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

import static mops.foren.infrastructure.web.ValidationService.*;

@Value
public class TopicForm {

    private static final int MIN_DESCRIPTION_LENGTH = 3;
    private static final int MIN_TITLE_LENGTH = 3;

    @NotNull(message = "Topic title cannot be null.")
    @Size(min = MIN_TITLE_LENGTH, max = MAX_TITLE_LENGTH,
            message = "Topic title must be between "
                    + MIN_TITLE_LENGTH + " and "
                    + MAX_TITLE_LENGTH + " characters.")
    private final String title;

    @NotNull(message = "Topic description cannot be null.")
    @Size(min = MIN_DESCRIPTION_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Topic description must be between "
                    + MIN_DESCRIPTION_LENGTH + " and "
                    + MAX_DESCRIPTION_LENGTH + " charcaters.")
    private final String description;

    // @NotNull(message = "Moderated cannot be null.") // uncomment when implemented
    private final Boolean moderated;

    @NotNull(message = "Anonymous cannot be null.")
    private final Boolean anonymous;


    /**
     * This is a constructor.
     *
     * @param title       of the topic.
     * @param description of the topic.
     * @param moderated   moderation mode of the topic.
     * @param anonymous   anonymous mode of the topic.
     */
    public TopicForm(String title, String description, Boolean moderated, Boolean anonymous) {
        this.title = title;
        this.description = description;
        this.moderated = Objects.requireNonNullElse(moderated, false);
        this.anonymous = Objects.requireNonNullElse(anonymous, false);
    }

    /**
     * Method to build a post out of the postContent.
     *
     * @param forumId The id of the thread the post is in.
     * @return The resulting post object.
     */
    public Topic getTopic(ForumId forumId) {
        return Topic.builder()
                .title(this.title)
                .anonymous(this.anonymous)
                .moderated(this.moderated)
                .description(this.description)
                .moderated(this.moderated)
                .forumId(forumId)
                .build();
    }
}
