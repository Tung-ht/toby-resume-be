package com.tobyresume.backend.content.project;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.project.dto.ProjectItemRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemResponse;
import com.tobyresume.backend.content.project.model.Project;
import com.tobyresume.backend.content.project.model.ProjectItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Projects CRUD on DRAFT only. List, get, add, update, delete, reorder.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    private Project getOrCreateDraft() {
        Project draft = projectRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new Project();
            draft.setContentState(ContentState.DRAFT);
            draft.setItems(new ArrayList<>());
            draft = projectRepository.save(draft);
        }
        if (draft.getItems() == null) {
            draft.setItems(new ArrayList<>());
        }
        return draft;
    }

    private List<ProjectItem> itemsSortedByOrder(Project doc) {
        return doc.getItems().stream()
                .sorted(Comparator.comparingInt(ProjectItem::getOrder))
                .collect(Collectors.toList());
    }

    public List<ProjectItemResponse> list() {
        Project draft = projectRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getItems() == null || draft.getItems().isEmpty()) {
            return List.of();
        }
        return projectMapper.itemsToResponses(itemsSortedByOrder(draft));
    }

    /**
     * List published project items with visible == true only, sorted by order. Used by GraphQL.
     */
    public List<ProjectItemResponse> listPublishedVisible() {
        Project published = projectRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getItems() == null) {
            return List.of();
        }
        List<ProjectItem> visible = published.getItems().stream()
                .filter(ProjectItem::isVisible)
                .sorted(Comparator.comparingInt(ProjectItem::getOrder))
                .collect(Collectors.toList());
        return projectMapper.itemsToResponses(visible);
    }

    public ProjectItemResponse get(String itemId) {
        Project draft = getOrCreateDraft();
        ProjectItem item = findItemById(draft, itemId);
        return projectMapper.itemToResponse(item);
    }

    public ProjectItemResponse add(ProjectItemRequest request) {
        Project draft = getOrCreateDraft();
        List<ProjectItem> items = draft.getItems();
        int nextOrder = items.isEmpty() ? 0 : items.stream().mapToInt(ProjectItem::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) {
            nextOrder = request.getOrder();
        }

        ProjectItem item = projectMapper.requestToItem(request);
        item.setItemId(IdGenerator.uuid());
        item.setOrder(nextOrder);
        if (request.getVisible() != null) {
            item.setVisible(request.getVisible());
        }
        items.add(item);
        draft.setItems(items);
        Project saved = projectRepository.save(draft);
        ProjectItem savedItem = saved.getItems().stream()
                .filter(i -> i.getItemId().equals(item.getItemId()))
                .findFirst()
                .orElse(item);
        return projectMapper.itemToResponse(savedItem);
    }

    public ProjectItemResponse update(String itemId, ProjectItemRequest request) {
        Project draft = getOrCreateDraft();
        ProjectItem item = findItemById(draft, itemId);
        projectMapper.updateItemFromRequest(request, item);
        if (request.getVisible() != null) {
            item.setVisible(request.getVisible());
        }
        projectRepository.save(draft);
        return projectMapper.itemToResponse(item);
    }

    public void delete(String itemId) {
        Project draft = getOrCreateDraft();
        List<ProjectItem> items = draft.getItems();
        boolean removed = items.removeIf(i -> i.getItemId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Project item not found: " + itemId);
        }
        draft.setItems(items);
        projectRepository.save(draft);
    }

    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        Project draft = getOrCreateDraft();
        List<ProjectItem> items = draft.getItems();
        if (items.isEmpty()) {
            return;
        }
        List<String> existingIds = items.stream().map(ProjectItem::getItemId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds)) {
            throw new ValidationException("orderedIds must contain exactly the same item IDs as current draft");
        }
        IntStream.range(0, orderedIds.size()).forEach(index -> {
            String id = orderedIds.get(index);
            items.stream().filter(i -> i.getItemId().equals(id)).findFirst().ifPresent(i -> i.setOrder(index));
        });
        items.sort(Comparator.comparingInt(ProjectItem::getOrder));
        draft.setItems(items);
        projectRepository.save(draft);
    }

    private ProjectItem findItemById(Project draft, String itemId) {
        return draft.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Project item not found: " + itemId));
    }
}
