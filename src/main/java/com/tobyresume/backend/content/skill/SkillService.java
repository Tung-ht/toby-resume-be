package com.tobyresume.backend.content.skill;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.util.IdGenerator;
import com.tobyresume.backend.content.skill.dto.SkillCategoryRequest;
import com.tobyresume.backend.content.skill.dto.SkillCategoryResponse;
import com.tobyresume.backend.content.skill.model.Skill;
import com.tobyresume.backend.content.skill.model.SkillCategory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillService(SkillRepository skillRepository, SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
    }

    private Skill getOrCreateDraft() {
        Skill draft = skillRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = new Skill();
            draft.setContentState(ContentState.DRAFT);
            draft.setCategories(new ArrayList<>());
            draft = skillRepository.save(draft);
        }
        if (draft.getCategories() == null) draft.setCategories(new ArrayList<>());
        return draft;
    }

    public List<SkillCategoryResponse> list() {
        Skill draft = skillRepository.findByContentState(ContentState.DRAFT);
        if (draft == null || draft.getCategories() == null || draft.getCategories().isEmpty()) return List.of();
        return skillMapper.categoriesToResponses(
                draft.getCategories().stream().sorted(Comparator.comparingInt(SkillCategory::getOrder)).collect(Collectors.toList()));
    }

    /** List published skill categories with items, sorted by order. Used by GraphQL. */
    public List<SkillCategoryResponse> listPublished() {
        Skill published = skillRepository.findByContentState(ContentState.PUBLISHED);
        if (published == null || published.getCategories() == null || published.getCategories().isEmpty()) return List.of();
        return skillMapper.categoriesToResponses(
                published.getCategories().stream().sorted(Comparator.comparingInt(SkillCategory::getOrder)).collect(Collectors.toList()));
    }

    public SkillCategoryResponse get(String categoryId) {
        Skill draft = getOrCreateDraft();
        SkillCategory cat = findCategoryById(draft, categoryId);
        return skillMapper.categoryToResponse(cat);
    }

    public SkillCategoryResponse add(SkillCategoryRequest request) {
        Skill draft = getOrCreateDraft();
        List<SkillCategory> categories = draft.getCategories();
        int nextOrder = categories.isEmpty() ? 0 : categories.stream().mapToInt(SkillCategory::getOrder).max().orElse(-1) + 1;
        if (request.getOrder() != null) nextOrder = request.getOrder();

        SkillCategory category = skillMapper.requestToCategory(request);
        category.setCategoryId(IdGenerator.uuid());
        category.setOrder(nextOrder);
        if (request.getItems() != null) {
            category.setItems(skillMapper.itemRequestsToItems(request.getItems()));
        } else {
            category.setItems(new ArrayList<>());
        }
        categories.add(category);
        draft.setCategories(categories);
        Skill saved = skillRepository.save(draft);
        SkillCategory savedCat = saved.getCategories().stream().filter(c -> c.getCategoryId().equals(category.getCategoryId())).findFirst().orElse(category);
        return skillMapper.categoryToResponse(savedCat);
    }

    public SkillCategoryResponse update(String categoryId, SkillCategoryRequest request) {
        Skill draft = getOrCreateDraft();
        SkillCategory category = findCategoryById(draft, categoryId);
        skillMapper.updateCategoryFromRequest(request, category);
        if (request.getItems() != null) {
            category.setItems(skillMapper.itemRequestsToItems(request.getItems()));
        }
        skillRepository.save(draft);
        return skillMapper.categoryToResponse(category);
    }

    public void delete(String categoryId) {
        Skill draft = getOrCreateDraft();
        List<SkillCategory> categories = draft.getCategories();
        if (!categories.removeIf(c -> c.getCategoryId().equals(categoryId)))
            throw new ResourceNotFoundException("Skill category not found: " + categoryId);
        draft.setCategories(categories);
        skillRepository.save(draft);
    }

    public void reorder(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        Skill draft = getOrCreateDraft();
        List<SkillCategory> categories = draft.getCategories();
        if (categories.isEmpty()) return;
        List<String> existingIds = categories.stream().map(SkillCategory::getCategoryId).sorted().toList();
        List<String> sortedRequestIds = orderedIds.stream().sorted().toList();
        if (existingIds.size() != sortedRequestIds.size() || !existingIds.equals(sortedRequestIds))
            throw new ValidationException("orderedIds must contain exactly the same category IDs as current draft");
        IntStream.range(0, orderedIds.size()).forEach(i -> categories.stream().filter(c -> c.getCategoryId().equals(orderedIds.get(i))).findFirst().ifPresent(c -> c.setOrder(i)));
        categories.sort(Comparator.comparingInt(SkillCategory::getOrder));
        draft.setCategories(categories);
        skillRepository.save(draft);
    }

    private SkillCategory findCategoryById(Skill draft, String categoryId) {
        return draft.getCategories().stream().filter(c -> c.getCategoryId().equals(categoryId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Skill category not found: " + categoryId));
    }
}
