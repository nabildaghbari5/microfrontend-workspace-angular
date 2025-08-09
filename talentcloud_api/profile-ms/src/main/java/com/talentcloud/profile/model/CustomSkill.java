package com.talentcloud.profile.model;

public enum CustomSkill {
    LANGUAGE_FRENCH("French"),
    LANGUAGE_SPANISH("Spanish"),
    LANGUAGE_GERMAN("German"),
    LANGUAGE_CHINESE("Chinese"),
    CERTIFICATION_PMP("PMP Certification"),
    CERTIFICATION_SCRUM("Scrum Master"),
    CERTIFICATION_AWS("AWS Certified"),
    INDUSTRY_HEALTHCARE("Healthcare Knowledge"),
    INDUSTRY_FINANCE("Finance Knowledge"),
    INDUSTRY_ECOMMERCE("E-commerce Experience"),
    PROJECT_AGILE("Agile Management"),
    PROJECT_WATERFALL("Waterfall Methodology"),
    ANALYTICAL_FORECASTING("Forecasting"),
    ANALYTICAL_REPORTING("Business Reporting"),
    PRESENTATION_PUBLIC_SPEAKING("Public Speaking"),
    WRITING_TECHNICAL("Technical Writing"),
    WRITING_CONTENT("Content Creation");

    private final String skill;

    CustomSkill(String skill) {
        this.skill = skill;
    }

    public String getSkill() {
        return skill;
    }
}