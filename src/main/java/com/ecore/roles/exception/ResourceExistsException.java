package com.ecore.roles.exception;

import static java.lang.String.format;
import static com.ecore.roles.MessageUtil.S_ALREADY_EXISTS;

public class ResourceExistsException extends RuntimeException {

    public <T> ResourceExistsException(Class<T> resource) {
        super(format(S_ALREADY_EXISTS, resource.getSimpleName()));
    }
}
