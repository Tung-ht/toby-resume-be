package com.tobyresume.backend.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.common.exception.PublishFailedException;
import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.certification.CertificationRepository;
import com.tobyresume.backend.content.certification.model.Certification;
import com.tobyresume.backend.content.education.EducationRepository;
import com.tobyresume.backend.content.education.model.Education;
import com.tobyresume.backend.content.experience.ExperienceRepository;
import com.tobyresume.backend.content.experience.model.WorkExperience;
import com.tobyresume.backend.content.hero.HeroRepository;
import com.tobyresume.backend.content.hero.model.Hero;
import com.tobyresume.backend.content.project.ProjectRepository;
import com.tobyresume.backend.content.project.model.Project;
import com.tobyresume.backend.content.skill.SkillRepository;
import com.tobyresume.backend.content.skill.model.Skill;
import com.tobyresume.backend.content.sociallink.SocialLinkRepository;
import com.tobyresume.backend.content.sociallink.model.SocialLink;
import com.tobyresume.backend.publish.model.VersionSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Publish pipeline: copy DRAFT → PUBLISHED for all sections, then save a version snapshot.
 * Missing DRAFT is treated as empty PUBLISHED.
 *
 * @see docs/ai/design/database-design.md §8.2, §8.3
 * @see docs/ai/design/api-design.md §5.2
 */
@Service
public class PublishService {

    private static final Logger log = LoggerFactory.getLogger(PublishService.class);

    private static final List<String> SECTION_ORDER = List.of(
            "hero", "experiences", "projects", "education", "skills", "certifications", "socialLinks"
    );

    private final HeroRepository heroRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final SkillRepository skillRepository;
    private final CertificationRepository certificationRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final PublishRepository publishRepository;
    private final ObjectMapper objectMapper;

    public PublishService(HeroRepository heroRepository,
                           ExperienceRepository experienceRepository,
                           ProjectRepository projectRepository,
                           EducationRepository educationRepository,
                           SkillRepository skillRepository,
                           CertificationRepository certificationRepository,
                           SocialLinkRepository socialLinkRepository,
                           PublishRepository publishRepository,
                           ObjectMapper objectMapper) {
        this.heroRepository = heroRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.educationRepository = educationRepository;
        this.skillRepository = skillRepository;
        this.certificationRepository = certificationRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.publishRepository = publishRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Copies all sections from DRAFT to PUBLISHED, then creates a version snapshot.
     * Returns metadata for the API response.
     */
    public PublishResult publish(String label) {
        List<String> sectionsPublished = new ArrayList<>();

        publishHero(sectionsPublished);
        publishExperiences(sectionsPublished);
        publishProjects(sectionsPublished);
        publishEducation(sectionsPublished);
        publishSkills(sectionsPublished);
        publishCertifications(sectionsPublished);
        publishSocialLinks(sectionsPublished);

        Map<String, Object> snapshotContent = buildSnapshotContent();
        Instant publishedAt = Instant.now();

        VersionSnapshot snapshot = new VersionSnapshot();
        snapshot.setContent(snapshotContent);
        snapshot.setLabel(label);
        snapshot.setPublishedAt(publishedAt);
        VersionSnapshot saved = publishRepository.save(snapshot);

        return new PublishResult(saved.getId(), publishedAt, sectionsPublished);
    }

    /**
     * Returns the latest publish time and total version count. Never published: lastPublishedAt null, versionCount 0.
     */
    public PublishStatus getStatus() {
        List<VersionSnapshot> latest = publishRepository.findTop1ByOrderByPublishedAtDesc();
        Instant lastPublishedAt = latest.isEmpty() ? null : latest.get(0).getPublishedAt();
        long versionCount = publishRepository.count();
        return new PublishStatus(lastPublishedAt, versionCount);
    }

    private void publishHero(List<String> sectionsPublished) {
        Hero draft = heroRepository.findByContentState(ContentState.DRAFT);
        Hero published = heroRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            heroRepository.delete(published);
        }
        Hero toSave = draft != null ? cloneForPublish(draft, Hero.class) : emptyHero();
        heroRepository.save(toSave);
        sectionsPublished.add("hero");
    }

