package com.momentum.api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FieldError {

    private String field;

    private String message;
}
