import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;

public class Chatter {
    private static final String hostName = "localhost";
    private static final int portNumber = 12345;
    private static final int MULTICAST_PORT = 12346;
    private static final String MULTICAST_ADDRESS = "230.0.0.1";

    private MulticastSocket multicastSocket;
    private DatagramSocket udpSocket;
    private InetAddress multicastAddress;
    private InetSocketAddress multicastGroup;
    private NetworkInterface multicastInterface;
    private Socket tcpSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;
    private String userName;

    private void initializeSockets(){
        try{
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastGroup = new InetSocketAddress(multicastAddress, MULTICAST_PORT);
            multicastInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            multicastSocket.joinGroup(multicastGroup, multicastInterface);        


            udpSocket = new DatagramSocket();
            tcpSocket = new Socket(hostName, portNumber);

            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));

        }catch(SocketException e){
            System.out.println("Connection to server failed");
            System.exit(1);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareComunication(){
        try{
            System.out.println(in.readLine());
            userName = stdIn.readLine();
            out.println(userName);
            startListeningThread();
            handleUserInput();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startListeningThread(){
        new Thread(this::serverThread).start();
        new Thread(this::multicastThread).start();
    }

    private void serverThread(){
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if(message.equals("Server is shutting down")){
                    serverShuttingDown();
                    break;
                }
                System.out.println(message);

            }
        } catch (IOException e) {
            if(!tcpSocket.isClosed()){
                e.printStackTrace();
            }
        }
    }

    private void multicastThread(){
        try {
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            while (!multicastSocket.isClosed()) {
                multicastSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            if(!multicastSocket.isClosed()){
                e.printStackTrace();
            }
        }
    }

    
    private void sentUdpMessage(){
        try{
            String udpSentMessage = "[UDP] " + userName + ": \n" +
            "   _____  \n" +
            "  /     \\ \n" +
            " | O   O | \n" +
            " |   ^   | \n" +
            " |  \\_/  | \n" +
            "  \\_____/ \n";
            byte[] udpSendBuffer = udpSentMessage.getBytes();
            DatagramPacket udpSendPacket = new DatagramPacket(udpSendBuffer, udpSendBuffer.length, tcpSocket.getInetAddress(), portNumber);
            udpSocket.send(udpSendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sentMulticastMessage(){
        try{
            String multicastSentMessage = "[Multicast] " + userName + ": " + "sent ASCII art";
            byte[] multicastSendBuffer = multicastSentMessage.getBytes();
            DatagramPacket multicastSendPacket = new DatagramPacket(multicastSendBuffer, multicastSendBuffer.length, multicastAddress, MULTICAST_PORT);
            multicastSocket.send(multicastSendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void handleUserInput(){
        try{
            String userInput;
            while((userInput = stdIn.readLine()) != null){
                switch(userInput){
                    case "U":
                        sentUdpMessage();
                        break;
                    case "M":
                        sentMulticastMessage();
                        break;
                    default:
                        out.println(userInput);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUp(){
        try{
            if(tcpSocket != null && !tcpSocket.isClosed()){
                tcpSocket.close();
            }
            if(multicastSocket != null && !multicastSocket.isClosed()){
                multicastSocket.leaveGroup(multicastGroup, multicastInterface);
                multicastSocket.close();
            }

            if(udpSocket != null && !udpSocket.isClosed()){
                udpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start(){
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUp));
        try {
            initializeSockets();
            prepareComunication();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            cleanUp();
        }
    }

    public static void main(String[] args) throws IOException {
        new Chatter().start();
    }

    private static void serverShuttingDown(){
        System.out.println("Server is shutting down");
        System.exit(0);
    }
}
