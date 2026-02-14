package com.tobyresume.backend.content.project;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.project.dto.ProjectItemRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemResponse;
import com.tobyresume.backend.content.project.model.Project;
import com.tobyresume.backend.content.project.model.ProjectItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project draft;
    private ProjectItem item1;
    private ProjectItemRequest request;
    private ProjectItemResponse response;

    @BeforeEach
    void setUp() {
        draft = new Project();
        draft.setId("doc1");
        draft.setContentState(ContentState.DRAFT);
        item1 = new ProjectItem();
        item1.setItemId("item-1");
        item1.setTitle(Map.of("en", "My Project"));
        item1.setVisible(true);
        item1.setOrder(0);
        draft.setItems(new ArrayList<>(List.of(item1)));

        request = new ProjectItemRequest();
        request.setTitle(Map.of("en", "New Project"));
        request.setVisible(true);

        response = new ProjectItemResponse();
        response.setItemId("item-1");
        response.setTitle(request.getTitle());
    }

    @Test
    void list_returnsEmpty_whenNoDraft() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);
        assertThat(projectService.list()).isEmpty();
    }

    @Test
    void get_throwsNotFound_whenItemIdMissing() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        assertThatThrownBy(() -> projectService.get("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void get_returnsItem_whenFound() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(projectMapper.itemToResponse(item1)).thenReturn(response);
        assertThat(projectService.get("item-1")).isSameAs(response);
        verify(projectMapper).itemToResponse(item1);
    }

    @Test
    void add_createsDraftAndItem_whenNoDraft() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        ProjectItem newItem = new ProjectItem();
        newItem.setItemId("new-id");
        newItem.setTitle(request.getTitle());
        newItem.setOrder(0);
        when(projectMapper.requestToItem(request)).thenReturn(newItem);
        when(projectMapper.itemToResponse(any())).thenReturn(response);

        ProjectItemResponse result = projectService.add(request);

        assertThat(result).isNotNull();
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Project savedWithItem = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(savedWithItem.getContentState()).isEqualTo(ContentState.DRAFT);
        assertThat(savedWithItem.getItems()).hasSize(1);
    }

    @Test
    void update_throwsNotFound_whenItemIdMissing() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        assertThatThrownBy(() -> projectService.update("nonexistent", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesItemAndSaves() {
        when(projectRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        projectService.delete("item-1");
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).isEmpty();
    }
}
