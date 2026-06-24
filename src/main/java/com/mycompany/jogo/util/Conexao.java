package com.mycompany.jogo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL  = "jdbc:postgresql://localhost:5432/Vesper";
    private static final String USER = "postgres";
    private static final String PASS = "postgre";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        DriverManager.setLoginTimeout(5);
        connection = DriverManager.getConnection(URL, USER, PASS);
    }
    return connection;
}

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
