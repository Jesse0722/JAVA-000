package io.github.jesse0722.springDemo;

import io.github.jesse0722.springDemo.jesse0722.starterDemo.School;
import io.github.jesse0722.springDemo.jesse0722.starterDemo.SchoolAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Lijiajun
 * @date 2020/11/18 17:17
 */
@SpringBootTest(classes = SchoolAutoConfiguration.class)
@RunWith(SpringRunner.class)
public class SchoolAutoConfigurationTest {

    @Autowired
    private School school;
    @Test
    public void school(){
        school.ding();
    }
}
