package com.tobyresume.backend.content.experience;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.experience.dto.ExperienceItemRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemResponse;
import com.tobyresume.backend.content.experience.model.ExperienceItem;
import com.tobyresume.backend.content.experience.model.WorkExperience;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Work experience CRUD on DRAFT only. List, get, add, update, delete, reorder.
 */
@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceMapper experienceMapper;

    public ExperienceService(ExperienceRepository experienceRepository, ExperienceMapper experienceMapper) {
        this.experienceRepository = experienceRepository;
        this.experienceMapper = experienceMapper;
    }

    private WorkExperience getOrCreateDraft() {
        WorkExperience draft = experienceRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new WorkExperience();
            draft.setContentState(ContentState.DRAFT);
            draft.setItems(new ArrayList<>());
            draft = experienceRepository.save(draft);
        }
        if (draft.getItems() == null) {
            draft.setItems(new ArrayList<>());
        }
        return draft;
    }

    private List<ExperienceItem> itemsSortedByOrder(WorkExperience doc) {
        return doc.getItems().stream()
                .sorted(Comparator.comparingInt(ExperienceItem::getOrder))
                .collect(Collectors.toList());
    }

    /**
     * List all draft experience items, sorted by order ascending.
     */
    public List<ExperienceItemResponse> list() {
        WorkExperience draft = experienceRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getItems() == null || draft.getItems().isEmpty()) {
            return List.of();
        }
        return experienceMapper.itemsToResponses(itemsSortedByOrder(draft));
    }

    /**
     * List published experience items, sorted by order ascending. Used by GraphQL.
     */
    public List<ExperienceItemResponse> listPublished() {
        WorkExperience published = experienceRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getItems() == null || published.getItems().isEmpty()) {
            return List.of();
        }
        return experienceMapper.itemsToResponses(itemsSortedByOrder(published));
    }

    /**
     * Get one item by itemId. 404 if not found.
     */
    public ExperienceItemResponse get(String itemId) {
        WorkExperience draft = getOrCreateDraft();
        ExperienceItem item = findItemById(draft, itemId);
        return experienceMapper.itemToResponse(item);
    }

    /**
     * Add a new item. Server assigns itemId; order defaults to last.
     */
    public ExperienceItemResponse add(ExperienceItemRequest request) {
        WorkExperience draft = getOrCreateDraft();
        List<ExperienceItem> items = draft.getItems();
        int nextOrder = items.isEmpty() ? 0 : items.stream().mapToInt(ExperienceItem::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) {
            nextOrder = request.getOrder();
        }

        ExperienceItem item = experienceMapper.requestToItem(request);
        item.setItemId(IdGenerator.uuid());
        item.setOrder(nextOrder);
        items.add(item);
        draft.setItems(items);
        WorkExperience saved = experienceRepository.save(draft);
        ExperienceItem savedItem = saved.getItems().stream()
                .filter(i -> i.getItemId().equals(item.getItemId()))
                .findFirst()
                .orElse(item);
        return experienceMapper.itemToResponse(savedItem);
    }

    /**
     * Update an existing item. 404 if itemId not found.
     */
    public ExperienceItemResponse update(String itemId, ExperienceItemRequest request) {
        WorkExperience draft = getOrCreateDraft();
        ExperienceItem item = findItemById(draft, itemId);
        experienceMapper.updateItemFromRequest(request, item);
        experienceRepository.save(draft);
        return experienceMapper.itemToResponse(item);
    }

    /**
     * Delete an item. 404 if itemId not found.
     */
    public void delete(String itemId) {
        WorkExperience draft = getOrCreateDraft();
        List<ExperienceItem> items = draft.getItems();
        boolean removed = items.removeIf(i -> i.getItemId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Experience item not found: " + itemId);
        }
        draft.setItems(items);
        experienceRepository.save(draft);
    }

    /**
     * Reorder items by the given orderedIds. Each id must exist; order becomes index in list.
     */
    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        WorkExperience draft = getOrCreateDraft();
        List<ExperienceItem> items = draft.getItems();
        if (items.isEmpty()) {
            return;
        }

        List<String> existingIds = items.stream().map(ExperienceItem::getItemId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds)) {
            throw new ValidationException("orderedIds must contain exactly the same item IDs as current draft");
        }

        IntStream.range(0, orderedIds.size()).forEach(index -> {
            String id = orderedIds.get(index);
            items.stream().filter(i -> i.getItemId().equals(id)).findFirst().ifPresent(i -> i.setOrder(index));
        });
        items.sort(Comparator.comparingInt(ExperienceItem::getOrder));
        draft.setItems(items);
        experienceRepository.save(draft);
    }

    private ExperienceItem findItemById(WorkExperience draft, String itemId) {
        return draft.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Experience item not found: " + itemId));
    }
}
