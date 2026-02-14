package com.tobyresume.backend.content.skill;

import com.tobyresume.backend.content.skill.dto.SkillCategoryRequest;
import com.tobyresume.backend.content.skill.dto.SkillCategoryResponse;
import com.tobyresume.backend.content.skill.dto.SkillItemRequest;
import com.tobyresume.backend.content.skill.dto.SkillItemResponse;
import com.tobyresume.backend.content.skill.model.SkillCategory;
import com.tobyresume.backend.content.skill.model.SkillItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    SkillItem itemRequestToItem(SkillItemRequest request);

    SkillItemResponse itemToResponse(SkillItem item);

    List<SkillItem> itemRequestsToItems(List<SkillItemRequest> requests);

    List<SkillItemResponse> itemsToResponses(List<SkillItem> items);

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "items", source = "items")
    SkillCategory requestToCategory(SkillCategoryRequest request);

    @Mapping(target = "categoryId", ignore = true)
    void updateCategoryFromRequest(SkillCategoryRequest request, @org.mapstruct.MappingTarget SkillCategory category);

    SkillCategoryResponse categoryToResponse(SkillCategory category);

    List<SkillCategoryResponse> categoriesToResponses(List<SkillCategory> categories);
}
