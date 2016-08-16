package com.montrosesoftware.repositories;

public class ConditionsAlreadyUsedException extends Exception {

    public ConditionsAlreadyUsedException() {}

    public ConditionsAlreadyUsedException(String message) {
        super(message);
    }
}
