/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messagequeueframework;

import MQUtil.MQConn;

/**
 *
 * @author Reinfo
 */
public class MessageQueueFramework {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        MQConn c1 = new MQConn("q1");
        c1.StartListen();
        MQConn c2 = new MQConn("q1");
        c2.SendMessage("Good");
    }
    
}
