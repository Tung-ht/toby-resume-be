package com.tobyresume.backend.preview;

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
import com.tobyresume.backend.content.sociallink.SocialLinkService;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemResponse;
import com.tobyresume.backend.preview.dto.PreviewResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates all DRAFT sections for admin preview. Optional locale filter returns
 * single-locale values per field (same shape as GraphQL for that locale).
 *
 * @see docs/ai/design/api-design.md §5.1
 */
@Service
public class PreviewService {

    private static final List<String> SUPPORTED_LOCALES = List.of("en", "vi");

    private final HeroService heroService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final CertificationService certificationService;
    private final SocialLinkService socialLinkService;
    private final SkillService skillService;

    public PreviewService(HeroService heroService,
                          ExperienceService experienceService,
                          ProjectService projectService,
                          EducationService educationService,
                          CertificationService certificationService,
                          SocialLinkService socialLinkService,
                          SkillService skillService) {
        this.heroService = heroService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.certificationService = certificationService;
        this.socialLinkService = socialLinkService;
        this.skillService = skillService;
    }

    /**
     * Build full draft payload. When locale is "en" or "vi", localized fields are
     * reduced to that locale's value (String). Otherwise returns full multi-locale shape.
     */
    public PreviewResponse getPreview(String locale) {
        PreviewResponse response = new PreviewResponse();

        HeroResponse hero = heroService.getDraft();
        List<ExperienceItemResponse> experiences = experienceService.list();
        List<ProjectItemResponse> projects = projectService.list();
        List<EducationItemResponse> education = educationService.list();
        List<SkillCategoryResponse> skills = skillService.list();
        List<CertificationItemResponse> certifications = certificationService.list();
        List<SocialLinkItemResponse> socialLinks = socialLinkService.list();

        if (isSingleLocale(locale)) {
            response.setHero(toHeroLocale(hero, locale));
            response.setExperiences(toExperiencesLocale(experiences, locale));
            response.setProjects(toProjectsLocale(projects, locale));
            response.setEducation(toEducationLocale(education, locale));
            response.setSkills(toSkillsLocale(skills, locale));
            response.setCertifications(toCertificationsLocale(certifications, locale));
            response.setSocialLinks(socialLinks);
        } else {
            response.setHero(hero);
            response.setExperiences(experiences);
            response.setProjects(projects);
            response.setEducation(education);
            response.setSkills(skills);
            response.setCertifications(certifications);
            response.setSocialLinks(socialLinks);
        }

        return response;
    }

    private static boolean isSingleLocale(String locale) {
        return locale != null && !locale.isBlank() && SUPPORTED_LOCALES.contains(locale.trim().toLowerCase());
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

    private Object toHeroLocale(HeroResponse hero, String locale) {
        if (hero == null) return null;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tagline", mapValue(hero.getTagline(), locale));
        out.put("bio", mapValue(hero.getBio(), locale));
        out.put("fullName", mapValue(hero.getFullName(), locale));
        out.put("title", mapValue(hero.getTitle(), locale));
        out.put("profilePhotoMediaId", hero.getProfilePhotoMediaId());
        return out;
    }

    private List<Map<String, Object>> toExperiencesLocale(List<ExperienceItemResponse> list, String locale) {
        if (list == null) return List.of();
        return list.stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getItemId());
                    m.put("company", mapValue(item.getCompany(), locale));
                    m.put("role", mapValue(item.getRole(), locale));
                    m.put("startDate", item.getStartDate());
                    m.put("endDate", item.getEndDate());
                    m.put("bulletPoints", mapListValue(item.getBulletPoints(), locale));
                    m.put("techUsed", item.getTechUsed() != null ? item.getTechUsed() : List.of());
                    m.put("order", item.getOrder());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toProjectsLocale(List<ProjectItemResponse> list, String locale) {
        if (list == null) return List.of();
        return list.stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getItemId());
                    m.put("title", mapValue(item.getTitle(), locale));
                    m.put("description", mapValue(item.getDescription(), locale));
                    m.put("techStack", item.getTechStack() != null ? item.getTechStack() : List.of());
                    m.put("links", item.getLinks());
                    m.put("mediaIds", item.getMediaIds() != null ? item.getMediaIds() : List.of());
                    m.put("visible", item.isVisible());
                    m.put("order", item.getOrder());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toEducationLocale(List<EducationItemResponse> list, String locale) {
        if (list == null) return List.of();
        return list.stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getItemId());
                    m.put("institution", item.getInstitution());
                    m.put("degree", item.getDegree());
                    m.put("field", item.getField());
                    m.put("startDate", item.getStartDate());
                    m.put("endDate", item.getEndDate());
                    m.put("details", mapValue(item.getDetails(), locale));
                    m.put("order", item.getOrder());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toSkillsLocale(List<SkillCategoryResponse> list, String locale) {
        if (list == null) return List.of();
        return list.stream()
                .map(cat -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("categoryId", cat.getCategoryId());
                    m.put("name", mapValue(cat.getName(), locale));
                    m.put("items", cat.getItems() != null ? cat.getItems() : List.of());
                    m.put("order", cat.getOrder());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toCertificationsLocale(List<CertificationItemResponse> list, String locale) {
        if (list == null) return List.of();
        return list.stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getItemId());
                    m.put("title", item.getTitle());
                    m.put("issuer", item.getIssuer());
                    m.put("date", item.getDate());
                    m.put("url", item.getUrl());
                    m.put("description", mapValue(item.getDescription(), locale));
                    m.put("order", item.getOrder());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
