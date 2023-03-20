package com.ecore.roles;

public class MessageUtil {

    private MessageUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final String INVALID_S_OBJECT = "Invalid '%s' object";
    public static final String S_ALREADY_EXISTS = "%s already exists";
    public static final String S_NOT_FOUND = "%s not found";
    public static final String S_S_NOT_FOUND = "%s %s not found";
    public static final String PROV_USR_DOESNT_BELONG_PROV_TEAM =
            "The provided user doesn't belong to the provided team.";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String NOT_FOUND = "Not Found";
}
