package com.example.demo;

import com.example.demo.proto.Data;
import com.example.demo.proto.Person;
import com.example.demo.proto.Phone;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        } else {
            query = "Insert into user(username, password) VALUES ('" + username + "','" + password + "')";
            sql_statement.execute(query);
            return "注册成功";
        }
    }

    @RequestMapping("updateDatabase")
    public String updateDatabase(@RequestParam("key") String url) throws Exception {
        Data data = Data.ADAPTER.decode(url.replace("%25", "%").getBytes());

        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + data.user + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        for (Person temp : data.persons) {
            query = "Insert into nameInfo(userId,id, name, isStarred) VALUES ("
                    + userIndex + ","
                    + temp.id + ",'"
                    + temp.name + "',"
                    + temp.isStarred + ")";
            sql_statement.execute(query);
        }

        for (Phone temp : data.phoned) {
            query = "Insert into phoneInfo(userId,id, nameId,phoneNumber,phoneType) VALUES ("
                    + userIndex + ","
                    + temp.id + ","
                    + temp.nameId + ",'"
                    + temp.number + "',"
                    + temp.type + ")";
            sql_statement.execute(query);
        }
        return "注册成功";
    }

    @RequestMapping("downloadDatabase")
    public String downloadDatabase(@RequestParam("username") String username) throws Exception {
        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + username + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        query = "select * from nameInfo where userId = " + userIndex;
        result = sql_statement.executeQuery(query);

        List<Person> personList = new ArrayList<>();
        while (result.next()) {
            Person temp = new Person.Builder()
                    .id(result.getInt(3))
                    .name(result.getString(4))
                    .isStarred(result.getInt(5))
                    .build();
            personList.add(temp);
        }

        query = "select * from phoneInfo where userId = " + userIndex;
        result = sql_statement.executeQuery(query);

        List<Phone> phoneList = new ArrayList<>();
        while (result.next()) {
            Phone temp = new Phone.Builder()
                    .id(result.getInt(3))
                    .nameId(result.getInt(4))
                    .number(result.getString(5))
                    .type(result.getInt(6))
                    .build();
            phoneList.add(temp);
        }

        Data data = new Data.Builder()
                .user(username)
                .persons(personList)
                .phoned(phoneList)
                .build();
        byte[] dataBytes = Data.ADAPTER.encode(data);

        return new String(dataBytes).replace("%", "%25");
    }

    @RequestMapping("delete")
    public String delete(@RequestParam("username") String username, @RequestParam("Id") int id) throws Exception {
        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + username + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        query = "delete from phoneInfo where nameId=" + id + " and userId =" + userIndex;
        sql_statement.execute(query);

        query = "delete from nameInfo where id=" + id + " and userId =" + userIndex;
        sql_statement.execute(query);

        return "删除成功";
    }

    @RequestMapping("updatePhoto")
    public String updatePhoto(@RequestParam("user") String username, @RequestParam("id") int id, @RequestParam("small") String photoSmall, @RequestParam("large") String photoLarge) throws Exception {
        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + username + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        query = "UPDATE nameInfo SET smallPhoto='" + photoSmall + "',largePhoto='" + photoLarge + "' WHERE id=" + id + " and userId =" + userIndex;
        sql_statement.execute(query);

        return username;
    }

    @RequestMapping("downloadPhoto")
    public String downloadPhoto(@RequestParam("user") String username, @RequestParam("id") int id, @RequestParam("type") String type) throws Exception {
        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + username + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        if (type.equals("small")) {
            query = "SELECT smallPhoto FROM nameInfo WHERE userId=" + userIndex + " and id=" + id;
        } else if (type.equals("large"))
            query = "SELECT largePhoto FROM nameInfo WHERE userId=" + userIndex + " and id=" + id;

        result = sql_statement.executeQuery(query);
        if (result.next()) {
            return result.getString(1);
        }
        return username;
    }

    @RequestMapping("changeStar")
    public String changeStar(@RequestParam("user") String username, @RequestParam("id") int id, @RequestParam("star") int star) throws Exception {
        System.out.println("修改星标");
        Connection conn = getConnection();
        Statement sql_statement = conn.createStatement();

        int userIndex = 0;

        String query = "select id from user where username = '" + username + "'";
        ResultSet result = sql_statement.executeQuery(query);
        if (result.next()) {
            userIndex = result.getInt(1);
        }

        query = "UPDATE nameInfo SET isStarred = " + star + " WHERE userId=" + userIndex + " AND id=" + id;
        sql_statement.execute(query);
        return "true";
    }
}
