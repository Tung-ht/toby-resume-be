package com.tobyresume.backend.content.certification;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.certification.dto.CertificationItemRequest;
import com.tobyresume.backend.content.certification.dto.CertificationItemResponse;
import com.tobyresume.backend.content.certification.model.Certification;
import com.tobyresume.backend.content.certification.model.CertificationItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final CertificationMapper certificationMapper;

    public CertificationService(CertificationRepository certificationRepository, CertificationMapper certificationMapper) {
        this.certificationRepository = certificationRepository;
        this.certificationMapper = certificationMapper;
    }

    private Certification getOrCreateDraft() {
        Certification draft = certificationRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new Certification();
            draft.setContentState(ContentState.DRAFT);
            draft.setItems(new ArrayList<>());
            draft = certificationRepository.save(draft);
        }
        if (draft.getItems() == null) draft.setItems(new ArrayList<>());
        return draft;
    }

    public List<CertificationItemResponse> list() {
        Certification draft = certificationRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getItems() == null || draft.getItems().isEmpty()) return List.of();
        return certificationMapper.itemsToResponses(
                draft.getItems().stream().sorted(Comparator.comparingInt(CertificationItem::getOrder)).collect(Collectors.toList()));
    }

    /** List published certification items, sorted by order. Used by GraphQL. */
    public List<CertificationItemResponse> listPublished() {
        Certification published = certificationRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getItems() == null || published.getItems().isEmpty()) return List.of();
        return certificationMapper.itemsToResponses(
                published.getItems().stream().sorted(Comparator.comparingInt(CertificationItem::getOrder)).collect(Collectors.toList()));
    }

    public CertificationItemResponse get(String itemId) {
        Certification draft = getOrCreateDraft();
        return certificationMapper.itemToResponse(findItemById(draft, itemId));
    }

    public CertificationItemResponse add(CertificationItemRequest request) {
        Certification draft = getOrCreateDraft();
        List<CertificationItem> items = draft.getItems();
        int nextOrder = items.isEmpty() ? 0 : items.stream().mapToInt(CertificationItem::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) nextOrder = request.getOrder();

        CertificationItem item = certificationMapper.requestToItem(request);
        item.setItemId(IdGenerator.uuid());
        item.setOrder(nextOrder);
        items.add(item);
        draft.setItems(items);
        Certification saved = certificationRepository.save(draft);
        CertificationItem savedItem = saved.getItems().stream().filter(i -> i.getItemId().equals(item.getItemId())).findFirst().orElse(item);
        return certificationMapper.itemToResponse(savedItem);
    }

    public CertificationItemResponse update(String itemId, CertificationItemRequest request) {
        Certification draft = getOrCreateDraft();
        CertificationItem item = findItemById(draft, itemId);
        certificationMapper.updateItemFromRequest(request, item);
        certificationRepository.save(draft);
        return certificationMapper.itemToResponse(item);
    }

    public void delete(String itemId) {
        Certification draft = getOrCreateDraft();
        List<CertificationItem> items = draft.getItems();
        if (!items.removeIf(i -> i.getItemId().equals(itemId))) throw new ResourceNotFoundException("Certification item not found: " + itemId);
        draft.setItems(items);
        certificationRepository.save(draft);
    }

    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        Certification draft = getOrCreateDraft();
        List<CertificationItem> items = draft.getItems();
        if (items.isEmpty()) return;
        List<String> existingIds = items.stream().map(CertificationItem::getItemId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds))
            throw new ValidationException("orderedIds must contain exactly the same item IDs as current draft");
        IntStream.range(0, orderedIds.size()).forEach(i -> items.stream().filter(it -> it.getItemId().equals(orderedIds.get(i))).findFirst().ifPresent(it -> it.setOrder(i)));
        items.sort(Comparator.comparingInt(CertificationItem::getOrder));
        draft.setItems(items);
        certificationRepository.save(draft);
    }

    private CertificationItem findItemById(Certification draft, String itemId) {
        return draft.getItems().stream().filter(i -> i.getItemId().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Certification item not found: " + itemId));
    }
}
