package io.github.jesse0722.springDemo.jesse0722.springDemo.beanAutowire;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lijiajun
 * @date 2020/11/16 15:28
 */
@Configuration
public class BeansConfiguration {
    @Bean
    public User user() {
        return new User("Lee", 18);
    }
}
