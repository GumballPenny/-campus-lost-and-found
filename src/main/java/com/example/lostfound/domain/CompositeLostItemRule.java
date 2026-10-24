package com.example.lostfound.domain;

import java.util.List;

public final class CompositeLostItemRule implements LostItemRule {
    private final List<LostItemRule> rules;

    public CompositeLostItemRule(List<LostItemRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public void check(LostItemStatus from, LostItemStatus to) {
        for (LostItemRule rule : rules) {
            rule.check(from, to);
        }
    }
}
