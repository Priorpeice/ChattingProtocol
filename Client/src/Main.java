import Macro.MacroFileWriter;
import Macro.MacroFileReader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//print랑 flush는 set다.

public class Main implements Runnable
{
    private static final int PORT = 8000;
    static Socket csocket ;
    static BufferedReader in;
    static PrintWriter out;
    //critical section 에서 쓰는 거처럼 atomic이 변수다,
    private static AtomicBoolean ID_reg_Flag = new AtomicBoolean(false);
    static Message message;  // Message 객체 생성
    private static Map<String, String> macro = new HashMap<>();
    private static String pathToTextFolder = System.getProperty("user.dir") + File.separator + "macro" + File.separator + "text";
    private static String pathToTextFile;
    public void reg_stop()
    {
        ID_reg_Flag.set(false);
        synchronized (this) {
            this.notify(); // 대기 중인 스레드를 깨움
        }
    }
    public void reg_go()
    {
        ID_reg_Flag.set(true);
        synchronized (this) {
            this.notify();// 대기 중인 스레드를 깨움
        }
    }
    //메시지 받는 부분
    @Override
    public void run() {
        String msg;
        String[] tokens;
        StringBuilder sb= new StringBuilder();
        while (true){
            try {
                msg = in.readLine();
                tokens = msg.split("/");
            } catch (IOException e) {
                break;
            }
            if (tokens[0].equals("ID"))
            {
                if(tokens[1].equals("Success"))
                {
                    System.out.println("ID 등록이 성공했습니다");
                    reg_stop();
                }else
                {
                    System.out.println("중복된 ID 입니다.");
                    reg_go();
                }
            } else if (tokens[0].equals("LOGIN"))
            {
                if(tokens[1].equals("Success"))
                {
                    System.out.println(tokens[2]+"님 어서오세요");
                    reg_stop();
                }
                else
                {
                    System.out.println("FAIL! "+tokens[2]);
                    reg_go();
                }
            }
            else if (Objects.equals(tokens[0],"BR"))
            {
                System.out.println(tokens[3]+" "+tokens[4]+" by "+tokens[1] + " " +tokens[2]);
            }
            else if(Objects.equals(tokens[0], "TO"))
            {
                System.out.println("Sender : "+tokens[1] +" Message :"+tokens[5]);
            }
            else if (Objects.equals(tokens[0], "GROUP"))
            {
                System.out.println(tokens[4]+" by " + tokens[1] + " time :"+tokens[2]);
            }
            else if (Objects.equals(tokens[0],"SHOWGROUP"))
            {
               sb.append("GroupName: ").append(tokens[1]).append("\n");
            }
            else if (Objects.equals(tokens[0],"MEMBER"))
            {
                sb.append("Member: ").append(tokens[1]).append("\n");
            }
            else if (Objects.equals(tokens[0],"END"))
            {
                System.out.println(sb);
                sb.setLength(0);
                // 여기 설정
            } else if (Objects.equals(tokens[0],"Error")) {
                System.out.println(tokens[0]+" "+tokens[1]);
            }else if(Objects.equals(tokens[0],"Success"))
            {
                System.out.println(tokens[1]);
            }
            else if (msg != null)
            {
                System.out.println("서버로부터 온 메시지: " + msg);
            }
            else {
                System.out.println("서버 연결이 종료되었습니다.");
                break;
           }
        }
    }
    private static void showUsage()
    {
        System.out.println("USAGE: \"BR/message\"if you want to send your message to whole other clients");
        System.out.println("USAGE: \"TO/peerID/message\"if you want to send your message to the specific client");
        System.out.println("USAGE: \"Quit/\"if you want to stop");
        System.out.println("USAGE: \"Again/\" to send the last sent message again");
        System.out.println("USAGE: \"groupmake/roomName/receiver1/receiver2/...\" Making the GroupChatting room");
        System.out.println("USAGE: \"Group/roomName/message\" Making the GroupChatting room");
        System.out.println("USAGE: \"groupleave/roomName\" Making the GroupChatting room");
        System.out.println("USAGE: \"showGroup/\"Show all the group rooms you belong to ");
        System.out.println("USAGE: \"addMacro/macroName/allmessageFormat\" Register your own macro ");
        System.out.println("USAGE: \"macro/macroName\" Using Your own macro ");
        System.out.println("USAGE: \"showMacro/\" Show Macro what you have ");
        System.out.println("USAGE: \"Invite/roomName/receiver1/receiver2/...\" if you want invite your friends you can invite them");
        System.out.println("USAGE: \"Member/roomName/\" Find Your GroupRoom Memeber");
    }

