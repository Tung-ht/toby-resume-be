package com.tobyresume.backend.content.hero;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.hero.dto.HeroRequest;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import com.tobyresume.backend.content.hero.model.Hero;
import org.springframework.stereotype.Service;

/**
 * Hero CRUD on DRAFT only. getDraft() returns null if none; upsertDraft() creates or updates the single DRAFT document.
 */
@Service
public class HeroService {

    private final HeroRepository heroRepository;
    private final HeroMapper heroMapper;

    public HeroService(HeroRepository heroRepository, HeroMapper heroMapper) {
        this.heroRepository = heroRepository;
        this.heroMapper = heroMapper;
    }

    /**
     * Returns the draft hero, or null if none exists.
     */
    public HeroResponse getDraft() {
        Hero draft = heroRepository.findByContentState(ContentState.DRAFT);
        return draft == null ? null : heroMapper.entityToResponse(draft);
    }

    /**
     * Returns the published hero, or null if none exists. Used by GraphQL.
     */
    public HeroResponse getPublished() {
        Hero published = heroRepository.findByContentState(ContentState.PUBLISHED);
        return published == null ? null : heroMapper.entityToResponse(published);
    }

    /**
     * Creates or updates the single DRAFT hero. Idempotent; full replace.
     */
    public HeroResponse upsertDraft(HeroRequest request) {
        Hero draft = heroRepository.findByContentState(ContentState.DRAFT);
        if (draft == null) {
            draft = heroMapper.requestToEntity(request);
        } else {
            heroMapper.updateEntityFromRequest(request, draft);
        }
        Hero saved = heroRepository.save(draft);
        return heroMapper.entityToResponse(saved);
    }
}
