package com.tobyresume.backend.content.project;

import com.tobyresume.backend.common.model.Link;
import com.tobyresume.backend.content.project.dto.LinkDto;
import com.tobyresume.backend.content.project.dto.ProjectItemRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemResponse;
import com.tobyresume.backend.content.project.model.ProjectItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper: ProjectItemRequest ↔ ProjectItem (with LinkDto ↔ Link), ProjectItem → ProjectItemResponse.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    Link linkDtoToLink(LinkDto dto);

    @Mapping(target = "itemId", ignore = true)
    ProjectItem requestToItem(ProjectItemRequest request);

    @Mapping(target = "itemId", ignore = true)
    void updateItemFromRequest(ProjectItemRequest request, @org.mapstruct.MappingTarget ProjectItem item);

    ProjectItemResponse itemToResponse(ProjectItem item);

    List<ProjectItemResponse> itemsToResponses(List<ProjectItem> items);
}
