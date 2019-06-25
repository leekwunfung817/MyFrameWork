/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Detector;

//import org.apache.commons.net.ftp.FTPClient;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 *
 * @author Reinfo
 */
public class SFTPDetector {

    public static void main(String[] args) {
        FTPClient client = new FTPClient();
        try {
            client.connect("ftp.javacodegeeks.com");
            boolean login = client.login("username", "password");
            if (login) {
                System.out.println("Connection established...");
                boolean logout = client.logout();
                if (logout) {
                    System.out.println("Connection close...");
                }
            } else {
                System.out.println("Connection fail...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
