package Controller;

/**
 *
 * @author Reinfo
 */
import MQUtil.MQConn;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EMailController {

    public static String join(String separator, String... args) {
        String result = "";
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result += separator;
            }
            result += args[i];
        }
        return result;
    }
    final static String mess_separator = "<EM>";

    public static void EmailMQServer() throws Exception {

        MQConn email_server = new MQConn("email") {
            @Override
            protected void OnMessage(String mq_name, String session, String text) {
                try {

                    System.out.println(text);
                    String[] infos = text.split(mess_separator);
                    send_email(infos[0], infos[1], infos[2], infos[3], infos[4], infos[5], infos[6], Integer.parseInt(infos[7]), infos[8], infos[9]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        email_server.StartListen();
    }

    public static void EmailMQClient() throws Exception {

        MQConn email_client = new MQConn("email");
        String FROM = "leekwunfung817@gmail.com";
        String FROMNAME = "Ivan Lee";
        String TO = "leekwunfung817@gmail.com";
        String SMTP_USERNAME = "leekwunfung817@gmail.com";
        String SMTP_PASSWORD = "whbdxpbhldcwoume";
        String CONFIGSET = "ConfigSet";
        String HOST = "smtp.gmail.com";
        int PORT = 587;
        String SUBJECT = "SFTP server dead";
        String BODY = join(
                System.getProperty("line.separator"),
                "<h1>Fail to connect (LPR_3) SFTP server</h1>",
                "<p>Restarting FreeSSHD service.</p>."
        );
        email_client.SendMessage(
                join(
                        mess_separator,
                        FROM,
                        FROMNAME,
                        TO,
                        SMTP_USERNAME,
                        SMTP_PASSWORD,
                        CONFIGSET,
                        HOST,
                        PORT + "",
                        SUBJECT,
                        BODY
                )
        );
    }

    public static void main(String[] args) throws Exception {
        EmailMQClient();
    }

    public static void send_email(
            String FROM,
            String FROMNAME,
            String TO,
            String SMTP_USERNAME,
            String SMTP_PASSWORD,
            String CONFIGSET,
            String HOST,
            int PORT,
            String SUBJECT,
            String BODY) {

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();

        props.put(
                "mail.transport.protocol", "smtp");
        props.put(
                "mail.smtp.port", PORT);
        props.put(
                "mail.smtp.starttls.enable", "true");
        props.put(
                "mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties. 
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
        // Create a transport.
        Transport transport = null;
        try {
            transport = session.getTransport();
            msg.setFrom(
                    new InternetAddress(FROM, FROMNAME));
            msg.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(TO));
            msg.setSubject(SUBJECT);

            msg.setContent(BODY,
                    "text/html");

            // Add a configuration set header. Comment or delete the 
            // next line if you are not using a configuration set
            msg.setHeader(
                    "X-SES-CONFIGURATION-SET", CONFIGSET);

            // Send the message.
            System.out.println("Sending...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            if (transport != null) {
                if (transport.isConnected()) {

                    try {
                        // Close and terminate the connection.
                        transport.close();
                    } catch (MessagingException ex) {
                        Logger.getLogger(EMailController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }
    }
}
