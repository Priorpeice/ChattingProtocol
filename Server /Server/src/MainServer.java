import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainServer implements  Runnable
{
        int port = 8000;
        ServerSocket server = null;
        Socket socket = null;

        HashMap<String, OutputStream> hm;

        Map<String, Map<String, Set<String>>> rm; // 동일한 방도 관리가 가능



    public MainServer() {}

    @Override
    public void run()
    {
        ServerThread st;
        Thread t;

        try {
            server = new ServerSocket( port ); //소켓 생성부터 listen까지
            System.out.println( "접속대기" );//출력
            //hashMap 객체를 생성
            hm = new HashMap<>();
            rm = new HashMap<>();


            while( true ) {
                System.out.println("Server while");
                    socket = server.accept();
                System.out.println("Server accept");
                    if (socket.isConnected()) {
                        st = new ServerThread(socket, hm,rm);
                        t = new Thread(st);
                        t.setDaemon(true);
                        t.start();//쓰레드 시작
                    }
            }
            // server.close();
            //System.out.println("Server terminated");
        }
        catch ( Exception e )	{
            e.printStackTrace(System.out);
        }
    }
}
