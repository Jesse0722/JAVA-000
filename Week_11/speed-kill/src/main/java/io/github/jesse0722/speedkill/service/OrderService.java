package io.github.jesse0722.speedkill.service;

import io.github.jesse0722.speedkill.dao.OrderMapper;
import io.github.jesse0722.speedkill.module.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lijiajun
 * @date 2020/11/25 22:08
 */
@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;


    private final static int limit = 50;

    /***
     * 单线程处理大量订单
     * @param order
     */
    public void dealOrderSingleThread(final Order order) {
        long beginTime = System.currentTimeMillis();

        int total = orderMapper.getTotal(order);

        int offset = 0;
        do {
            List<Order> orders = orderMapper.findList(order, offset, limit);
            orders.forEach(item -> {
                item.setStatus(2);
                orderMapper.update(item);
            });
            offset = offset + limit;
        }while(offset < total);

        long endTime = System.currentTimeMillis();
        System.out.println("Order deal spend time:" + (endTime - beginTime));
    }


}
