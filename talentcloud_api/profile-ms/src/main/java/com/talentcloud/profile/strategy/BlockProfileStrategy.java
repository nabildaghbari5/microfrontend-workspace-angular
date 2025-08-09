package com.talentcloud.profile.strategy;

import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.VisibilitySettings;

public class BlockProfileStrategy implements ProfileStrategy {
    // In BlockProfileStrategy
    @Override
    public void applyStrategy(Candidate candidate) {
        candidate.setVisibilitySettings(VisibilitySettings.RESTRICTED);
        candidate.setBlocked(true);  // Can keep it for additional blocking logic
    }
}
