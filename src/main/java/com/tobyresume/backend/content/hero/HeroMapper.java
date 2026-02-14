package com.tobyresume.backend.content.hero;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.hero.dto.HeroRequest;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import com.tobyresume.backend.content.hero.model.Hero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper: HeroRequest → Hero (DRAFT), Hero → HeroResponse.
 */
@Mapper(componentModel = "spring")
public interface HeroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contentState", constant = "DRAFT")
    Hero requestToEntity(HeroRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contentState", constant = "DRAFT")
    void updateEntityFromRequest(HeroRequest request, @MappingTarget Hero hero);

    HeroResponse entityToResponse(Hero hero);
}
