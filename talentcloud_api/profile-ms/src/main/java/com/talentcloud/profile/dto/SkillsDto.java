package com.talentcloud.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillsDto {
    private Set<String> programmingLanguages = new HashSet<>();
    private Set<String> softSkills = new HashSet<>();
    private Set<String> technicalSkills = new HashSet<>();
    private Set<String> toolsAndTechnologies = new HashSet<>();
    private Set<String> customSkills = new HashSet<>();
}