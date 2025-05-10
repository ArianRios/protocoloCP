package com.mycompany.servidorcp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Cliente implements Runnable {

    private final Socket socket;
    private final List<String> historial = new ArrayList<>();
    private int precision = 4; // Valor por defecto para SET PRECISION

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
                // Solo agrega al historial operaciones exitosas entre números
                if (
                    !requisicao.equals("EXIT") &&
                    !requisicao.equals("HIST") &&
                    !requisicao.equals("HELP") &&
                    !resposta.startsWith("ERR") &&
                    !resposta.startsWith("HIST") &&
                    !resposta.startsWith("COMANDOS DISPONÍVEIS")
                ) {
                    historial.add(requisicao + " = " + resposta);
                }
            } while (!requisicao.equals("EXIT"));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private String getRespostaCP(String requisicao) {
        if (requisicao.equals("EXIT")) {
            return "BYE";
        } else if (requisicao.equals("HIST")) {
            return getHistorial();
        } else if (requisicao.equals("HELP")) {
            return "COMANDOS DISPONÍVEIS:\n"
                 + "SUM a b         - Soma dois números (a + b)\n"
                 + "SUB a b         - Subtrai o segundo número do primeiro (a - b)\n"
                 + "MUL a b         - Multiplica dois números (a * b)\n"
                 + "DIV a b         - Divide o primeiro pelo segundo (a / b)\n"
                 + "POW a b         - Potência (a elevado a b)\n"
                 + "SQRT a          - Raiz quadrada de a\n"
                 + "SIN a           - Seno de a (em radianos)\n"
                 + "COS a           - Cosseno de a (em radianos)\n"
                 + "TAN a           - Tangente de a (em radianos)\n"
                 + "LOG a           - Logaritmo decimal de a\n"
                 + "EVAL expr       - Avalia uma expressão matemática (ex: EVAL 3+4*2)\n"
                 + "SET PRECISION n - Altera o número de casas decimais exibidas (ex: SET PRECISION 4)\n"
                 + "HIST            - Mostra o histórico das operações da sessão\n"
                 + "HELP            - Mostra esta mensagem de ajuda\n"
                 + "EXIT            - Encerra a sessão";
        } else if (requisicao.startsWith("SET PRECISION ")) {
            return configurarPrecision(requisicao);
        } else if (requisicao.startsWith("SUM ")) {
            return calcular(requisicao, "SUM");
        } else if (requisicao.startsWith("SUB ")) {
            return calcular(requisicao, "SUB");
        } else if (requisicao.startsWith("MUL ")) {
            return calcular(requisicao, "MUL");
        } else if (requisicao.startsWith("DIV ")) {
            return calcular(requisicao, "DIV");
        } else if (requisicao.startsWith("POW ")) {
            return calcular(requisicao, "POW");
        } else if (requisicao.startsWith("SQRT ")) {
            return calcularUnario(requisicao, "SQRT");
        } else if (requisicao.startsWith("SIN ")) {
            return calcularUnario(requisicao, "SIN");
        } else if (requisicao.startsWith("COS ")) {
            return calcularUnario(requisicao, "COS");
        } else if (requisicao.startsWith("TAN ")) {
            return calcularUnario(requisicao, "TAN");
        } else if (requisicao.startsWith("LOG ")) {
            return calcularUnario(requisicao, "LOG");
        } else if (requisicao.startsWith("EVAL ")) {
            return evaluarExpresion(requisicao);
        } else {
            return "ERR";
        }
    }

    private String configurarPrecision(String requisicao) {
        try {
            String strPrecision = requisicao.substring(14).trim();
            int novaPrecision = Integer.parseInt(strPrecision);
            if (novaPrecision < 0) {
                return "ERR: A precisão deve ser um número não negativo";
            }
            this.precision = novaPrecision;
            return "Precisão configurada para " + precision + " casas decimais";
        } catch (NumberFormatException e) {
            return "ERR: Formato de precisão inválido";
        }
    }

    private String calcular(String requisicao, String operacion) {
        try {
            String[] partes = requisicao.substring(4).trim().split("\\s+");
            if (partes.length != 2) return "ERR-FORMAT: São necessários exatamente dois números";
            double a = Double.parseDouble(partes[0]);
            double b = Double.parseDouble(partes[1]);
            double resultado;
            switch (operacion) {
                case "SUM": resultado = a + b; break;
                case "SUB": resultado = a - b; break;
                case "MUL": resultado = a * b; break;
                case "DIV":
                    if (b == 0) return "ERR-MATH: Divisão por zero";
                    resultado = a / b; break;
                case "POW": resultado = Math.pow(a, b); break;
                default: return "ERR";
            }
            return formatearResultado(resultado);
        } catch (NumberFormatException e) {
            return "ERR-NUM: Formato de número inválido";
        } catch (Exception e) {
            return "ERR: " + e.getMessage();
        }
    }
    
    private String calcularUnario(String requisicao, String operacion) {
        try {
            String valor = requisicao.substring(operacion.length()).trim();
            double a = Double.parseDouble(valor);
            double resultado;
            switch (operacion) {
                case "SQRT":
                    if (a < 0) return "ERR-MATH: Não é possível calcular a raiz de um número negativo";
                    resultado = Math.sqrt(a); break;
                case "SIN": resultado = Math.sin(a); break;
                case "COS": resultado = Math.cos(a); break;
                case "TAN": resultado = Math.tan(a); break;
                case "LOG":
                    if (a <= 0) return "ERR-MATH: Logaritmo não definido para valores <= 0";
                    resultado = Math.log10(a); break;
                default: return "ERR";
            }
            return formatearResultado(resultado);
        } catch (NumberFormatException e) {
            return "ERR-NUM: Formato de número inválido";
        } catch (Exception e) {
            return "ERR: " + e.getMessage();
        }
    }
    
    private String evaluarExpresion(String requisicao) {
        try {
            String expresion = requisicao.substring(5).trim();
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object resultado = engine.eval(expresion);
            if (resultado instanceof Number) {
                return formatearResultado(((Number) resultado).doubleValue());
            } else {
                return "RESULT: " + resultado.toString();
            }
        } catch (Exception e) {
            return "ERR-EXPR: " + e.getMessage();
        }
    }
    
    private String formatearResultado(double resultado) {
        if (resultado == (int) resultado) {
            return "RESULT: " + (int) resultado;
        } else {
            return String.format("RESULT: %." + precision + "f", resultado);
        }
    }
    
    private String getHistorial() {
        if (historial.isEmpty()) {
            return "HIST: Histórico vazio";
        }
        StringBuilder sb = new StringBuilder("HISTÓRICO:\n");
        int max = Math.min(historial.size(), 10); // Solo las últimas 10 operaciones
        for (int i = historial.size() - max; i < historial.size(); i++) {
            sb.append(i + 1).append(". ").append(historial.get(i)).append("\n");
        }
        return sb.toString();
    }
}
