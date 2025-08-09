package com.talentcloud.profile.model;

public enum SoftSkill {
    COMMUNICATION("Communication"),
    TEAMWORK("Teamwork"),
    PROBLEM_SOLVING("Problem Solving"),
    LEADERSHIP("Leadership"),
    TIME_MANAGEMENT("Time Management"),
    ADAPTABILITY("Adaptability"),
    CREATIVITY("Creativity"),
    CRITICAL_THINKING("Critical Thinking"),
    CONFLICT_RESOLUTION("Conflict Resolution"),
    DECISION_MAKING("Decision Making"),
    NEGOTIATION("Negotiation"),
    PRESENTATION("Presentation Skills"),
    EMPATHY("Empathy"),
    COLLABORATION("Collaboration"),
    ORGANIZATION("Organizational Skills"),
    STRESS_MANAGEMENT("Stress Management"),
    ATTENTION_TO_DETAIL("Attention to Detail"),
    INNOVATION("Innovation"),
    WORK_ETHIC("Work Ethic"),
    CUSTOMER_ORIENTATION("Customer Orientation");

    private final String skill;

    SoftSkill(String skill) {
        this.skill = skill;
    }

    public String getSkill() {
        return skill;
    }
}