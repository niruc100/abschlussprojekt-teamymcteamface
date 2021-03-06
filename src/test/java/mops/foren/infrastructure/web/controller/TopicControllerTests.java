package mops.foren.infrastructure.web.controller;

import mops.foren.applicationservices.*;
import mops.foren.domain.model.Forum;
import mops.foren.domain.model.ForumId;
import mops.foren.domain.model.Topic;
import mops.foren.domain.model.User;
import mops.foren.domain.model.paging.Paging;
import mops.foren.domain.model.paging.ThreadPage;
import mops.foren.infrastructure.web.Account;
import mops.foren.infrastructure.web.KeycloakService;
import mops.foren.infrastructure.web.KeycloakTokenMock;
import mops.foren.infrastructure.web.TopicForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TopicControllerTests {
    @Autowired
    MockMvc mvcMock;

    @Autowired
    WebApplicationContext context;

    @MockBean
    KeycloakService keycloakServiceMock;

    @MockBean
    UserService userServiceMock;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    TopicService topicServiceMock;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    ForumService forumServiceMock;

    @MockBean
    PostService postServiceMock;

    @MockBean
    ThreadService threadServiceMock;

    @MockBean
    User userMock;


    /**
     * Building up a security environment for the Test.
     */
    @BeforeEach
    public void setup() {
        this.mvcMock = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();

        Account fakeAccount = Account.builder()
                .name("studentin")
                .roles(Set.of("studentin"))
                .build();
        KeycloakTokenMock.setupTokenMock(fakeAccount);
    }

    @Test
    void testEnterATopicWithoutPermission() throws Exception {
        Paging paging = new Paging(true, true, false, 0, 0L, 0);
        ThreadPage threadPage = new ThreadPage(paging, List.of());

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(false);
        when(this.topicServiceMock.getTopic(any())).thenReturn(Topic.builder().build());
        when(this.threadServiceMock.getThreadPageByVisibility(any(), any(), any()))
                .thenReturn(threadPage);
        when(this.threadServiceMock.countInvisibleThreads(any())).thenReturn(1);

        this.mvcMock.perform(get("/foren/topic/?topicId=1&page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-no-permission"));
    }


    @Test
    void testEnterATopicWithPermission() throws Exception {

        Paging paging = new Paging(true, true, false, 0, 0L, 0);
        ThreadPage threadPage = new ThreadPage(paging, List.of());
        Forum fakeForum = Forum.builder().id(new ForumId(1L)).title("")
                .lastChange(LocalDateTime.now()).build();

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.topicServiceMock.getTopic(any()).getForumId()).thenReturn(fakeForum.getId());
        when(this.threadServiceMock.getThreadPageByVisibility(any(), any(), any()))
                .thenReturn(threadPage);
        when(this.threadServiceMock.countInvisibleThreads(any())).thenReturn(0);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when((this.forumServiceMock.getForum(any()).getTitle())).thenReturn(fakeForum.getTitle());

        this.mvcMock.perform(get("/foren/topic/?topicId=1&page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-threads"));
    }

    @Test
    void testEnterATopicModel() throws Exception {

        Paging paging = new Paging(true, true, false, 0, 0L, 0);
        ThreadPage threadPage = new ThreadPage(paging, List.of());
        Forum fakeForum = Forum.builder().id(new ForumId(1L)).title("")
                .lastChange(LocalDateTime.now()).build();

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.topicServiceMock.getTopic(any()).getForumId()).thenReturn(fakeForum.getId());
        when(this.threadServiceMock.getThreadPageByVisibility(any(), any(), any()))
                .thenReturn(threadPage);
        when(this.threadServiceMock.countInvisibleThreads(any())).thenReturn(0);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when((this.forumServiceMock.getForum(any()).getTitle())).thenReturn(fakeForum.getTitle());

        this.mvcMock.perform(get("/foren/topic/?topicId=1&page=0"))
                .andExpect(model().attributeExists("topic"))
                .andExpect(model().attribute("forumTitle", ""))
                .andExpect(model().attribute("forumId", 1L))
                .andExpect(model().attribute("topicId", 1L))
                .andExpect(model().attribute("pagingObject", paging))
                .andExpect(model().attribute("threads", List.of()))
                .andExpect(model().attribute("countInvisibleThreads", 0))
                .andExpect(model().attribute("moderatePermission", true))
                .andExpect(model().attribute("deletePermission", true));
    }

    @Test
    void testEnterATopicAsModeratorWithoutPermission() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(false);
        when((this.topicServiceMock.getTopic(any()).getForumId())).thenReturn(new ForumId(1L));

        this.mvcMock.perform(get("/foren/topic/moderationview?topicId=1&page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-no-permission"));
    }

    @Test
    void testEnterATopicAsModeratorWithPermission() throws Exception {

        Paging paging = new Paging(true, true, false, 0, 0L, 0);
        ThreadPage threadPage = new ThreadPage(paging, List.of());

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when((this.topicServiceMock.getTopic(any()).getForumId())).thenReturn(new ForumId(1L));
        when(this.threadServiceMock.getThreadPageByVisibility(any(), any(), any()))
                .thenReturn(threadPage);

        this.mvcMock.perform(get("/foren/topic/moderationview?topicId=1&page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-threads-moderator"));
    }

    @Test
    void testEnterATopicAsModeratorWithPermissionModel() throws Exception {

        Paging paging = new Paging(true, true, false, 0, 0L, 0);
        ThreadPage threadPage = new ThreadPage(paging, List.of());

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when(this.forumServiceMock.getForum(any()).getTitle()).thenReturn("");
        when((this.topicServiceMock.getTopic(any()).getForumId())).thenReturn(new ForumId(1L));
        when(this.threadServiceMock.getThreadPageByVisibility(any(), any(), any()))
                .thenReturn(threadPage);

        this.mvcMock.perform(get("/foren/topic/moderationview?topicId=1&page=0"))
                .andExpect(model().attribute("forumId", 1L))
                .andExpect(model().attribute("topicId", 1L))
                .andExpect(model().attribute("forumTitle", ""))
                .andExpect(model().attribute("pagingObject", paging))
                .andExpect(model().attribute("threads", List.of()))
                .andExpect(model().attribute("deletePermission", true));
    }

    @Test
    void testCreateTopicView() throws Exception {

        this.mvcMock.perform(get("/foren/topic/create-topic?forumId=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-topic"));
    }

    @Test
    void testCreateTopicModel() throws Exception {

        this.mvcMock.perform(get("/foren/topic/create-topic?forumId=1"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("minTitleLength", "maxTitleLength",
                        "minDescriptionLength", "maxDescriptionLength"))
                .andExpect(model().attribute("forumId", 1L))
                .andExpect(model().attribute("form",
                        new TopicForm("", "", false, false)));
    }

    @Test
    void testAddATopicBindingResult() throws Exception {

        this.mvcMock.perform(post("/foren/topic/add-topic?forumId=1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foren/topic/create-topic?forumId=1"));
    }

    @Test
    void testAddATopicWithoutPermission() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(false);

        this.mvcMock.perform(post("/foren/topic/add-topic?forumId=1")
                .param("title", "       ")
                .param("description", "       ")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-no-permission"));
    }

    @Test
    void testAddATopicWithPermission() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);

        this.mvcMock.perform(post("/foren/topic/add-topic?forumId=1")
                .param("title", "       ")
                .param("description", "       ")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foren/my-forums/enter?forumId=1"));
    }

    @Test
    void testAddATopicWithPermissionInvocationOfAdd() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);

        this.mvcMock.perform(post("/foren/topic/add-topic?forumId=1")
                .param("title", "       ")
                .param("description", "       ")
                .with(csrf()));

        verify(this.forumServiceMock).addTopicInForum(any(), any());
    }

    @Test
    void testDeleteATopicWithoutPermission() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(false);

        this.mvcMock.perform(post("/foren/topic/delete-topic?topicId=1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-no-permission"));
    }

    @Test
    void testDeleteATopicWithPermissionView() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when((this.topicServiceMock.getTopic(any()).getForumId())).thenReturn(new ForumId(1L));

        this.mvcMock.perform(post("/foren/topic/delete-topic?topicId=1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foren/my-forums/enter?forumId=1"));
    }

    @Test
    void testDeleteATopicWithPermissionInvokeDelete() throws Exception {

        when(this.userServiceMock.getUserFromDB(any())).thenReturn(this.userMock);
        when(this.userMock.checkPermission(any(), any())).thenReturn(true);
        when((this.topicServiceMock.getTopic(any()).getForumId())).thenReturn(new ForumId(1L));

        this.mvcMock.perform(post("/foren/topic/delete-topic?topicId=1")
                .with(csrf()));

        verify(this.topicServiceMock).deleteTopic(any());
    }

    @Test
    void testAccountIsInModel() throws Exception {
        Account fakeAccount = Account.builder()
                .name("orga")
                .roles(Set.of("orga"))
                .build();
        KeycloakTokenMock.setupTokenMock(fakeAccount);

        when(this.keycloakServiceMock.createAccountFromPrincipal(
                any(KeycloakAuthenticationToken.class))).thenReturn(fakeAccount);

        this.mvcMock.perform(get("/foren/topic/create-topic?forumId=1"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("account", fakeAccount));
    }
}
