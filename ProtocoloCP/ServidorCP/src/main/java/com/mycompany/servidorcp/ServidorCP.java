package com.mycompany.servidorcp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServidorCP {
    public static void main(String[] args) {
        System.out.println("Servidor Caluladora!");
        try {
            ServerSocket servidor = new ServerSocket(60000);
            while (true) {
                Socket socket = servidor.accept();
                Thread t = new Thread(new Cliente(socket));
                t.start();
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
