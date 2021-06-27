package com.company.test;

import com.company.annotation.*;

import java.util.Arrays;

@Configuration
@ComponentScan(basePackage = "com.company.test")
public class RootConfiguration {


    @Bean
//    @Lazy
    public Cat cat2(@Value("Test Cat2") String name){
        return new Cat();
    }
//
//    @Bean
//    public Cat cat2(@Value("Test Cat2") String name){
//        return new Cat(name);
//    }
//
//    @Bean(initMethod = {"myInit", "init2"}, destroyMethod = {"destroy", "destroy2"})
//    public User user(@Value("Test User") String name, @Qualifier("cat") Cat cat){
//        return new User();
//    }

//    @Bean
//    public User user() {
//        return new User();
//    }
}
