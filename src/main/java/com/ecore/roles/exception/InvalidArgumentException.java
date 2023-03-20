package com.ecore.roles.exception;

import static com.ecore.roles.utils.MessageUtil.INVALID_S_OBJECT;
import static java.lang.String.format;

public class InvalidArgumentException extends RuntimeException {

    public <T> InvalidArgumentException(Class<T> resource) {
        super(format(INVALID_S_OBJECT, resource.getSimpleName()));
    }

    public <T> InvalidArgumentException(Class<T> resource, String additionalInfo) {
        super(new StringBuilder()
                .append(format(INVALID_S_OBJECT, resource.getSimpleName()))
                .append(". ")
                .append(additionalInfo)
                .toString());
    }
}
