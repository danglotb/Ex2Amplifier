package fr.inria.stamp.instrumentation.util;

import spoon.reflect.declaration.CtType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/11/17
 */
public class TypeUtils {

    private static final Map<String, String> supportedTypeAsString = new HashMap<>();

    static {
        supportedTypeAsString.put("java.lang.Integer", "Int");
        supportedTypeAsString.put("int", "Int");
        supportedTypeAsString.put("java.lang.String", "String");
    }

    public static boolean isSupported(CtType<?> type) {
        return type != null && supportedTypeAsString.containsKey(type.getQualifiedName());
    }

    public static String toAlloyType(CtType<?> type) {
        return supportedTypeAsString.get(type.getQualifiedName());
    }

}
