package Controller;

import java.io.*;
import java.util.*;
import DAO.DatabaseHandler;
public class GroupManagerHandler {
    private HashMap<String, OutputStream> hm;
    private Map<String, Map<String, Set<String>>> rm;
    private DatabaseHandler db;


    private int nextGroupId;
    public GroupManagerHandler(HashMap<String, OutputStream> hm, Map<String, Map<String, Set<String>>> rm,DatabaseHandler db, int id) {
        this.hm = hm;
        this.rm = rm;
        this.db = db;
        this.nextGroupId = id;
    }
    private String findRoomIdByRoomName(String roomName) {
        for (Map.Entry<String, Map<String, Set<String>>> entry : rm.entrySet()) {
            Map<String, Set<String>> roomMap = entry.getValue();
            if (roomMap.containsKey(roomName)) {
                return entry.getKey();
            }
        }
        System.out.println("Error: groupId not found for roomName " + roomName);
        return null;
    }
    public void handleGroupMake(String sender, String roomName, String... targetUsers) throws IOException {
        // groupId 증가 및 할당
        String groupId = Integer.toString(nextGroupId++);

        Set<String> users = new HashSet<>();
        for (String item : targetUsers) {
            users.add(item);
        }
        users.add(sender); // sender를 사용자 목록에 추가
        db.getGroupManager().createGroup(groupId, roomName);

        for (String user : users) {
            db.getGroupManager().addUserToGroup(user, groupId);
        }

        rm.put(groupId, new HashMap<>());
        rm.get(groupId).put(roomName, users);

        firstSendUsersInRoom(groupId, roomName);
        // 방 생성 후, 클라이언트에게 응답
    }
    public void addGroupUser(String roomName,String... targetUsers)throws IOException
    {
        String groupId = findRoomIdByRoomName(roomName);

        if (groupId == null) {
            System.out.println("Error: groupId not found for roomName " + roomName);
            return;
        }
        Set<String> users = new HashSet<>();
        // 기존 사용자 목록을 가져와서 새로운 사용자를 추가
        for (String item : targetUsers) {
            users.add(item);
        }
        Set<String> existingUsers = rm.get(groupId).get(roomName);
        if (existingUsers == null) {
            System.out.println("Error: existingUsers is null for roomName " + roomName);
            return;
        }
        existingUsers.addAll(users);
        for (String user : users) {
            db.getGroupManager().addUserToGroup(user, groupId);
        }
        rm.get(groupId).put(roomName, existingUsers);
        firstSendUsersInRoom(groupId, roomName);

    }
    public void firstSendUsersInRoom(String groupId, String roomName) throws IOException {
        Set<String> usersInRoom = rm.get(groupId).get(roomName);
        PrintWriter out;

        synchronized (hm) {
            for (String user : usersInRoom) {
                if (hm.containsKey(user)) {
                    out = new PrintWriter(hm.get(user));
                    if (out != null) {
                        out.println("Success/Welcome! User Joined: " + roomName + " Member :" + usersInRoom);
                        out.flush();
                    } else {
                        System.out.println("connected Error");
                    }
                }
                else {
                    System.out.println("User " + user + " is not online.");
                }
            }
        }
    }

    public void sendUsersInRoom(String roomName, String msg) throws IOException {
        String groupId = findRoomIdByRoomName(roomName);

        if (groupId == null) {
            System.out.println("Error: groupId not found for roomName " + roomName);
            return;
        }

        Set<String> usersInRoom = rm.get(groupId).get(roomName);
        synchronized (hm) {
            for (String user : usersInRoom) {
                if (hm.containsKey(user)) {  // 사용자가 등록되어 있는지 확인
                    PrintWriter out = new PrintWriter(hm.get(user));
                    if (out != null) {
                        out.println(msg);
                        out.flush();
                    } else {
                        System.out.println("connected Error");
                    }
                } else {
                    System.out.println("User " + user + " is not online.");
                }
            }
        }
    }
    //group 나가기
    public void handleGroupLeave(String sender, String roomName, String msg) throws IOException {
        String message = "";
        String groupId = findRoomIdByRoomName(roomName);
        PrintWriter out;

        if (rm.containsKey(groupId)) {
            // 방이 존재하는 경우
            Set<String> usersInRoom = rm.get(groupId).get(roomName);
            db.getGroupManager().deleteUserFromGroup(sender, groupId);
            if (usersInRoom.contains(sender)) {
                usersInRoom.remove(sender);  // 사용자를 방에서 제거
                synchronized (hm) {
                    out=new PrintWriter(hm.get(sender));
                    message = "Success/"+roomName + " success leave";
                    out.println(message);
                    out.flush();
                }
                message = "Success/"+sender + " has gone";
                sendUsersInRoom(roomName, message);
            } //방에 속해 있지 않을때
            else {
                synchronized (hm) {
                    out = new PrintWriter(hm.get(sender));
                    msg = "Error/ You don't belong to " + roomName;
                    out.println(msg);
                    out.flush();
                }
            }
        } //방이 없을떄
        else {
            synchronized (hm) {
                out = new PrintWriter(hm.get(sender));
                msg = "Error/ We don't have " + roomName;
                out.println(msg);
                out.flush();
            }
        }
    }
    //user가 가진 group 보여주기
    public void showGroupNameFromUser(String sender) {
        List<String> userGroups = db.getGroupManager().getUserGroups(sender);
        PrintWriter out;
        if (!userGroups.isEmpty()) {
            synchronized (hm) {
                out = new PrintWriter(hm.get(sender));
                for (String groupName : userGroups) {
                    out.println("SHOWGROUP/" + groupName);
                }
                out.println("END");
                out.flush();
            }
        } else {
            synchronized (hm) {
                out = new PrintWriter(hm.get(sender));
                out.println("Error/You don't have any room");
                out.flush();
            }
        }
    }
    //group에 속한 Users 보기
    public void showUsersinGroup(String sender,String roomName){
        String groupId = findRoomIdByRoomName(roomName);
        PrintWriter out;
        if (groupId == null) {
            System.out.println("Error: groupId not found for roomName " + roomName);
            synchronized (hm) {
                out = new PrintWriter(hm.get(sender));
                out.println("Error/Not found :" + roomName);
                out.flush();
            }
            return;
        }
        Set<String> usersInRoom = rm.get(groupId).get(roomName);
        synchronized (hm) {
            out = new PrintWriter(hm.get(sender));
            for (String user : usersInRoom) {
                out.println("MEMBER/" + user);
            }
            out.println("END");
            out.flush();
        }
    }


}
