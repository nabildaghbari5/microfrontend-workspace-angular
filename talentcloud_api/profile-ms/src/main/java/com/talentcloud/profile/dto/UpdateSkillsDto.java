package com.talentcloud.profile.dto;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class UpdateSkillsDto {
    private Set<String> programmingLanguages = new HashSet<>();
    private Set<String> softSkills = new HashSet<>();
    private Set<String> technicalSkills = new HashSet<>();
    private Set<String> toolsAndTechnologies = new HashSet<>();
    private Set<String> customSkills = new HashSet<>();
}