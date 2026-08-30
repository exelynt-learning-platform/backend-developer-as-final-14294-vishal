package com.resourcebooking.common.response.constant;

public final class ErrorCode {

    private ErrorCode() {
    }

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}