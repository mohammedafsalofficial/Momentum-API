package com.momentum.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    List<String> valueList = null;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        valueList = new ArrayList<>();
        Class<? extends Enum<?>> enumClass =  constraintAnnotation.enumClazz();

        Enum<?>[] enumValArr = enumClass.getEnumConstants();

        for (Enum<?> enumVal : enumValArr) {
            valueList.add(enumVal.name().toUpperCase());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return valueList.contains(value.toUpperCase());
    }
}
