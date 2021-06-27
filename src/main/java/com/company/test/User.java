package com.company.test;

import com.company.annotation.Autowired;
import com.company.annotation.Component;
import com.company.annotation.Qualifier;
import com.company.annotation.Value;

//constructor
//setter
//field

@Component
public class User {

    @Autowired
//    @Qualifier("cat2")
    private Cat cat;

    @Value("Test User")
    private String name;

    public void myInit(){
        System.out.println("Init");
    }

    public void init2(){
        System.out.println("Init2");
    }

    public void destroy(){
        System.out.println("Destroy");
    }

    public void destroy2(){
        System.out.println("Destroy2");
    }

//    @Autowired
//    public void setCat(Animal cat) {
//        this.cat = cat;
//    }

    @Override
    public String toString() {
        return "User{" +
                "cat=" + cat +
                ", name='" + name + '\'' +
                '}';
    }
}
