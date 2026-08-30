package com.resourcebooking.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ApiError {

    private String code;

    private Map<String, String> details;
}
