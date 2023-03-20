package com.ecore.roles.exception;

import static com.ecore.roles.utils.MessageUtil.S_ALREADY_EXISTS;
import static java.lang.String.format;

public class ResourceExistsException extends RuntimeException {

    public <T> ResourceExistsException(Class<T> resource) {
        super(format(S_ALREADY_EXISTS, resource.getSimpleName()));
    }
}
