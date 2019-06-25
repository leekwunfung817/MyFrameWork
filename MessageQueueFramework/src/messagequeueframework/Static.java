/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messagequeueframework;

import org.apache.activemq.ActiveMQConnection;

/**
 *
 * @author Reinfo
 */
public class Static {
    static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    static String subject = "JCG_QUEUE"; // Queue Name.You can create any/many queue names as per your requirement. 
}
