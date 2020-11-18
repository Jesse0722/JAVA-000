package io.github.jesse0722.springDemo.jesse0722.starterDemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Lijiajun
 * @date 2020/11/18 14:56
 */

@ConfigurationProperties(prefix = "school")
@Data
public class SchoolProperties {
    public List<Integer> studentIds;

    public List<String> studentNames;

    public List<Integer> klassIds;

    public List<Integer> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Integer> studentIds) {
        this.studentIds = studentIds;
    }

    public List<String> getStudentNames() {
        return studentNames;
    }

    public void setStudentNames(List<String> studentNames) {
        this.studentNames = studentNames;
    }

    public List<Integer> getKlassIds() {
        return klassIds;
    }

    public void setKlassIds(List<Integer> klassIds) {
        this.klassIds = klassIds;
    }
}
