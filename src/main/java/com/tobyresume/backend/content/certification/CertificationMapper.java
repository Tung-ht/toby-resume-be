package com.tobyresume.backend.content.certification;

import com.tobyresume.backend.content.certification.dto.CertificationItemRequest;
import com.tobyresume.backend.content.certification.dto.CertificationItemResponse;
import com.tobyresume.backend.content.certification.model.CertificationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CertificationMapper {

    @Mapping(target = "itemId", ignore = true)
    CertificationItem requestToItem(CertificationItemRequest request);

    @Mapping(target = "itemId", ignore = true)
    void updateItemFromRequest(CertificationItemRequest request, @org.mapstruct.MappingTarget CertificationItem item);

    CertificationItemResponse itemToResponse(CertificationItem item);

    List<CertificationItemResponse> itemsToResponses(List<CertificationItem> items);
}
