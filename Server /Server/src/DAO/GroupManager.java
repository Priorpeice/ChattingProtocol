package DAO;


import java.sql.*;
import java.util.*;

public class GroupManager extends DatabaseConfig {
    private static final String INSERT_GROUP_QUERY = "INSERT INTO grouproom (groupid, groupname) VALUES (?, ?)";
    private static final String INSERT_USER_TO_GROUP_QUERY = "INSERT INTO usergrouproom (user_id, group_id) VALUES (?, ?)";
    private static final String DELETE_USER_TO_GROUP_QUERY = "DELETE FROM usergrouproom WHERE user_id = ? AND group_id = ?";
    private static final String SELECT_FROM_GROUPROOM ="SELECT * FROM grouproom";
    private static final String SELCET_FROM_USERGROUPROOM= "SELECT user_id FROM usergrouproom WHERE group_id = ?";
    private static final String SLECET_MAX_GROUPID= "SELECT MAX(groupid) AS maxGroupId FROM grouproom";
    private static final String SELECT_ALLGROUPNAME= "SELECT g.groupname " + "FROM users u " + "JOIN usergrouproom ug ON u.user_id = ug.user_id " + "JOIN grouproom g ON ug.group_id = g.groupid " + "WHERE u.user_id = ?";
    private static final String DELETE_ALLGROUPROOM_USERONE =    "DELETE FROM usergrouproom " + "WHERE group_id IN (" + "SELECT group_id FROM (" + "SELECT ug.group_id " + "FROM usergrouproom ug " + "LEFT JOIN usergrouproom ug2 ON ug.group_id = ug2.group_id AND ug.user_id != ug2.user_id " + "WHERE ug2.user_id IS NULL" + ") AS subquery" + ")";
    private static final String SELECT_ALLGROUPROOM_USERONE ="SELECT group_id FROM usergrouproom " + "WHERE group_id IN (" + "SELECT group_id FROM (" + "SELECT ug.group_id " + "FROM usergrouproom ug " + "LEFT JOIN usergrouproom ug2 ON ug.group_id = ug2.group_id AND ug.user_id != ug2.user_id " + "WHERE ug2.user_id IS NULL" + ") AS subquery" + ")";
    private static final String DELETE_GROUPID= "DELETE FROM grouproom WHERE groupid = ?";

    //그룹 채팅방 만들기
    public static void createGroup(String groupId, String groupName) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GROUP_QUERY)) {
                preparedStatement.setString(1, groupId);
                preparedStatement.setString(2, groupName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //이용자 추가하기
    public static void addUserToGroup(String userId, String groupId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_TO_GROUP_QUERY)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, groupId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //그룹에서 이용자 삭제
    public static void deleteUserFromGroup(String userId, String groupId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_USER_TO_GROUP_QUERY)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, groupId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //서버 재시작시 서버로 방 정보 저장하기
    public void loadRoomsFromDatabase(Map<String, Map<String, Set<String>>> rm) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_GROUPROOM)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String groupId = resultSet.getString("groupid");
                        String roomName = resultSet.getString("groupname");

                        // 방 정보 추가
                        rm.put(groupId, new HashMap<>());
                        rm.get(groupId).put(roomName, new HashSet<>());

                        // 해당 방에 속한 사용자 정보 가져오기
                        Set<String> usersInRoom = getUsersInRoom(groupId);
                        rm.get(groupId).get(roomName).addAll(usersInRoom);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //방에 있는 사용자 정보 가져오기
    private Set<String> getUsersInRoom(String groupId) {
        Set<String> usersInRoom = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELCET_FROM_USERGROUPROOM)) {
                preparedStatement.setString(1, groupId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String userId = resultSet.getString("user_id");
                        usersInRoom.add(userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usersInRoom;
    }
    // 서버에서 그룹 id를 위한 id 업데이트  (random으로 id 부여로 변경 할 수도 있음)
    public static int getNextGroupId() {
        int nextGroupId = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SLECET_MAX_GROUPID)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        nextGroupId = resultSet.getInt("maxGroupId") + 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextGroupId;
    }
    //사용자가 속한 채팅방을 보여주기 위함
    public static List<String> getUserGroups(String userID) {
        List<String> userGroups = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALLGROUPNAME)) {
                preparedStatement.setString(1, userID);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String groupName = resultSet.getString("groupname");
                        userGroups.add(groupName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userGroups;
    }
    //사용자가 한명 밖에 없는 방 찾기
    public static List<String> selectAllGroupRoomUserOne() {
        List<String> groupInfoList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALLGROUPROOM_USERONE);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String groupId = resultSet.getString("group_id");
                groupInfoList.add(groupId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groupInfoList;
    }
    //n:m관계를 해소하기 위한 usergrouproom에서 한명 밖에 없는 채팅방 관계 삭제
    public static void deleteAllGroupRoomUserOne() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ALLGROUPROOM_USERONE)) {

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " rows from usergrouproom.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //해당 id 방 삭제
    public static void deleteGroup(String id) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_GROUPID)) {
                preparedStatement.setString(1, id);
                preparedStatement.executeUpdate();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
