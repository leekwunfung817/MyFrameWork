/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MQUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ProducerInfo;

/**
 *
 * @author Reinfo
 */
public class MQConn {

    ConnectionFactory connectionFactory;
    Connection connection;
    Session session;
//    Destination destination;
//    MessageConsumer consumer;
//    MessageProducer producer;
    Thread thread;
    MQConn _this = this;
    String send_text_content;
    String receive_text_content;
    TextMessage send_message;
    TextMessage receive_message;
    Message message;
    boolean keep_run = false;

    String mq_name;
    String session_id;
    int max_session_subfix = 3;
    String session_divide = "<sess>";

    public MQConn(String mq_name) throws Exception {
        String subfix = (mq_name.length() < max_session_subfix ? mq_name : mq_name.substring(0, max_session_subfix));
        this.session_id = (new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS,")).format(new Date()) + subfix;

        this.mq_name = mq_name;
        reconnect();
        // Getting the queue 'JCG_QUEUE'
//        destination = session.createQueue(this.mq_name);
        // MessageConsumer is used for receiving (consuming) messages
//        consumer = session.createConsumer(destination);
//        producer = session.createProducer(destination);
//        System.out.println("session ID '" + this.session_id + "'");
//        System.out.println("subfix ID '" + subfix + "'");
//        System.out.println("mq_name '" + mq_name + "'");
//        System.out.println("get Client ID '" + connection.getClientID() + "'");
    }

    public void reconnect() {
        try {
            connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            // Creating session for seding messages
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, "Fail to connect, re-try in 3 seconds", ex);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, null, ex1);
            }
            reconnect();
        }

    }

    public void StartListenReturn() {
        StartListen(getReturnMQName(mq_name, session_id), true);
    }

    public void StartListen() {
        StartListen(mq_name, false);
    }
    private String return_mq_name = null;
    private String return_session_id = null;

    protected void SendReturnMessage(String message) {
        try {
            if (return_mq_name != null && return_session_id != null) {
                SendMessage(return_mq_name, return_session_id, message);
            } else {
                System.out.println("Please start return message listener");
            }
        } catch (JMSException ex) {
            Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void StartListen(final String mq_name, final boolean is_return) {
        System.out.println("Start Listen:" + mq_name);
        if (thread != null) {
            return;
        }
//        String mq_name_ = mq_name;
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run(); //To change body of generated methods, choose Tools | Templates.
                    keep_run = true;

                    // Getting the queue 'JCG_QUEUE'
                    Destination destination = session.createQueue(mq_name);

                    // MessageConsumer is used for receiving (consuming) messages
                    MessageConsumer consumer = session.createConsumer(destination);
                    while (keep_run) {

                        try {

                            // Here we receive the message.
//                        System.out.println("wait for message");
                            Message message = consumer.receive();

                            if (message instanceof ActiveMQMessage) {
                                ActiveMQMessage aMsg = (ActiveMQMessage) message;
                                ProducerInfo prod = (ProducerInfo) aMsg.getDataStructure();
                                String producter_name = (String) aMsg.getPropertyNames().toString();

//                            System.out.println("From producter name '" + producter_name + "'");
//                            System.out.println("From producter '" + prod.getProducerId().getValue() + "'");
                            }
//                        System.out.println("receive message");

                            // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
                            // so we must cast to it to get access to its .getText() method.
                            if (message instanceof ActiveMQBytesMessage) {
                                
                                ActiveMQBytesMessage bm = (ActiveMQBytesMessage) message;
                                try {
                                    byte[] bs = bm.getMessage().getContent().getData();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if (message instanceof TextMessage) {
                                receive_message = (TextMessage) message;
                                receive_text_content = receive_message.getText();
                                String[] session_arr = receive_text_content.split(session_divide);
//                            System.out.println(receive_text_content);
//                            System.out.println(session_divide);
//                            System.out.println(session_arr.length);
//                            for (String session_arr1 : session_arr) {
//                                System.out.println(session_arr1);
//                            }
                                String session_id = session_arr[0];
                                receive_text_content = session_arr[1];
                                System.out.println("Received message '" + receive_text_content + "' from " + session_id + " in " + mq_name + " is_return " + is_return);
                                return_mq_name = null;
                                return_session_id = null;

                                if (is_return) {
//                                    client
//                                    System.out.println("is return message from " + return_mq_name + " " + return_session_id);
                                    OnReturnMessage(mq_name, receive_text_content);
                                } else {
//                                    server
                                    return_mq_name = mq_name + "";
                                    return_session_id = session_id + "";
                                    OnMessage(mq_name, session_id, receive_text_content);
                                }

                            }

                        } catch (IllegalStateException ex) {

                            Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, "Cannot dequeue", ex);
                        } catch (JMSException ex) {
                            Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, "Cannot dequeue", ex);
                        }
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
        thread.start();
    }

    protected void OnMessage(String mq_name, String session, String text) {
        System.out.println("Received message '" + text + "' from " + session + " in " + mq_name);
        OnMessage(session, text);
    }

    protected void OnMessage(String session, String text) {
        OnMessage(text);
    }

    protected void OnMessage(String text) {

    }

    protected void OnReturnMessage(String mq_name, String text) {
        OnReturnMessage(text);
    }

    protected void OnReturnMessage(String text) {

    }

    private String getReturnMQName(String mq_name, String to_session) {
        return mq_name + "||" + to_session;
    }

    public void SendMessage(String mq_name, String to_session, String text) throws JMSException {
        SendMessage(getReturnMQName(mq_name, to_session), text);
    }

    public void SendMessage(String text) throws JMSException {
        SendMessage(mq_name, text);
    }

    public void SendMessage(String mq_name, String text) throws JMSException {
        Destination destination = session.createQueue(mq_name);
        MessageProducer producer = session.createProducer(destination);

        // We will send a small text message saying 'Hello World!!!' 
//        TextMessage message = session.createTextMessage("Hello !!! Welcome to the world of ActiveMQ.");
        send_message = session.createTextMessage(session_id + session_divide + text);
        // Here we are sending our message!
        send_text_content = send_message.getText();
        System.out.println("Send message '" + mq_name + "'>'" + text + "'");
        producer.send(send_message);
//        System.out.println("JCG printing@@ '" + send_text_content + "'");
    }

    public void stop() throws JMSException {
        session.close();
        connection.close();
        keep_run = false;
        thread.interrupt();
        thread.stop();
//        thread.destroy();
        thread = null;
    }

//
//    public static void main(String[] args) {
//
//        Logger.getLogger(MQConn.class.getName()).log(Level.SEVERE, "Hello", new Exception());
//
//    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
        stop();
    }
}
