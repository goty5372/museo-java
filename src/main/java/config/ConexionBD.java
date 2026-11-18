package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/sistema_museos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    public static Connection obtenerConexion() throws SQLException {
        String url = System.getenv().getOrDefault("MUSEO_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("MUSEO_DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("MUSEO_DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
