package Controller;

import MQUtil.MQConn;
import java.util.Map;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Reinfo
 */
public class SQLController {

    public static void main(String[] args) throws Exception {
        server_block();
//        client_block();

//        c1.stop();
    }

    private static void client_block() throws Exception {
        MQConn sql_client = new MQConn("sql") {
            @Override
            protected void OnReturnMessage(String mq_name, String text) {
                System.out.println("returned:" + text);
            }
        };
        sql_client.SendMessage("SELECT * FROM Goods");
    }

    private static void server_block() throws Exception {
        MQConn sql_server = new MQConn("sql") {
            @Override
            protected void OnMessage(String mq_name, String session, String text) {
                System.out.println(mq_name + session + text);
//                SendReturnMessage("done");
            }
        };

        sql_server.StartListen();
//        sql_server.StartListenReturn
//        ();

        sql_server.SendMessage("Hello");
        sql_server.SendMessage("Hey");
    }

    public static TreeMap<String, String> sys_parm_decode(String args) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        String[] arr = args.split(",");
        for (String ele : arr) {
            String[] arr_ele = ele.split(":");
            String key = arr_ele[0];
            String value = arr_ele[1];
            treeMap.put(key, value);
        }
        return treeMap;
    }

    public static String sys_parm_encode(TreeMap<String, String> args) {
        String output = "";
        boolean begin = false;
        for (Map.Entry<String, String> entrySet : args.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            if (begin) {
                output = ",";
            } else {
                begin = true;
            }
            output = "" + key + ":" + value;
        }
        return output;
    }
}