    private static void showMacros() {
        System.out.println("Your Macros:");
        for (Map.Entry<String, String> entry : macro.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

    }
    public static void main(String[] args) throws IOException
    {

        Main t = new Main();
        final Scanner sc = new Scanner(System.in);
            // 소켓 생성
            csocket = new Socket("localhost", PORT);
            // 읽기 스트림
            in = new BufferedReader (new InputStreamReader(csocket.getInputStream()));
            // 쓰기 스트림
            out = new PrintWriter(csocket.getOutputStream());
            MessageSender ms = new MessageSender(out);

            //override run recieve 전용
            Thread rt = new Thread(t);
            rt.start();

            String msg;
            String myID="";
            String userChoice;
            String myPW;
            StringBuilder valueBuilder;
            String[] receivers;

        do {
                System.out.println("Do you hava a account?");
                System.out.println("if you want a make a account, plz press the button '1'");
                System.out.println("If not, please enter your ID.");
                userChoice = sc.nextLine();
                if (userChoice.equals("1")) {
                    System.out.println("Enter your ID");
                    String userID = sc.nextLine();
                    System.out.println("Enter your password");
                    String password = sc.nextLine();
                    message = new Message("ID", userID,password);
                    myID=userID;
                    myPW=password;
                }else {
                    myID = userChoice;
                    System.out.println("Enter your password");
                    myPW = sc.nextLine();
                    message = new Message("LOGIN", myID, myPW);
                }
                ms.sendMessage(message);
                synchronized (t) {
                    try {
                        t.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }while ( ID_reg_Flag.get());

            showUsage();
            pathToTextFile = pathToTextFolder + File.separator + myID+".txt";
            MacroFileReader.readMacroFile(pathToTextFile, macro);

            while (true)
            {

                try {
                    msg = sc.nextLine(); // 개행 문자 소비

                    String[] tokens = msg.split("/");
                    String code = tokens[0];
                    /* command에 따라 Message 객체 업데이트*/
                    if (code.equalsIgnoreCase("MACRO")) {
                        if (macro.containsKey(tokens[1])) {
                            valueBuilder = new StringBuilder();
                            valueBuilder.append(macro.get(tokens[1]));
                            for (int i = 2; i < tokens.length; i++) {
                                valueBuilder.append(tokens[i]);
                                if (i < tokens.length ) {
                                    valueBuilder.append("/");
                                }
                            }
                            msg = valueBuilder.toString();
                            tokens=msg.split("/");
                            code=tokens[0];
                        }
                    }
                    if (code.equalsIgnoreCase("Quit")) {
                        message = new Message("QUIT", myID);
                        ms.sendMessage(message);
                        break;
                    } else if (code.equalsIgnoreCase("ADDMACRO")) {
                        valueBuilder = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            valueBuilder.append(tokens[i]);
                            if (i < tokens.length ) {
                                valueBuilder.append("/");
                            }
                        }
                        String value = valueBuilder.toString();
                        macro.put(tokens[1],value);
                        // 매크로를 파일에 쓰기
                        MacroFileWriter.writeMacroToFile(pathToTextFolder, myID+".txt", macro);
                        continue;
                    } else if (code.equalsIgnoreCase("To")) {
                        message = new Message("TO", myID, tokens[1], tokens[2]); // 1 sender , 2 message
                    } else if (code.equalsIgnoreCase("BR")) {
                        message = new GroupMessage("BR", myID,"All",tokens[1]);
                    } else if (code.equalsIgnoreCase("AGAIN")) {
                        message = new Message("AGAIN", myID);
                    } else if (code.equalsIgnoreCase("GROUPMAKE")) {
                        receivers = Arrays.copyOfRange(tokens, 2, tokens.length);
                        message = new GroupMessage("GROUPMAKE", myID, tokens[1], receivers); // tokens[1] = roomName
                    } else if (code.equalsIgnoreCase("GROUP")) {
                        message = new GroupMessage("GROUP", myID, tokens[1], tokens[2]); // tokens[1] = roomName 2, message
                    } else if (code.equalsIgnoreCase("GROUPLEAVE")) {
                        message = new GroupMessage("GROUPLEAVE", myID, tokens[1]); // tokens[1] = roomName
                    }else if(code.equalsIgnoreCase("SHOWGROUP")){
                        message= new Message("SHOWGROUP",myID);
                    } else if (code.equalsIgnoreCase("SHOWMACRO")) {
                        showMacros();
                        continue;
                    } else if (code.equalsIgnoreCase("INVITE"))
                    {
                        receivers = Arrays.copyOfRange(tokens, 2, tokens.length);
                        message=new GroupMessage("INVITE", myID, tokens[1], receivers);
                    } else if (code.equalsIgnoreCase("MEMBER")) {
                        message=new GroupMessage("MEMBER",myID,tokens[1]);
                    } else{
                        message = new Message("ERROR", myID);
                    }

                    ms.sendMessage(message);
                }/*else{  //정해진 사용법이 아닌 것으로 입력시
                   // showUsage();
                } */ catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Invalid input format. Please try again.");
                } catch (Exception e) {
                    System.out.println("An error occurred. Please try again.");
                }
            }
            out.close();
            csocket.close();
    }
}
