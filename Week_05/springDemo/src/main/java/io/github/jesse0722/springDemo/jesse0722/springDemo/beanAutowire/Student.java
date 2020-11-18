package io.github.jesse0722.springDemo.jesse0722.springDemo.beanAutowire;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Student implements BeanNameAware, Serializable {

    private int id;
    private String name;

    @Override
    public void setBeanName(String name) {
        System.out.println(name);
    }
}
