import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Message {
    private static final String SEP = "/";

    private String command;
    private String sender;
    private String receiver;
    private String content;
    private DateTimeFormatter formatter;

    public Message(String command, String sender) {
        this.command = command;
        this.sender = sender;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    public Message(String command, String sender ,String content){
        this.command = command;
        this.sender = sender;
        this.content= content;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    public Message (String command, String sender, String receiver, String content) {
        this.command = command;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }
    public String getCommand(){
        return command;
    }
    //기본 format
    public String createMessage() {
        String message="";
        String timestamp = LocalDateTime.now().format(formatter);
        message = command + SEP + sender + SEP + timestamp;
        return message;
    }
    //again용 메시지 포맷
    public String createMessage(String lastContent) {
        return lastContent;
    }
    //주로 to에서 사용되는 메시지 생성
    public String createMessage(String recipient, String content) {
        String message="";
        String timestamp = LocalDateTime.now().format(formatter);
        int messageLength =content.length();
        message = command + SEP + sender + SEP + recipient + SEP + timestamp + SEP + messageLength + SEP + content;
        return message;
    }
    //계정과 관련된 메시지
    public String accountMessage() {
        String message="";
        message = command + SEP + sender + SEP + content+ SEP;
        return message;
    }



}
