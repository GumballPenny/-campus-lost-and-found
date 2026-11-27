package com.example.lostfound.domain;
public class InvalidLostItemTransition extends RuntimeException {
    public InvalidLostItemTransition() { super("lost item cannot move to this status"); }
}
