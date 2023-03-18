package com.ecore.roles.exception;

import static java.lang.String.format;

import com.ecore.roles.model.Membership;

public class InvalidArgumentException extends RuntimeException {

    public <T> InvalidArgumentException(Class<T> resource) {
        super(format("Invalid '%s' object", resource.getSimpleName()));
    }

    public InvalidArgumentException(Class<Membership> resource, String additionalInfo) {
        super(format("Invalid '%s' object. " + additionalInfo, resource.getSimpleName()));
    }
}
