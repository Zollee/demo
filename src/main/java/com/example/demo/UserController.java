package com.example.demo;

import com.example.demo.proto.Data;
import com.example.demo.proto.Person;
import com.example.demo.proto.Phone;
import org.springframework.web.bind.annotation.*;
import sun.net.www.content.text.Generic;

import java.net.URLDecoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Howard on 05/30/2017.
 */
@RestController
public class UserController {
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");

        String url = "jdbc:mysql://47.94.97.91:3306/person";
        String username = "root";
        String password = "zollee.7817";

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Database connection established");
        return conn;
    }

    @RequestMapping("/login")
    public String Login(@RequestParam("username") String username, @RequestParam("password") String password) throws Exception {
        Connection conn = getConnection();

            Statement sql_statement = conn.createStatement();

            String query = "select * from user where username = '" + username + "' and password = '" + password + "'";
            ResultSet result = sql_statement.executeQuery(query);

            if (result.next()) {
                return "登录通过";
            } else
                return "用户名或密码错误";
    }

    @RequestMapping("signup")
    public String SignUp(@RequestParam("username") String username, @RequestParam("password") String password) throws Exception {
        Connection conn = getConnection();

            Statement sql_statement = conn.createStatement();

            String query = "select * from user where username = '" + username + "'";
            ResultSet result = sql_statement.executeQuery(query);

            if (result.next()) {
                return "用户名重复";
            } else{
                query = "Insert into user(username, password) VALUES ('" + username + "','" + password + "')";
                sql_statement.execute(query);
                return "注册成功";
            }
    }

    @RequestMapping(value = "updateDatabase")
    public String updateDatabase(@RequestParam("key") String url) throws Exception{
        Data data = Data.ADAPTER.decode(url.replace("%25","%").getBytes());

        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex=0;

        String query = "select id from user where username = '" + data.user + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        for (Person temp:data.persons) {
            query = "Insert into nameInfo(userId,id, name, photoSmall, photoLarge, isStarred) VALUES ("
                    + userIndex + ","
                    + temp.id + ",'"
                    + temp.name + "','"
                    + temp.photoSmall + "','"
                    + temp.photoLarge + "',"
                    + temp.isStarred + ")";
            System.out.println(query);
            sql_statement.execute(query);
        }

        for (Phone temp:data.phoned){
            query = "Insert into phoneInfo(userId,id, nameId,phoneNumber,phoneType) VALUES ("
                    + userIndex + ","
                    + temp.id + ","
                    + temp.nameId + ",'"
                    + temp.number + "',"
                    + temp.type + ")";
            System.out.println(query);
            sql_statement.execute(query);
        }
        return "注册成功";
    }
}
