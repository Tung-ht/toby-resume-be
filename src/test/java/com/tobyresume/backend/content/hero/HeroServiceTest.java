package com.tobyresume.backend.content.hero;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.hero.dto.HeroRequest;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import com.tobyresume.backend.content.hero.model.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeroServiceTest {

    @Mock
    private HeroRepository heroRepository;

    @Mock
    private HeroMapper heroMapper;

    @InjectMocks
    private HeroService heroService;

    private HeroRequest request;
    private Hero draftHero;
    private HeroResponse response;

    @BeforeEach
    void setUp() {
        request = new HeroRequest();
        request.setTagline(Map.of("en", "Developer"));
        request.setFullName(Map.of("en", "Toby"));

        draftHero = new Hero();
        draftHero.setId("id1");
        draftHero.setContentState(ContentState.DRAFT);

        response = new HeroResponse();
        response.setTagline(request.getTagline());
        response.setFullName(request.getFullName());
    }

    @Test
    void getDraft_returnsNull_whenNoDraft() {
        when(heroRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);

        HeroResponse result = heroService.getDraft();

        assertThat(result).isNull();
    }

    @Test
    void getDraft_returnsResponse_whenDraftExists() {
        when(heroRepository.findByContentState(ContentState.DRAFT)).thenReturn(draftHero);
        when(heroMapper.entityToResponse(draftHero)).thenReturn(response);

        HeroResponse result = heroService.getDraft();

        assertThat(result).isSameAs(response);
    }

    @Test
    void upsertDraft_createsNew_whenNoDraft() {
        when(heroRepository.findByContentState(ContentState.DRAFT)).thenReturn(null);
        when(heroMapper.requestToEntity(request)).thenReturn(draftHero);
        when(heroRepository.save(draftHero)).thenReturn(draftHero);
        when(heroMapper.entityToResponse(draftHero)).thenReturn(response);

        HeroResponse result = heroService.upsertDraft(request);

        assertThat(result).isSameAs(response);
        verify(heroMapper).requestToEntity(request);
        verify(heroRepository).save(draftHero);
    }

    @Test
    void upsertDraft_updatesExisting_whenDraftExists() {
        when(heroRepository.findByContentState(ContentState.DRAFT)).thenReturn(draftHero);
        when(heroRepository.save(draftHero)).thenReturn(draftHero);
        when(heroMapper.entityToResponse(draftHero)).thenReturn(response);

        HeroResponse result = heroService.upsertDraft(request);

        assertThat(result).isSameAs(response);
        verify(heroMapper).updateEntityFromRequest(eq(request), eq(draftHero));
        verify(heroRepository).save(draftHero);
    }
}
