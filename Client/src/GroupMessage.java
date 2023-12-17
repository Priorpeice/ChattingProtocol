import java.util.List;
import java.util.stream.Collectors;
public class GroupMessage extends Message {
    private static final String SEP = "/";
    private String roomName; // 그룹 채팅 방 이름 등 추가 가능
    private String[] receivers;
    public GroupMessage(String command, String sender, String roomName,String ...receivers) {
        super(command, sender);
        this.roomName = roomName;
        this.receivers = receivers;
    }
    public GroupMessage(String command, String sender, String roomName, String content) {
        super(command, sender ,content);
        this.roomName = roomName;
    }
    public GroupMessage(String command, String sender, String roomName) {
        super(command, sender);
        this.roomName = roomName;
    }

    @Override //group chatting 용
    public String createMessage(String content) {
        String message = super.createMessage();
        message += SEP + roomName +SEP + content;
        return message;
    }
    @Override  //Groupmake용
    public String createMessage() {
        String message = super.createMessage();
        message += SEP + roomName +SEP ;
        String receiversString = String.join(SEP, receivers);
        message += receiversString + SEP;
        return message;
    }

}
