package com.company.test;

import com.company.annotation.Component;
import com.company.annotation.PostConstruct;
import com.company.annotation.PreDestroy;
import com.company.annotation.Value;

@Component
public class Cat implements Animal{

    @Value("Test Cat")
    private String name;

    @PostConstruct
    public void init(){
        System.out.println("init22");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("destroy");
    }

    @Override
    public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                '}';
    }
}
