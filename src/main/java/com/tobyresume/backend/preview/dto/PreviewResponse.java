package com.tobyresume.backend.preview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Root payload for GET /api/v1/preview. Aggregates all DRAFT sections.
 * When no locale: hero and list items use full multi-locale shapes (e.g. Map for tagline).
 * When ?locale=en|vi: each localized field is the single-locale value (String).
 *
 * @see docs/ai/design/api-design.md §5.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviewResponse {

    private Object hero;
    private Object experiences;
    private Object projects;
    private Object education;
    private Object skills;
    private Object certifications;
    private Object socialLinks;

    public Object getHero() {
        return hero;
    }

    public void setHero(Object hero) {
        this.hero = hero;
    }

    public Object getExperiences() {
        return experiences;
    }

    public void setExperiences(Object experiences) {
        this.experiences = experiences;
    }

    public Object getProjects() {
        return projects;
    }

    public void setProjects(Object projects) {
        this.projects = projects;
    }

    public Object getEducation() {
        return education;
    }

    public void setEducation(Object education) {
        this.education = education;
    }

    public Object getSkills() {
        return skills;
    }

    public void setSkills(Object skills) {
        this.skills = skills;
    }

    public Object getCertifications() {
        return certifications;
    }

    public void setCertifications(Object certifications) {
        this.certifications = certifications;
    }

    public Object getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(Object socialLinks) {
        this.socialLinks = socialLinks;
    }
}
