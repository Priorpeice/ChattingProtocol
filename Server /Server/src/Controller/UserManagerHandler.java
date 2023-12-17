package Controller;

import java.io.*;
import java.util.*;
import DAO.DatabaseHandler;
public class UserManagerHandler {
    private HashMap<String, OutputStream> hm;
    private DatabaseHandler db;
    private  OutputStream os;

    public UserManagerHandler(HashMap<String, OutputStream> hm, DatabaseHandler db,  OutputStream os) {
        this.hm = hm;
        this.db = db;
        this.os = os;
    }

    public void handleQuit(String sender) {
        System.out.println(sender + "종료합니다.");
        synchronized (hm) {
            try {
                hm.remove(sender);
            } catch (Exception ignored) {
            }
        }
    }
//ppw = plain password
    public void userAuth(String sender, String pw, String ppw) {
        String DBpw = db.getUserManager().getPasswordFromDB(sender);
        PrintWriter out;
        if (pw.equals(DBpw)) {
            synchronized (hm) {
                hm.put(sender, os);
                out=new PrintWriter(hm.get(sender));
                out.println("LOGIN/Success/" + sender);
                out.flush();
            }
        } else {
            out = new PrintWriter(os);
            out.println("LOGIN/Fail/" + ppw);
            out.flush();

        }
    }

    public void handleIDRegistration(String sender, String pw) throws IOException {
        PrintWriter out;
        if (db.getUserManager().isIDRegistered(sender)) {
            out=new PrintWriter(os);
            out.println("ID/Fail/Reg_ID");
            out.flush();
        } else {
            db.getUserManager().saveIDToDB(sender, pw);
            synchronized (hm) {
                hm.put(sender, os);
                out=new PrintWriter(hm.get(sender));
                out.println("ID/Success/Reg_ID");
                out.flush();
            }
        }
    }

}
