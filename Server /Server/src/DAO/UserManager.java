package DAO;



import java.sql.*;

public class UserManager extends DatabaseConfig {
    private static final String INSERT_USER_QUERY = "INSERT INTO users (user_id, password) VALUES (?, ?)";
    private static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_USER_PASSWORD = "SELECT password FROM users WHERE user_id = ?";
    public static void saveIDToDB (String id,String pw) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_QUERY)) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, pw);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Login 대조를 위함
    public static String getPasswordFromDB(String id) {
        String password = null;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_PASSWORD)) {
                preparedStatement.setString(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        password = resultSet.getString("password");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return password;
    }
    //등록된 사용자인지 체크
    public static boolean isIDRegistered(String id) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_QUERY)) {
                preparedStatement.setString(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
