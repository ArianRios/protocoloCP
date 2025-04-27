package com.mycompany.servidorcp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Cliente implements Runnable {

    private final Socket socket;

    public Cliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            String requisicao, resposta;
            do {
                requisicao = (String) input.readObject();
                System.out.println(requisicao);
                resposta = getRespostaCP(requisicao);
                output.writeObject(resposta);
            } while (!requisicao.equals("EXIT"));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private String getRespostaCP(String requisicao) {
        if (requisicao.equals("EXIT")) {
            return "BYE";
        } else if (requisicao.startsWith("SUM ")) {
            return calcular(requisicao, "SUM");
        } else if (requisicao.startsWith("SUB ")) {
            return calcular(requisicao, "SUB");
        } else if (requisicao.startsWith("MUL ")) {
            return calcular(requisicao, "MUL");
        } else if (requisicao.startsWith("DIV ")) {
            return calcular(requisicao, "DIV");
        } else {
            return "ERR";
        }
    }

    private String calcular(String requisicao, String operacion) {
        try {
            String[] partes = requisicao.substring(4).trim().split("\\s+");
            if (partes.length != 2) return "ERR";
            double a = Double.parseDouble(partes[0]);
            double b = Double.parseDouble(partes[1]);
            double resultado;
            switch (operacion) {
                case "SUM": resultado = a + b; break;
                case "SUB": resultado = a - b; break;
                case "MUL": resultado = a * b; break;
                case "DIV":
                    if (b == 0) return "ERR";
                    resultado = a / b; break;
                default: return "ERR";
            }
            return "RESULT " + resultado;
        } catch (Exception e) {
            return "ERR";
        }
    }

}
