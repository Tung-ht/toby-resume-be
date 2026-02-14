package com.tobyresume.backend.graphql;

import com.tobyresume.backend.content.certification.CertificationService;
import com.tobyresume.backend.content.certification.dto.CertificationItemResponse;
import com.tobyresume.backend.content.education.EducationService;
import com.tobyresume.backend.content.education.dto.EducationItemResponse;
import com.tobyresume.backend.content.experience.ExperienceService;
import com.tobyresume.backend.content.experience.dto.ExperienceItemResponse;
import com.tobyresume.backend.content.hero.HeroService;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import com.tobyresume.backend.content.project.ProjectService;
import com.tobyresume.backend.content.project.dto.ProjectItemResponse;
import com.tobyresume.backend.content.skill.SkillService;
import com.tobyresume.backend.content.skill.dto.SkillCategoryResponse;
import com.tobyresume.backend.content.skill.dto.SkillItemResponse;
import com.tobyresume.backend.content.sociallink.SocialLinkService;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemResponse;
import com.tobyresume.backend.graphql.model.*;
import com.tobyresume.backend.settings.SettingsService;
import com.tobyresume.backend.settings.dto.SiteSettingsResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL root Query resolvers. PUBLISHED content only; locale from argument or settings default.
 *
 * @see docs/ai/design/api-design.md §8
 */
@Controller
public class ContentGraphQLController {

    private final HeroService heroService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final CertificationService certificationService;
    private final SocialLinkService socialLinkService;
    private final SkillService skillService;
    private final SettingsService settingsService;

    public ContentGraphQLController(HeroService heroService,
                                   ExperienceService experienceService,
                                   ProjectService projectService,
                                   EducationService educationService,
                                   CertificationService certificationService,
                                   SocialLinkService socialLinkService,
                                   SkillService skillService,
                                   SettingsService settingsService) {
        this.heroService = heroService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.certificationService = certificationService;
        this.socialLinkService = socialLinkService;
        this.skillService = skillService;
        this.settingsService = settingsService;
    }

    @QueryMapping
    public Hero hero(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        HeroResponse r = heroService.getPublished();
        if (r == null) return null;
        Hero out = new Hero();
        out.setTagline(mapValue(r.getTagline(), localeStr));
        out.setBio(mapValue(r.getBio(), localeStr));
        out.setFullName(mapValue(r.getFullName(), localeStr));
        out.setTitle(mapValue(r.getTitle(), localeStr));
        out.setProfilePhotoUrl(r.getProfilePhotoMediaId());
        return out;
    }

    @QueryMapping
    public List<ExperienceItem> experiences(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        List<ExperienceItemResponse> list = experienceService.listPublished();
        List<ExperienceItem> out = new ArrayList<>(list.size());
        for (ExperienceItemResponse r : list) {
            ExperienceItem item = new ExperienceItem();
            item.setId(r.getItemId());
            item.setCompany(mapValue(r.getCompany(), localeStr));
            item.setRole(mapValue(r.getRole(), localeStr));
            item.setStartDate(r.getStartDate());
            item.setEndDate(r.getEndDate());
            item.setBulletPoints(mapListValue(r.getBulletPoints(), localeStr));
            item.setTechUsed(r.getTechUsed() != null ? r.getTechUsed() : List.of());
            item.setOrder(r.getOrder());
            out.add(item);
        }
        return out;
    }

    @QueryMapping
    public List<ProjectItem> projects(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        List<ProjectItemResponse> list = projectService.listPublishedVisible();
        List<ProjectItem> out = new ArrayList<>(list.size());
        for (ProjectItemResponse r : list) {
            ProjectItem item = new ProjectItem();
            item.setId(r.getItemId());
            item.setTitle(mapValue(r.getTitle(), localeStr));
            item.setDescription(mapValue(r.getDescription(), localeStr));
            item.setTechStack(r.getTechStack() != null ? r.getTechStack() : List.of());
            item.setLinks(toGraphQlLinks(r.getLinks()));
            item.setMediaUrls(r.getMediaIds() != null ? r.getMediaIds() : List.of());
            item.setVisible(r.isVisible());
            item.setOrder(r.getOrder());
            out.add(item);
        }
        return out;
    }

