package io.github.jesse0722.springDemo.jesse0722.starterDemo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Klass {

    private int id;

    private List<Student> students;

    public void dong(){
        System.out.println(this.getStudents());
    }

}

