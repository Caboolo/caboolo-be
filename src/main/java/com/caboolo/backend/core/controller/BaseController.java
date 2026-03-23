package com.caboolo.backend.core.controller;

import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.core.dto.RestResponse;
import org.springframework.http.HttpStatus;

public abstract class BaseController {

    protected <T> RestEntity<T> successResponse(T data, String message) {
        RestResponse<T> response = new RestResponse<>(true, message, data);
        return new RestEntity<>(response, HttpStatus.OK);
    }

    protected <T> RestEntity<T> successResponse(T data) {
        return successResponse(data, "Success");
    }

    protected <T> RestEntity<T> successResponse(String message) {
        return successResponse(null, message);
    }

    protected <T> RestEntity<T> errorResponse(String message, HttpStatus status) {
        RestResponse<T> response = new RestResponse<>(false, message, null);
        return new RestEntity<>(response, status);
    }

    protected <T> RestEntity<T> errorResponse(String message) {
        return errorResponse(message, HttpStatus.BAD_REQUEST);
    }
}