    private void publishExperiences(List<String> sectionsPublished) {
        WorkExperience draft = experienceRepository.findByContentState(ContentState.DRAFT);
        WorkExperience published = experienceRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            experienceRepository.delete(published);
        }
        WorkExperience toSave = draft != null ? cloneForPublish(draft, WorkExperience.class) : emptyWorkExperience();
        experienceRepository.save(toSave);
        sectionsPublished.add("experiences");
    }

    private void publishProjects(List<String> sectionsPublished) {
        Project draft = projectRepository.findByContentState(ContentState.DRAFT);
        Project published = projectRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            projectRepository.delete(published);
        }
        Project toSave = draft != null ? cloneForPublish(draft, Project.class) : emptyProject();
        projectRepository.save(toSave);
        sectionsPublished.add("projects");
    }

    private void publishEducation(List<String> sectionsPublished) {
        Education draft = educationRepository.findByContentState(ContentState.DRAFT);
        Education published = educationRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            educationRepository.delete(published);
        }
        Education toSave = draft != null ? cloneForPublish(draft, Education.class) : emptyEducation();
        educationRepository.save(toSave);
        sectionsPublished.add("education");
    }

    private void publishSkills(List<String> sectionsPublished) {
        Skill draft = skillRepository.findByContentState(ContentState.DRAFT);
        Skill published = skillRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            skillRepository.delete(published);
        }
        Skill toSave = draft != null ? cloneForPublish(draft, Skill.class) : emptySkill();
        skillRepository.save(toSave);
        sectionsPublished.add("skills");
    }

    private void publishCertifications(List<String> sectionsPublished) {
        Certification draft = certificationRepository.findByContentState(ContentState.DRAFT);
        Certification published = certificationRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            certificationRepository.delete(published);
        }
        Certification toSave = draft != null ? cloneForPublish(draft, Certification.class) : emptyCertification();
        certificationRepository.save(toSave);
        sectionsPublished.add("certifications");
    }

    private void publishSocialLinks(List<String> sectionsPublished) {
        SocialLink draft = socialLinkRepository.findByContentState(ContentState.DRAFT);
        SocialLink published = socialLinkRepository.findByContentState(ContentState.PUBLISHED);
        if (published != null) {
            socialLinkRepository.delete(published);
        }
        SocialLink toSave = draft != null ? cloneForPublish(draft, SocialLink.class) : emptySocialLink();
        socialLinkRepository.save(toSave);
        sectionsPublished.add("socialLinks");
    }

    @SuppressWarnings("unchecked")
    private <T> T cloneForPublish(T draft, Class<T> clazz) {
        try {
            T clone = objectMapper.readValue(objectMapper.writeValueAsString(draft), clazz);
            BaseDocument base = (BaseDocument) clone;
            base.setId(null);
            Method setContentState = clazz.getMethod("setContentState", ContentState.class);
            setContentState.invoke(clone, ContentState.PUBLISHED);
            return clone;
        } catch (Exception e) {
            log.error("Failed to clone content for publish", e);
            throw new PublishFailedException("Publish failed: could not copy content", e);
        }
    }

    private static Hero emptyHero() {
        Hero h = new Hero();
        h.setContentState(ContentState.PUBLISHED);
        return h;
    }

    private static WorkExperience emptyWorkExperience() {
        WorkExperience w = new WorkExperience();
        w.setContentState(ContentState.PUBLISHED);
        w.setItems(Collections.emptyList());
        return w;
    }

    private static Project emptyProject() {
        Project p = new Project();
        p.setContentState(ContentState.PUBLISHED);
        p.setItems(Collections.emptyList());
        return p;
    }

    private static Education emptyEducation() {
        Education e = new Education();
        e.setContentState(ContentState.PUBLISHED);
        e.setItems(Collections.emptyList());
        return e;
    }

    private static Skill emptySkill() {
        Skill s = new Skill();
        s.setContentState(ContentState.PUBLISHED);
        s.setCategories(Collections.emptyList());
        return s;
    }

    private static Certification emptyCertification() {
        Certification c = new Certification();
        c.setContentState(ContentState.PUBLISHED);
        c.setItems(Collections.emptyList());
        return c;
    }

    private static SocialLink emptySocialLink() {
        SocialLink s = new SocialLink();
        s.setContentState(ContentState.PUBLISHED);
        s.setItems(Collections.emptyList());
        return s;
    }

    private Map<String, Object> buildSnapshotContent() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("hero", sectionToSnapshotMap(heroRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("experiences", sectionToSnapshotMap(experienceRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("projects", sectionToSnapshotMap(projectRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("education", sectionToSnapshotMap(educationRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("skills", sectionToSnapshotMap(skillRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("certifications", sectionToSnapshotMap(certificationRepository.findByContentState(ContentState.PUBLISHED)));
        content.put("socialLinks", sectionToSnapshotMap(socialLinkRepository.findByContentState(ContentState.PUBLISHED)));
        return content;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sectionToSnapshotMap(Object entity) {
        if (entity == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = objectMapper.convertValue(entity, Map.class);
        map.remove("id");
        map.remove("contentState");
        map.remove("createdAt");
        map.remove("updatedAt");
        return map;
    }

    public record PublishResult(String versionId, Instant publishedAt, List<String> sectionsPublished) {}

    public record PublishStatus(Instant lastPublishedAt, long versionCount) {}
}
