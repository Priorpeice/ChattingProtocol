package DAO;

public class DatabaseConfig {
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/Network";
    protected static final String DB_USER = "";
    protected static final String DB_PASSWORD = "";

    static {
        initializeDatabaseDriver();
    }

    public static void initializeDatabaseDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}