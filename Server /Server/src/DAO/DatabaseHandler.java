package DAO;

public class DatabaseHandler {
    private UserManager userManager;
    private GroupManager groupManager;
//facade를 구현
    public DatabaseHandler() {
        this.userManager = new UserManager();
        this.groupManager = new GroupManager();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

}
