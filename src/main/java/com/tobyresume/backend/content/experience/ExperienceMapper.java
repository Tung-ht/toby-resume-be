package com.tobyresume.backend.content.experience;

import com.tobyresume.backend.content.experience.dto.ExperienceItemRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemResponse;
import com.tobyresume.backend.content.experience.model.ExperienceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper: ExperienceItemRequest ↔ ExperienceItem, ExperienceItem → ExperienceItemResponse.
 */
@Mapper(componentModel = "spring")
public interface ExperienceMapper {

    @Mapping(target = "itemId", ignore = true)
    ExperienceItem requestToItem(ExperienceItemRequest request);

    @Mapping(target = "itemId", ignore = true)
    void updateItemFromRequest(ExperienceItemRequest request, @org.mapstruct.MappingTarget ExperienceItem item);

    ExperienceItemResponse itemToResponse(ExperienceItem item);

    List<ExperienceItemResponse> itemsToResponses(List<ExperienceItem> items);
}
