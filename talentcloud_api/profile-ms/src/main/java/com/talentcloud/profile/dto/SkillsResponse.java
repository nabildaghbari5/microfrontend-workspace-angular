package com.talentcloud.profile.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
public class SkillsResponse {
    private Long id;
    private Set<String> programmingLanguages ;
    private Set<String> softSkills;
    private Set<String> technicalSkills ;
    private Set<String> toolsAndTechnologies;
    private Set<String> customSkills;

}
