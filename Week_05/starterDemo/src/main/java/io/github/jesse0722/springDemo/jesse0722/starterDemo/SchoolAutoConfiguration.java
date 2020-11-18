package io.github.jesse0722.springDemo.jesse0722.starterDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lijiajun
 * @date 2020/11/17 18:42
 */
@EnableConfigurationProperties(SchoolProperties.class)
@Configuration
@ConditionalOnClass(School.class)
@ConditionalOnProperty(prefix = "school", value = "enable", havingValue = "true")
@PropertySource("classpath:application.properties")
public class SchoolAutoConfiguration {

    @Autowired
    private SchoolProperties schoolProperties;

    @Bean
    public School school(){
        Student student1 = new Student(schoolProperties.studentIds.get(0), schoolProperties.studentNames.get(0));
        Student student2 = new Student(schoolProperties.studentIds.get(1), schoolProperties.studentNames.get(1));
        Student student3 = new Student(schoolProperties.studentIds.get(2), schoolProperties.studentNames.get(2));


        Klass klass1 = new Klass(schoolProperties.klassIds.get(0), Arrays.asList(student1,student3));
        Klass klass2 = new Klass(schoolProperties.klassIds.get(1), Arrays.asList(student2));

        List<Klass> classList = Arrays.asList(klass1, klass2);
        School school = new School(classList);
        return school;
    }
}
