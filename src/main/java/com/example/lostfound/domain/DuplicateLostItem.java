package com.example.lostfound.domain;

public class DuplicateLostItem extends RuntimeException {
    public DuplicateLostItem(String businessKey) {
        super("duplicate lost item: " + businessKey);
    }
}
