package com.talentcloud.profile.strategy;

import com.talentcloud.profile.model.Candidate;

public interface ProfileStrategy {
    void applyStrategy(Candidate candidate);
}
