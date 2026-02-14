package com.tobyresume.backend.graphql.model;

import java.util.List;

/**
 * GraphQL type SiteSettings (supportedLocales, defaultLocale only).
 */
public class SiteSettings {

    private List<String> supportedLocales;
    private String defaultLocale;

    public List<String> getSupportedLocales() { return supportedLocales; }
    public void setSupportedLocales(List<String> supportedLocales) { this.supportedLocales = supportedLocales; }
    public String getDefaultLocale() { return defaultLocale; }
    public void setDefaultLocale(String defaultLocale) { this.defaultLocale = defaultLocale; }
}
