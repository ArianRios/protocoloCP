package com.mycompany.servidorcp;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServidorCP {
    private static final int PORTA = 60000;
    private static final List<Thread> clientesAtivos = new ArrayList<>();
    private static final AtomicBoolean executando = new AtomicBoolean(true);
    
    public static void main(String[] args) {
        // Informações do servidor
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║       SERVIDOR CALCULADORA       ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║ Porta: " + PORTA + "                     ║");
        System.out.println("╚══════════════════════════════════╝");
        
        // Hook para encerramento limpo do servidor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nEncerrando o servidor...");
            executando.set(false);
        }));
        
        ServerSocket servidor = null;
        try {
            // Criar o socket do servidor
            servidor = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado em " + 
                              InetAddress.getLocalHost().getHostAddress() + 
                              ":" + PORTA);
            System.out.println("Aguardando conexões... (Ctrl+C para encerrar)");
            
            // Loop principal do servidor
            while (executando.get()) {
                // Aceitar nova conexão
                Socket socket = servidor.accept();
                
                // Criar e iniciar uma thread para o cliente
                Thread clienteThread = new Thread(new Cliente(socket));
                clientesAtivos.add(clienteThread);
                clienteThread.start();
                
                // Mensagem de conexão do cliente
                System.out.println("Cliente conectado de: " + 
                                  socket.getInetAddress().getHostAddress() + 
                                  ":" + socket.getPort());
                
                // Limpar threads de clientes inativos
                limparClientesInativos();
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fechar o socket do servidor ao finalizar
            if (servidor != null && !servidor.isClosed()) {
                try {
                    servidor.close();
                    System.out.println("Servidor encerrado.");
                } catch (Exception e) {
                    System.out.println("Erro ao fechar o servidor: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Remove da lista as threads de clientes que já terminaram
     */
    private static void limparClientesInativos() {
        clientesAtivos.removeIf(thread -> !thread.isAlive());
        System.out.println("Clientes ativos: " + clientesAtivos.size());
    }
}
