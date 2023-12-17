import java.io.*;
import java.util.*;
public class MessageSender {
    private PrintWriter out;
    private static final List<String> COMMANDS = Arrays.asList("LOGIN","ID","BR", "TO", "QUIT", "AGAIN", "GROUPMAKE", "GROUP", "GROUPLEAVE","SHOWGROUP","ADDMACRO","MACRO","SHOWMACRO","INVITE","MEMBER");//Method 종류 관리
    private String lastMessage;
    public MessageSender(PrintWriter out) {
        this.out = out;
    }
    public void sendMessage(Message m) //메소드 종류에 따른 메세지 보내기
    {
        String content;
        String command= m.getCommand();
        // command에 따라 Message 객체의 메소드 호출
        if (!COMMANDS.contains(command)) {
            System.out.println("Not a command");
            return;
        }
        if (command.equals("TO")) {
            String c=m.getContent();
            String r=m.getReceiver();
            content = m.createMessage(r,c);
            lastMessage=content;
        } else if (command.equals("BR")) {
            content = m.getContent();
            content = m.createMessage(content);

        } else if (command.equals("AGAIN")) {
            content = m.createMessage(lastMessage);
        } else if (command.equals("GROUP")){
            content = m.getContent();
            content = m.createMessage(content);
        }
        else if (command.equals("GROUPLEAVE")||command.equals("MEMBER")){
            content = m.createMessage(command);
        } else if (command.equals("ID")||command.equals("LOGIN")) {
            content=m.accountMessage();
        } else {
            content = m.createMessage();
        }
        out.println(content);
        out.flush();
    }

}
