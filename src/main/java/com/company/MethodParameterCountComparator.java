package com.company;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodParameterCountComparator implements Comparator<Method> {
    @Override
    public int compare(Method o1, Method o2) {
        return Integer.compare(o1.getParameterCount(), o2.getParameterCount());
    }
}
