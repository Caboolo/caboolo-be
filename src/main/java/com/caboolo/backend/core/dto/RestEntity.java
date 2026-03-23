package com.caboolo.backend.core.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RestEntity<T> extends ResponseEntity<RestResponse<T>> {

    public RestEntity(RestResponse<T> body, HttpStatus status) {
        super(body, status);
    }
}
