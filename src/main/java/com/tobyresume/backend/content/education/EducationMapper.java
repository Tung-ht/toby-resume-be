package com.tobyresume.backend.content.education;

import com.tobyresume.backend.content.education.dto.EducationItemRequest;
import com.tobyresume.backend.content.education.dto.EducationItemResponse;
import com.tobyresume.backend.content.education.model.EducationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EducationMapper {

    @Mapping(target = "itemId", ignore = true)
    EducationItem requestToItem(EducationItemRequest request);

    @Mapping(target = "itemId", ignore = true)
    void updateItemFromRequest(EducationItemRequest request, @org.mapstruct.MappingTarget EducationItem item);

    EducationItemResponse itemToResponse(EducationItem item);

    List<EducationItemResponse> itemsToResponses(List<EducationItem> items);
}
