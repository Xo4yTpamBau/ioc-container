package com.company;

import java.lang.reflect.Constructor;
import java.util.Comparator;

public class ConstructorParameterCountComparator implements Comparator<Constructor<?>> {
    @Override
    public int compare(Constructor<?> o1, Constructor<?> o2) {
        return Integer.compare(o1.getParameterCount(), o2.getParameterCount());
    }
}
