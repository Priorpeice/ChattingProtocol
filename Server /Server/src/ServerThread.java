import Controller.GroupManagerHandler;
import Controller.SendHandler;
import Controller.UserManagerHandler;
import DAO.DatabaseHandler;
import Encryption.Encrypt;

import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread implements Runnable {
    Socket client;//Socket 클래스 타입의 변수 child 선언
    InputStream is;
    OutputStream os;
    BufferedReader in; // BufferReader 클래스 타입의 변수 ois 선언
    PrintWriter out; // PrintWriter 클래스 타입의 변수 oos 선언
    public HashMap<String, OutputStream> hm ; // 접속자 관리
    public Map<String, Map<String, Set<String>>> rm;// 방 관리
    String SEP="/";
    InetAddress ip; // InetAddress 클래스 타입의 변수 ip 선언
    private static int nextGroupId ;
    private DatabaseHandler db;
    private UserManagerHandler userManagerHandler;
    private SendHandler sendHandler;
    private GroupManagerHandler groupManagerHandler;
    private Encrypt encrypt;
    public ServerThread ( Socket s, HashMap<String, OutputStream> h , Map<String, Map<String, Set<String>>> r) throws IOException
    {
       client = s;
       hm = h;
       rm = r;
       encrypt = new Encrypt();
       db = new DatabaseHandler();
        //facade 쓰기
        db.getGroupManager().loadRoomsFromDatabase(rm);
       nextGroupId= db.getGroupManager().getNextGroupId();
       List<String> groupsToDelete = db.getGroupManager().selectAllGroupRoomUserOne();
        if (!groupsToDelete.isEmpty()) {
            db.getGroupManager().deleteAllGroupRoomUserOne();
            for (String groupId : groupsToDelete) {
                rm.remove(groupId);
                db.getGroupManager().deleteGroup(groupId);
            }
        }

        try	{
            is = client.getInputStream();
            os = client.getOutputStream();
            in = new BufferedReader( new InputStreamReader(is) );
            out = new PrintWriter( os );
            ip = client.getInetAddress();
        }catch (IOException e) {
            System.out.println(e.toString());
        }

        userManagerHandler = new UserManagerHandler(hm, db, os);
        sendHandler = new SendHandler(hm,os);
        groupManagerHandler = new GroupManagerHandler(hm,rm,db,nextGroupId);
    }

    public void run()
    {
        String msg; // 문자열 변수 receiveDate 선언
        String Method;
        String Sender =null;
        String password;
        String plain_password;
        String[] targetUsers;
        int Length;
        try {
            while ((msg = in.readLine()) != null)
            {
                System.out.println("received msg: " + msg);
                final String[] tokens = msg.split(SEP);
                Method = tokens[0];
                Sender = tokens[1];

                switch (Method) {
                    case "QUIT":
                        userManagerHandler.handleQuit(Sender);
                        break;
                    case "ID":
                        password=encrypt.encrypt(tokens[2]);
                        userManagerHandler.handleIDRegistration(Sender,password);
                        break;
                    case "LOGIN":
                        plain_password=tokens[2];
                        password =encrypt.encrypt(tokens[2]);
                        userManagerHandler.userAuth(Sender,password,plain_password);
                        break;
                    case "TO":
                        String toID = tokens[2];
                        Length = Integer.parseInt(tokens[4]);
                        sendHandler.sendLengthToSender(Sender,Length);
                        sendHandler.sendTo(Sender,toID, msg);
                        break;
                    case "GROUPMAKE":
                        targetUsers= Arrays.copyOfRange(tokens, 4, tokens.length);// roomName 이후의 내용을 모두 targetUsers로 사용
                        groupManagerHandler.handleGroupMake(Sender, tokens[3], targetUsers);//tokens[3]= groupName
                        break;
                    case "GROUP":
                        groupManagerHandler.sendUsersInRoom(tokens[3],msg);
                        break;
                    case "GROUPLEAVE":
                        groupManagerHandler.handleGroupLeave(Sender, tokens[3],msg);
                        break;
                    case "SHOWGROUP":
                        groupManagerHandler.showGroupNameFromUser(Sender);
                        break;
                    case "INVITE":
                        targetUsers= Arrays.copyOfRange(tokens, 4, tokens.length);
                        groupManagerHandler.addGroupUser(tokens[3],targetUsers);
                        break;
                    case "MEMBER":
                        groupManagerHandler.showUsersinGroup(Sender, tokens[3]);
                        break;
                    default:
                        sendHandler.broadcast(msg);
                        break;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            closeConnections();
        }
    }
    private void closeConnections()
    {
        try {
            in.close();
            out.close();
            client.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
