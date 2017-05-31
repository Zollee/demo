package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

/**
 * Created by Howard on 05/30/2017.
 */
@RestController
public class UserController {
    public Connection getConnnection() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.jdbc.Driver");

        String url = "jdbc:mysql://47.94.97.91:3306/person";
        String username = "root";
        String password = "zollee.7817";

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Database connection established");
        return conn;
    }

    @RequestMapping("/login")
    public String Login(@RequestParam("username") String username, @RequestParam("password") String password) throws Exception{
        Connection conn = getConnnection();

        try {
            Statement sql_statement = conn.createStatement();

            String query = "select * from user where username = '" + username + "' and password = '" + password + "'";
            ResultSet result = sql_statement.executeQuery(query);

            if (result.next()) {
                return "登陆通过";
            } else
                return "用户名或密码错误";

        }catch (Exception e) {
            e.printStackTrace();
            if (conn != null){
                //事务回滚
                conn.rollback();
            }
        } finally {
            if (conn != null) {
                // 关闭连接
                try {
                    conn.close();
                    System.out.println("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */
                }
            }
        }

        return "0";
    }
}
