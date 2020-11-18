package io.github.jesse0722.springDemo.jesse0722.starterDemo;

import lombok.Data;

import java.util.List;

@Data
public class School implements ISchool {

    private List<Klass> classList;

    public School(List<Klass> classList) {
        this.classList = classList;
    }

    @Override
    public void ding(){

        System.out.println(classList);

    }

}
