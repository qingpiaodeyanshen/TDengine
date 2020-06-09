package com.taosdata.jdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TSDBImportTest {
    Connection connection = null;
    Statement statement = null;
    String dbName = "test";
    String tName = "t0";
    String host = "localhost";

    @Before
    public void createDatabase() throws SQLException {
        try {
            Class.forName("com.taosdata.jdbc.TSDBDriver");
        } catch (ClassNotFoundException e) {
            return;
        }
        Properties properties = new Properties();
        properties.setProperty(TSDBDriver.PROPERTY_KEY_HOST, host);
        connection = DriverManager.getConnection("jdbc:TAOS://" + host + ":0/" + "?user=root&password=taosdata"
                , properties);

        statement = connection.createStatement();
        statement.executeUpdate("drop database if exists " + dbName);
        statement.executeUpdate("create database if not exists " + dbName);
        statement.executeUpdate("create table if not exists " + dbName + "." + tName + " (ts timestamp, k int, v int)");

    }

    @Test
    public void insertInto() throws Exception {
        long ts = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            ts += i;
            int row = statement.executeUpdate("insert into " + dbName + "." + tName + " values (" + ts + ", " + (100 + i) + ", " + i + ")");
            System.out.println("insert into " + dbName + "." + tName + " values (" + ts + ", " + (100 + i) + ", " + i + ")" + "\t" + row);
            assertEquals(1, row);
        }
    }

    @Test
    public void importInto() throws Exception {
        // 避免时间重复
        long ts = System.currentTimeMillis() + 1000;

        StringBuilder sqlBuilder = new StringBuilder("insert into ").append(dbName).append(".").append(tName).append(" values ");

        for (int i = 0; i < 50; i++) {
            int a = i / 5;
            long t = ts + a;
            sqlBuilder.append("(").append(t).append(",").append((100 + i)).append(",").append(i).append(") ");
        }
        System.out.println(sqlBuilder.toString());
        int rows = statement.executeUpdate(sqlBuilder.toString());
        System.out.println(rows);
        assertEquals(10, rows);
    }

    @After
    public void close() throws Exception {
        statement.executeQuery("drop database " + dbName);
        statement.close();
        connection.close();
    }
}
