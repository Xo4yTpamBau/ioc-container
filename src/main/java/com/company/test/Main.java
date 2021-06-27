package com.company.test;

import com.company.Factory;
import com.company.annotation.Component;
import org.reflections.Reflections;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Factory factory = new Factory(RootConfiguration.class);
        System.out.println(factory.getBeans());
        factory.close();
    }
}
