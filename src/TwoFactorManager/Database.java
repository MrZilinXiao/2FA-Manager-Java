package TwoFactorManager;

import java.sql.*;

/**
 * Database Related Operations Wrapper
 */
public class Database {
    private static Database INSTANCE = null;

    // SQL Related
    private static Connection conn = null;
    private static ResultSet rs = null;
    private static Statement statement = null;

    Database(){
        try { // Init SQL Connection
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:./code.db");
            statement = conn.createStatement();
        }catch (ClassNotFoundException | SQLException ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void execQuery(String query){
        try{
            rs = statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getRs(){
        return rs;
    }

    public static Database getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

}