    @QueryMapping
    public List<EducationItem> education(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        List<EducationItemResponse> list = educationService.listPublished();
        List<EducationItem> out = new ArrayList<>(list.size());
        for (EducationItemResponse r : list) {
            EducationItem item = new EducationItem();
            item.setId(r.getItemId());
            item.setInstitution(r.getInstitution());
            item.setDegree(r.getDegree());
            item.setField(r.getField());
            item.setStartDate(r.getStartDate());
            item.setEndDate(r.getEndDate());
            item.setDetails(mapValue(r.getDetails(), localeStr));
            item.setOrder(r.getOrder());
            out.add(item);
        }
        return out;
    }

    @QueryMapping
    public List<SkillCategory> skills(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        List<SkillCategoryResponse> list = skillService.listPublished();
        List<SkillCategory> out = new ArrayList<>(list.size());
        for (SkillCategoryResponse r : list) {
            SkillCategory cat = new SkillCategory();
            cat.setId(r.getCategoryId());
            cat.setName(mapValue(r.getName(), localeStr));
            cat.setItems(toGraphQlSkillItems(r.getItems()));
            cat.setOrder(r.getOrder());
            out.add(cat);
        }
        return out;
    }

    @QueryMapping
    public List<CertificationItem> certifications(@Argument Locale locale) {
        String localeStr = resolveLocale(locale);
        List<CertificationItemResponse> list = certificationService.listPublished();
        List<CertificationItem> out = new ArrayList<>(list.size());
        for (CertificationItemResponse r : list) {
            CertificationItem item = new CertificationItem();
            item.setId(r.getItemId());
            item.setTitle(r.getTitle());
            item.setIssuer(r.getIssuer());
            item.setDate(r.getDate());
            item.setUrl(r.getUrl());
            item.setDescription(mapValue(r.getDescription(), localeStr));
            item.setOrder(r.getOrder());
            out.add(item);
        }
        return out;
    }

    @QueryMapping
    public List<SocialLinkItem> socialLinks() {
        List<SocialLinkItemResponse> list = socialLinkService.listPublished();
        List<SocialLinkItem> out = new ArrayList<>(list.size());
        for (SocialLinkItemResponse r : list) {
            SocialLinkItem item = new SocialLinkItem();
            item.setId(r.getItemId());
            item.setPlatform(r.getPlatform());
            item.setUrl(r.getUrl());
            item.setIcon(r.getIcon());
            item.setOrder(r.getOrder());
            out.add(item);
        }
        return out;
    }

    @QueryMapping
    public SiteSettings siteSettings() {
        SiteSettingsResponse r = settingsService.getOrCreate();
        SiteSettings out = new SiteSettings();
        out.setSupportedLocales(r.getSupportedLocales() != null
                ? List.of(r.getSupportedLocales())
                : List.of("en", "vi"));
        out.setDefaultLocale(r.getDefaultLocale() != null ? r.getDefaultLocale() : "en");
        return out;
    }

    private String resolveLocale(Locale locale) {
        if (locale != null) {
            return locale.name().toLowerCase();
        }
        SiteSettingsResponse settings = settingsService.getOrCreate();
        return settings.getDefaultLocale() != null ? settings.getDefaultLocale() : "en";
    }

    private static String mapValue(Map<String, String> map, String locale) {
        if (map == null) return null;
        return map.get(locale);
    }

    private static List<String> mapListValue(Map<String, List<String>> map, String locale) {
        if (map == null) return List.of();
        List<String> list = map.get(locale);
        return list != null ? list : List.of();
    }

    private static List<Link> toGraphQlLinks(List<com.tobyresume.backend.common.model.Link> links) {
        if (links == null) return List.of();
        return links.stream().map(l -> {
            Link gql = new Link();
            gql.setLabel(l.getLabel());
            gql.setUrl(l.getUrl());
            return gql;
        }).collect(Collectors.toList());
    }

    private static List<SkillItem> toGraphQlSkillItems(List<SkillItemResponse> items) {
        if (items == null) return List.of();
        return items.stream().map(r -> {
            SkillItem item = new SkillItem();
            item.setName(r.getName());
            item.setLevel(r.getLevel());
            return item;
        }).collect(Collectors.toList());
    }
}
