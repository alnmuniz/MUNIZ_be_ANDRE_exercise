package com.ecore.roles.exception;

import java.util.UUID;

import static java.lang.String.format;

import static com.ecore.roles.MessageUtil.S_NOT_FOUND;
import static com.ecore.roles.MessageUtil.S_S_NOT_FOUND;

public class ResourceNotFoundException extends RuntimeException {

    public <T> ResourceNotFoundException(Class<T> resource, UUID id) {
        super(format(S_S_NOT_FOUND, resource.getSimpleName(), id));
    }

    public <T> ResourceNotFoundException(Class<T> resource) {
        super(format(S_NOT_FOUND, resource.getSimpleName()));
    }
}
