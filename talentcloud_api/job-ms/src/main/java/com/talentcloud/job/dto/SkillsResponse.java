package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillsResponse {
    private Long id;
    private Set<String> programmingLanguages;  // ✅ Changed from List<String> to Set<String>
    private Set<String> softSkills;            // ✅ Changed from List<String> to Set<String>
    private Set<String> technicalSkills;       // ✅ Changed from List<String> to Set<String>
    private Set<String> toolsAndTechnologies;  // ✅ Changed from List<String> to Set<String>
    private Set<String> customSkills;          // ✅ Changed from List<String> to Set<String>
}