package Controller;

import java.io.*;
import java.util.*;

public class SendHandler {
    private HashMap<String, OutputStream> hm;
    private OutputStream os;

    public SendHandler(HashMap<String, OutputStream> hm, OutputStream os)
    {
        this.hm = hm;
        this.os=os;
    }

    public void broadcast(String message) throws IOException {

        synchronized (hm) {
            for ( OutputStream os: hm.values()) {
                PrintWriter out= new PrintWriter(os);
                out.println(message);
                out.flush();
            }
        }
    }

    public void sendTo(String sender,String receiver, String message) throws IOException {
        if (hm.containsKey(receiver)) {  // 사용자가 등록되어 있는지 확인
            PrintWriter out = new PrintWriter(hm.get(receiver));
            if (out != null) {
                out.println(message);
                out.flush();
            } else {
                System.out.println("connected Error");
            }
        } else {
            PrintWriter out = new PrintWriter(hm.get(sender));
            if (out != null) {
                out.println("Error/User " + receiver + " is offline.");
                out.flush();
            }
        }
    }

    public void sendLengthToSender(String sender, int messageLength) {
        PrintWriter out = new PrintWriter(hm.get(sender));
        try {
            // sender에게 길이 정보를 보내기
            String lengthMessage = "LENGTH" + ":" + messageLength + ":" + "good";
            out.println(lengthMessage);
            out.flush();
        } catch (Exception e) {
            // 예외 처리: 스트림이 닫혔거나 전송 중 오류가 발생할 경우
            e.printStackTrace();
        }
    }
}
