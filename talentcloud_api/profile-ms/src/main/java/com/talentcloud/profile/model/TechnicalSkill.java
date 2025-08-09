package com.talentcloud.profile.model;

public enum TechnicalSkill {
    REST_API("REST API Development"),
    GRAPHQL("GraphQL"),
    DATABASE_DESIGN("Database Design"),
    MACHINE_LEARNING("Machine Learning"),
    DATA_ANALYSIS("Data Analysis"),
    FRONTEND_DEVELOPMENT("Frontend Development"),
    BACKEND_DEVELOPMENT("Backend Development"),
    FULLSTACK_DEVELOPMENT("Fullstack Development"),
    CLOUD_COMPUTING("Cloud Computing"),
    NETWORK_SECURITY("Network Security"),
    CI_CD("Continuous Integration/Delivery"),
    CONTAINERIZATION("Containerization"),
    OOP("Object-Oriented Programming"),
    FUNCTIONAL_PROGRAMMING("Functional Programming"),
    UNIT_TESTING("Unit Testing"),
    AUTOMATED_DEPLOYMENT("Automated Deployment"),
    CODE_REVIEW("Code Review"),
    DEVSECOPS("DevSecOps"),
    ALGORITHMS("Algorithms and Data Structures"),
    PERFORMANCE_OPTIMIZATION("Performance Optimization");

    private final String skill;

    TechnicalSkill(String skill) {
        this.skill = skill;
    }

    public String getSkill() {
        return skill;
    }
}