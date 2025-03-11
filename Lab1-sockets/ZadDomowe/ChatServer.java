import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



public class ChatServer {
    private static final int TCP_PORT = 12345;
    private static final int UDP_PORT = 12345;

    private static final Set<TcpChatterHandlerer> tcpChatters = ConcurrentHashMap.newKeySet();

    static class TcpChatterHandlerer extends Thread{
        private final Socket tcpChatterSocket;
        private PrintWriter out;
        private String name;

        public TcpChatterHandlerer(Socket chatterSocket){
            this.tcpChatterSocket = chatterSocket;
        }

        @Override
        public void run(){
            try (BufferedReader in = new BufferedReader(new InputStreamReader(tcpChatterSocket.getInputStream()))){
                    PrintWriter out = new PrintWriter(tcpChatterSocket.getOutputStream(), true);
                    this.out = out;
                    out.println("Enter your name: ");
                    name = in.readLine();
                    System.out.println("New user connected: " + name);
                    ChatServer.broadcast(name + " has joined the chat", this);

                    String message = in.readLine();
                    while (message != null && !message.equals("exit")){
                        broadcast(name + ": " + message, this);
                        message = in.readLine();
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cleanUp();
            }
        }
        public void sendMessage(String message) {
            out.println(message);
        }


        private void cleanUp(){
            System.out.println("User disconnected: " + name);
            try {
                tcpChatterSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeChatter(this);
            ChatServer.broadcast(name + " has left the chat", this);
        }

    }
    public static void main(String[] args) throws IOException {
        System.out.println("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanUp();
            System.out.println("Server stopped");
        }));

        try (ServerSocket tcpServerSocket = new ServerSocket(TCP_PORT)){
            Thread udpServerThread = new Thread(() -> UdpServer());
            udpServerThread.start();
            while(true){
                Socket tcpChatterSocket = tcpServerSocket.accept();
                System.out.println("New client connected: " + tcpChatterSocket.getInetAddress());
                
                TcpChatterHandlerer chatterHandler = new TcpChatterHandlerer(tcpChatterSocket);
                tcpChatters.add(chatterHandler);
                chatterHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void UdpServer(){
        try (DatagramSocket udpServerSocket = new DatagramSocket(UDP_PORT)){
            byte[] receiveBuffer = new byte[1024];
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                udpServerSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());
                System.out.println("Received UDP message: " + message);
                UdpBroadcast(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void UdpBroadcast(String message){
        for(TcpChatterHandlerer chatter : tcpChatters){
            chatter.sendMessage(message);
        }
    }

    private static void broadcast(String message, TcpChatterHandlerer sender) {
        for (TcpChatterHandlerer chatter : tcpChatters) {
            if (chatter != sender){
                chatter.sendMessage(message);
            }
        }
    }

    private static void removeChatter(TcpChatterHandlerer chatter) {
        tcpChatters.remove(chatter);
    }

    private static void cleanUp(){
        for (TcpChatterHandlerer chatter : tcpChatters){
            chatter.sendMessage("Server is shutting down");
            chatter.cleanUp();
        }
    }
}
