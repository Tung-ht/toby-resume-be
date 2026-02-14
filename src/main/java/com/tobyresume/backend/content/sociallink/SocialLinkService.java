package com.tobyresume.backend.content.sociallink;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemRequest;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemResponse;
import com.tobyresume.backend.content.sociallink.model.SocialLink;
import com.tobyresume.backend.content.sociallink.model.SocialLinkItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;

    public SocialLinkService(SocialLinkRepository socialLinkRepository, SocialLinkMapper socialLinkMapper) {
        this.socialLinkRepository = socialLinkRepository;
        this.socialLinkMapper = socialLinkMapper;
    }

    private SocialLink getOrCreateDraft() {
        SocialLink draft = socialLinkRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new SocialLink();
            draft.setContentState(ContentState.DRAFT);
            draft.setItems(new ArrayList<>());
            draft = socialLinkRepository.save(draft);
        }
        if (draft.getItems() == null) draft.setItems(new ArrayList<>());
        return draft;
    }

    public List<SocialLinkItemResponse> list() {
        SocialLink draft = socialLinkRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getItems() == null || draft.getItems().isEmpty()) return List.of();
        return socialLinkMapper.itemsToResponses(
                draft.getItems().stream().sorted(Comparator.comparingInt(SocialLinkItem::getOrder)).collect(Collectors.toList()));
    }

    /** List published social link items, sorted by order. Used by GraphQL. */
    public List<SocialLinkItemResponse> listPublished() {
        SocialLink published = socialLinkRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getItems() == null || published.getItems().isEmpty()) return List.of();
        return socialLinkMapper.itemsToResponses(
                published.getItems().stream().sorted(Comparator.comparingInt(SocialLinkItem::getOrder)).collect(Collectors.toList()));
    }

    public SocialLinkItemResponse get(String itemId) {
        SocialLink draft = getOrCreateDraft();
        return socialLinkMapper.itemToResponse(findItemById(draft, itemId));
    }

    public SocialLinkItemResponse add(SocialLinkItemRequest request) {
        SocialLink draft = getOrCreateDraft();
        List<SocialLinkItem> items = draft.getItems();
        int nextOrder = items.isEmpty() ? 0 : items.stream().mapToInt(SocialLinkItem::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) nextOrder = request.getOrder();

        SocialLinkItem item = socialLinkMapper.requestToItem(request);
        item.setItemId(IdGenerator.uuid());
        item.setOrder(nextOrder);
        items.add(item);
        draft.setItems(items);
        SocialLink saved = socialLinkRepository.save(draft);
        SocialLinkItem savedItem = saved.getItems().stream().filter(i -> i.getItemId().equals(item.getItemId())).findFirst().orElse(item);
        return socialLinkMapper.itemToResponse(savedItem);
    }

    public SocialLinkItemResponse update(String itemId, SocialLinkItemRequest request) {
        SocialLink draft = getOrCreateDraft();
        SocialLinkItem item = findItemById(draft, itemId);
        socialLinkMapper.updateItemFromRequest(request, item);
        socialLinkRepository.save(draft);
        return socialLinkMapper.itemToResponse(item);
    }

    public void delete(String itemId) {
        SocialLink draft = getOrCreateDraft();
        List<SocialLinkItem> items = draft.getItems();
        if (!items.removeIf(i -> i.getItemId().equals(itemId))) throw new ResourceNotFoundException("Social link item not found: " + itemId);
        draft.setItems(items);
        socialLinkRepository.save(draft);
    }

    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        SocialLink draft = getOrCreateDraft();
        List<SocialLinkItem> items = draft.getItems();
        if (items.isEmpty()) return;
        List<String> existingIds = items.stream().map(SocialLinkItem::getItemId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds))
            throw new ValidationException("orderedIds must contain exactly the same item IDs as current draft");
        IntStream.range(0, orderedIds.size()).forEach(i -> items.stream().filter(it -> it.getItemId().equals(orderedIds.get(i))).findFirst().ifPresent(it -> it.setOrder(i)));
        items.sort(Comparator.comparingInt(SocialLinkItem::getOrder));
        draft.setItems(items);
        socialLinkRepository.save(draft);
    }

    private SocialLinkItem findItemById(SocialLink draft, String itemId) {
        return draft.getItems().stream().filter(i -> i.getItemId().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Social link item not found: " + itemId));
    }
}
