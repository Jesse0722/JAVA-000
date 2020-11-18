package io.github.jesse0722.springDemo.jesse0722.starterDemo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Student implements Serializable {

    private int id;
    private String name;

}
