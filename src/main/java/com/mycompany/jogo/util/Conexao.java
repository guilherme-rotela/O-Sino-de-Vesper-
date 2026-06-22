package com.mycompany.jogo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL  = "jdbc:postgresql://localhost:5432/SInoDeVesper";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        // Evita que a aplicacao trave indefinidamente se o PostgreSQL
        // nao estiver acessivel: falha em 5 segundos em vez de ficar
        // esperando o timeout padrao do sistema operacional (que pode
        // levar mais de um minuto e ate derrubar o processo no Windows).
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
