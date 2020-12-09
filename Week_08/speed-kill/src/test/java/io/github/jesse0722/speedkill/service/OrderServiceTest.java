package io.github.jesse0722.speedkill.service;

import io.github.jesse0722.speedkill.dao.OrderMapper;
import io.github.jesse0722.speedkill.module.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lijiajun
 * @date 2020/12/02 22:31
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class OrderServiceTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void insert() {
        Order order = new Order("xasdasdaqsfafa", 12345567,123123, 1);
        orderMapper.insert(order);
    }

    @Test
    void delete() {
        orderMapper.delete(23956);    }

    @Test
    void select() {
        System.out.println(orderMapper.get(23958,123456));
    }


    @Test
    void getByUserId() {
        System.out.println(orderMapper.getByUserId(123456L));
    }

}
