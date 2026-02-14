package com.tobyresume.backend.content.experience;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.experience.dto.ExperienceItemRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemResponse;
import com.tobyresume.backend.content.experience.model.ExperienceItem;
import com.tobyresume.backend.content.experience.model.WorkExperience;
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
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ExperienceMapper experienceMapper;

    @InjectMocks
    private ExperienceService experienceService;

    private WorkExperience draft;
    private ExperienceItem item1;
    private ExperienceItemRequest request;
    private ExperienceItemResponse response;

    @BeforeEach
    void setUp() {
        draft = new WorkExperience();
        draft.setId("doc1");
        draft.setContentState(ContentState.DRAFT);
        item1 = new ExperienceItem();
        item1.setItemId("item-1");
        item1.setCompany(Map.of("en", "TechCorp"));
        item1.setRole(Map.of("en", "Engineer"));
        item1.setStartDate("2023-01");
        item1.setOrder(0);
        draft.setItems(new ArrayList<>(List.of(item1)));

        request = new ExperienceItemRequest();
        request.setCompany(Map.of("en", "NewCorp"));
        request.setRole(Map.of("en", "Senior Dev"));
        request.setStartDate("2024-01");

        response = new ExperienceItemResponse();
        response.setItemId("item-1");
        response.setCompany(request.getCompany());
        response.setRole(request.getRole());
    }

    @Test
    void list_returnsEmpty_whenNoDraft() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);

        List<ExperienceItemResponse> result = experienceService.list();

        assertThat(result).isEmpty();
    }

    @Test
    void list_returnsItemsSortedByOrder_whenDraftExists() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(experienceMapper.itemsToResponses(any())).thenReturn(List.of(response));

        List<ExperienceItemResponse> result = experienceService.list();

        assertThat(result).hasSize(1);
        verify(experienceMapper).itemsToResponses(any());
    }

    @Test
    void get_throwsNotFound_whenItemIdMissing() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);

        assertThatThrownBy(() -> experienceService.get("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void get_returnsItem_whenFound() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(experienceMapper.itemToResponse(item1)).thenReturn(response);

        ExperienceItemResponse result = experienceService.get("item-1");

        assertThat(result).isSameAs(response);
        verify(experienceMapper).itemToResponse(item1);
    }

    @Test
    void add_createsDraftAndItem_whenNoDraft() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);
        when(experienceRepository.save(any(WorkExperience.class))).thenAnswer(inv -> inv.getArgument(0));
        ExperienceItem newItem = new ExperienceItem();
        newItem.setItemId("new-id");
        newItem.setCompany(request.getCompany());
        newItem.setRole(request.getRole());
        newItem.setStartDate(request.getStartDate());
        newItem.setOrder(0);
        when(experienceMapper.requestToItem(request)).thenReturn(newItem);
        when(experienceMapper.itemToResponse(any())).thenReturn(response);

        ExperienceItemResponse result = experienceService.add(request);

        assertThat(result).isNotNull();
        ArgumentCaptor<WorkExperience> captor = ArgumentCaptor.forClass(WorkExperience.class);
        verify(experienceRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        // First save: empty draft creation; second save: draft with new item
        WorkExperience savedWithItem = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(savedWithItem.getContentState()).isEqualTo(ContentState.DRAFT);
        assertThat(savedWithItem.getItems()).hasSize(1);
    }

    @Test
    void update_throwsNotFound_whenItemIdMissing() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);

        assertThatThrownBy(() -> experienceService.update("nonexistent", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_savesDraft_whenItemFound() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(experienceRepository.save(draft)).thenReturn(draft);
        when(experienceMapper.itemToResponse(item1)).thenReturn(response);

        experienceService.update("item-1", request);

        verify(experienceMapper).updateItemFromRequest(eq(request), eq(item1));
        verify(experienceRepository).save(draft);
    }

    @Test
    void delete_throwsNotFound_whenItemIdMissing() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);

        assertThatThrownBy(() -> experienceService.delete("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesItemAndSaves() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(experienceRepository.save(any(WorkExperience.class))).thenAnswer(inv -> inv.getArgument(0));

        experienceService.delete("item-1");

        ArgumentCaptor<WorkExperience> captor = ArgumentCaptor.forClass(WorkExperience.class);
        verify(experienceRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).isEmpty();
    }

    @Test
    void reorder_updatesOrderByPosition() {
        when(experienceRepository.findByContentState(ContentState.DRAFT)).thenReturn(draft);
        when(experienceRepository.save(any(WorkExperience.class))).thenAnswer(inv -> inv.getArgument(0));
        ExperienceItem item2 = new ExperienceItem();
        item2.setItemId("item-2");
        item2.setOrder(1);
        draft.getItems().add(item2);

        experienceService.reorder(List.of("item-2", "item-1"));

        ArgumentCaptor<WorkExperience> captor = ArgumentCaptor.forClass(WorkExperience.class);
        verify(experienceRepository).save(captor.capture());
        List<ExperienceItem> items = captor.getValue().getItems();
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getItemId()).isEqualTo("item-2");
        assertThat(items.get(0).getOrder()).isZero();
        assertThat(items.get(1).getItemId()).isEqualTo("item-1");
        assertThat(items.get(1).getOrder()).isOne();
    }
}
