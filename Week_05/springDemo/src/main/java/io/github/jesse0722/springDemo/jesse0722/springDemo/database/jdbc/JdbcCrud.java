package io.github.jesse0722.springDemo.jesse0722.springDemo.database.jdbc;

import java.util.List;

/**
 * @author Lijiajun
 * @date 2020/11/18 18:20
 */
public interface JdbcCrud {
    boolean insert(String sql);

    boolean delete(String sql);

    void update(String sql);

    List<User> query(String sql);

    int insertBatch(List<User> users);
}
