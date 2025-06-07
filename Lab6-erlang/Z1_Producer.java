import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Z1_Producer {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("Z1 PRODUCER");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                ) {

            // queue
            String QUEUE_NAME = "queue1";
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // producer (publish msg)
            // Zadanie 1.1
//            BufferedReader br = new BufferedReader(new
//                    InputStreamReader(System.in));
//            while (true) {
//                System.out.print("Enter message: ");
//                String message = br.readLine();
//                if (message.equalsIgnoreCase("exit")) {
//                    break;
//                }
//                // publish message
//                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//                System.out.println("Sent: " + message);
//            }

            // Zadanie 1.2
            String[] tasks = {"1", "5", "1", "5", "1", "5", "1", "5", "1", "5"};
            for (String task : tasks) {
                channel.basicPublish("", QUEUE_NAME, null, task.getBytes("UTF-8"));
                System.out.println("Sent: " + task);
            }
        }
    }
}
