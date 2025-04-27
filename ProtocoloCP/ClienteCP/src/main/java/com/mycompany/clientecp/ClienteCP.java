package com.mycompany.clientecp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClienteCP {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Socket socket = new Socket(args[0], 60000);
            System.out.println("Conectado ao servidor Calculadora...");
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            while (true) {
                System.out.print("> ");
                String requisicao = scanner.nextLine();
                if (requisicao.trim().isEmpty()) {
                    continue;
                }
                output.writeObject(requisicao);
                String resposta = (String) input.readObject();
                if (!resposta.equals("OK")) {
                    System.out.println(resposta);
                }
                if (requisicao.equals("EXIT")) {
                    socket.close();
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
