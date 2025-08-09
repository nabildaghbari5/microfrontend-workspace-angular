package com.talentcloud.profile.strategy;

import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.VisibilitySettings;

public class PrivateProfileStrategy implements ProfileStrategy {
    @Override
    public void applyStrategy(Candidate candidate) {
        candidate.setVisibilitySettings(VisibilitySettings.PRIVATE);
    }
}

