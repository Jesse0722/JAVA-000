package io.github.jesse0722.springDemo.jesse0722.springDemo.database.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lijiajun
 * @date 2020/11/18 18:24
 */
public class JdbcTemplateService implements JdbcCrud {

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;

    private void createConnection()  {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println();
        } catch (ClassNotFoundException e) {
            System.out.println("Can't find mysql jdbc driver");
            e.printStackTrace();
            return;
        }


        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "admin","123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean insert(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
//            for (int i = 0;i < objectList.size(); i++) {
//                statement.setObject(i+1, objectList.get(i));
//            }
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String sql) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void update(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            System.out.println("update success");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public List<User> query(String sql) {

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                users.add(new User(id, name, age));
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insertBatch(List<User> users) {
        PreparedStatement statement = null;
        try {

            statement = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?)");
            for (User user : users) {
                statement.setInt(1, user.getId());
                statement.setString(2, user.getName());
                statement.setInt(3, user.getAge());
                statement.addBatch();
                statement.clearParameters();
            }
            int[] ints = statement.executeBatch();
            return ints.length;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static void main(String[] args) {
        JdbcTemplateService jdbcTemplateService = new JdbcTemplateService();
        jdbcTemplateService.createConnection();

        String insertSql = "insert into users (id, name, age) values (10003, \"lee\", 18)";
        System.out.println(jdbcTemplateService.insert(insertSql));

        String deleteSql = "delete from users where id = 10003";
        System.out.println(jdbcTemplateService.delete(deleteSql));

        String updateSql = "update users set name=\"Hello\" where id = 10001";
        jdbcTemplateService.update(updateSql);

        String querySql = "select * from users where id>0";
        System.out.println(jdbcTemplateService.query(querySql));

        //批量插入
        System.out.println(jdbcTemplateService.insertBatch(Arrays.asList(new User(10005,"user5",10), new User(10006,"user6",10))));
        try {
            jdbcTemplateService.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
