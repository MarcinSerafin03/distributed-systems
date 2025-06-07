import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Z1_Consumer_WithQoS {

    public static void main(String[] argv) throws Exception {
        System.out.println("Z1 CONSUMER (with QoS)");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String QUEUE_NAME = "queue1";
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.basicQos(1);

        // consumer (handle msg)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("[Consumer " + Thread.currentThread().getId() + "] Received: " + message);
                try {
                    int timeToSleep = Integer.parseInt(message);
                    Thread.sleep(timeToSleep * 1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    System.out.println("[Consumer " + Thread.currentThread().getId() + "] Done: " + message);
                }
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // close
//        channel.close();
//        connection.close();
    }
}
