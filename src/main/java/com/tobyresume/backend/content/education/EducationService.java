package com.tobyresume.backend.content.education;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.education.dto.EducationItemRequest;
import com.tobyresume.backend.content.education.dto.EducationItemResponse;
import com.tobyresume.backend.content.education.model.Education;
import com.tobyresume.backend.content.education.model.EducationItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EducationService {

    private final EducationRepository educationRepository;
    private final EducationMapper educationMapper;

    public EducationService(EducationRepository educationRepository, EducationMapper educationMapper) {
        this.educationRepository = educationRepository;
        this.educationMapper = educationMapper;
    }

    private Education getOrCreateDraft() {
        Education draft = educationRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new Education();
            draft.setContentState(ContentState.DRAFT);
            draft.setItems(new ArrayList<>());
            draft = educationRepository.save(draft);
        }
        if (draft.getItems() == null) draft.setItems(new ArrayList<>());
        return draft;
    }

    public List<EducationItemResponse> list() {
        Education draft = educationRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getItems() == null || draft.getItems().isEmpty())
            return List.of();
        return educationMapper.itemsToResponses(
                draft.getItems().stream().sorted(Comparator.comparingInt(EducationItem::getOrder)).collect(Collectors.toList()));
    }

    /** List published education items, sorted by order. Used by GraphQL. */
    public List<EducationItemResponse> listPublished() {
        Education published = educationRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getItems() == null || published.getItems().isEmpty())
            return List.of();
        return educationMapper.itemsToResponses(
                published.getItems().stream().sorted(Comparator.comparingInt(EducationItem::getOrder)).collect(Collectors.toList()));
    }

    public EducationItemResponse get(String itemId) {
        Education draft = getOrCreateDraft();
        EducationItem item = findItemById(draft, itemId);
        return educationMapper.itemToResponse(item);
    }

    public EducationItemResponse add(EducationItemRequest request) {
        Education draft = getOrCreateDraft();
        List<EducationItem> items = draft.getItems();
        int nextOrder = items.isEmpty() ? 0 : items.stream().mapToInt(EducationItem::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) nextOrder = request.getOrder();

        EducationItem item = educationMapper.requestToItem(request);
        item.setItemId(IdGenerator.uuid());
        item.setOrder(nextOrder);
        items.add(item);
        draft.setItems(items);
        Education saved = educationRepository.save(draft);
        EducationItem savedItem = saved.getItems().stream().filter(i -> i.getItemId().equals(item.getItemId())).findFirst().orElse(item);
        return educationMapper.itemToResponse(savedItem);
    }

    public EducationItemResponse update(String itemId, EducationItemRequest request) {
        Education draft = getOrCreateDraft();
        EducationItem item = findItemById(draft, itemId);
        educationMapper.updateItemFromRequest(request, item);
        educationRepository.save(draft);
        return educationMapper.itemToResponse(item);
    }

    public void delete(String itemId) {
        Education draft = getOrCreateDraft();
        List<EducationItem> items = draft.getItems();
        if (!items.removeIf(i -> i.getItemId().equals(itemId)))
            throw new ResourceNotFoundException("Education item not found: " + itemId);
        draft.setItems(items);
        educationRepository.save(draft);
    }

    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        Education draft = getOrCreateDraft();
        List<EducationItem> items = draft.getItems();
        if (items.isEmpty()) return;
        List<String> existingIds = items.stream().map(EducationItem::getItemId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds))
            throw new ValidationException("orderedIds must contain exactly the same item IDs as current draft");
        IntStream.range(0, orderedIds.size()).forEach(i -> items.stream().filter(it -> it.getItemId().equals(orderedIds.get(i))).findFirst().ifPresent(it -> it.setOrder(i)));
        items.sort(Comparator.comparingInt(EducationItem::getOrder));
        draft.setItems(items);
        educationRepository.save(draft);
    }

    private EducationItem findItemById(Education draft, String itemId) {
        return draft.getItems().stream().filter(i -> i.getItemId().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Education item not found: " + itemId));
    }
}
