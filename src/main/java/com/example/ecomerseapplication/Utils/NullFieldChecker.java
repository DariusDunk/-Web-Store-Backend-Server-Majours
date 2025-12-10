package com.example.ecomerseapplication.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NullFieldChecker {

    /**
     * Returns true if the object has at least one null field.
     */
    public static boolean hasNullFields(Object obj) {
        return !getNullFields(obj).isEmpty();
    }

    /**
     * Returns a list of field names that are null.
     */
    public static List<String> getNullFields(Object obj) {
        List<String> nullFields = new ArrayList<>();

        if (obj == null) {
            nullFields.add("<<object is null>>");
            return nullFields;
        }

        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object value = field.get(obj);
                if (value == null) {
                    nullFields.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        return nullFields;
    }

    /**
     * Throws an exception if any field is null.
     */
    public static void validateNoNullFields(Object obj) {
        List<String> nulls = getNullFields(obj);

        if (!nulls.isEmpty()) {
            throw new IllegalStateException(
                    "Object " + obj.getClass().getSimpleName() +
                            " has null fields: " + String.join(", ", nulls)
            );
        }
    }
}
