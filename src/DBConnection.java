/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author pc
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DBConnection {

    public static Connection getConnection() throws Exception {

        InputStream is = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties");

        if (is == null) {
            throw new RuntimeException("db.properties not found in classpath");
        }

        Properties props = new Properties();
        props.load(is);

        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }
}
