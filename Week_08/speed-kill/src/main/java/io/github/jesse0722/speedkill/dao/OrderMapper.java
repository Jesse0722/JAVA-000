package io.github.jesse0722.speedkill.dao;

import io.github.jesse0722.speedkill.module.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Lijiajun
 * @date 2020/11/25 15:15
 */
@Mapper
public interface OrderMapper {

    void insert(Order order);

    void update(Order order);

    int getTotal(Order order);

    List<Order> findList(Order order);

    void insertBatch(List<Order> list);

    @Delete("delete from `t_order` where id = #{id}")
    void delete(long id);

    @Select("select * from t_order where id = #{id} and user_id = #{userId}")
    Order get(long id, long userId);

    @Select("select * from t_order where user_id = #{userId}")
    List<Order> getByUserId(long userId);
}
