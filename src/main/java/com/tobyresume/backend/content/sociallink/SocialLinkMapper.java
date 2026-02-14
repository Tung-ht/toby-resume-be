package com.tobyresume.backend.content.sociallink;

import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemRequest;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemResponse;
import com.tobyresume.backend.content.sociallink.model.SocialLinkItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SocialLinkMapper {

    @Mapping(target = "itemId", ignore = true)
    SocialLinkItem requestToItem(SocialLinkItemRequest request);

    @Mapping(target = "itemId", ignore = true)
    void updateItemFromRequest(SocialLinkItemRequest request, @org.mapstruct.MappingTarget SocialLinkItem item);

    SocialLinkItemResponse itemToResponse(SocialLinkItem item);

    List<SocialLinkItemResponse> itemsToResponses(List<SocialLinkItem> items);
}
