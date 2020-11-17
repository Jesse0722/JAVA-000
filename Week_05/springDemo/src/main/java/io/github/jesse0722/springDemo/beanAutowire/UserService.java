package io.github.jesse0722.springDemo.beanAutowire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;

/**
 * @author Lijiajun
 * @date 2020/11/16 15:53
 */
public class UserService {

    @Autowired
    @Qualifier(value = "user2")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
