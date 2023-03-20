package com.ecore.roles.exception;

import java.util.UUID;

import static com.ecore.roles.utils.MessageUtil.S_NOT_FOUND;
import static com.ecore.roles.utils.MessageUtil.S_S_NOT_FOUND;
import static java.lang.String.format;

public class ResourceNotFoundException extends RuntimeException {

    public <T> ResourceNotFoundException(Class<T> resource, UUID id) {
        super(format(S_S_NOT_FOUND, resource.getSimpleName(), id));
    }

    public <T> ResourceNotFoundException(Class<T> resource) {
        super(format(S_NOT_FOUND, resource.getSimpleName()));
    }
}
