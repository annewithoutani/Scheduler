package com.scheduler;

import com.scheduler.process.ProcessControlBlock;
import com.scheduler.process.ProcessState;

import java.util.*;
import java.io.*;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

public class Escalonador {
    private final int quantum;
    private int numQuanti;
    private int numInstructions;

    private final HashMap<Integer, ProcessControlBlock> processTable;
    private final Queue<Integer> ready; // Lista de processos prontos
    private final Queue<Integer> waiting; // Lista de processos bloqueados

    public Escalonador(int quantum) {
        this.quantum = quantum;
        this.numQuanti = 0;
        this.numInstructions = 0;
        this.processTable = new HashMap<>();
        ready = new LinkedList<>();
        waiting = new LinkedList<>();
    }

    public static void main(String[] args) {

        List<ProcessControlBlock> processes = new ArrayList<>();
        try {
            // Carrega os processos a partir dos arquivos.
            for (int i = 1; i <= 10; i++) {
                processes.add(ProcessControlBlock.fromFile(Path.of(String.format("programas/%02d.txt", i))));
            }
        } catch (IOException err) {
            System.err.println("Falha em carregar os arquivos de programas: " + err);
        }

        int quantum = 0;
        try (Scanner scanner = new Scanner(new File("programas/quantum.txt"))) {
            quantum = scanner.nextInt();
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo quantum.txt não foi encontrado.");
        }

        Escalonador sched = new Escalonador(quantum);
        sched.appendProcesses(processes);
        sched.run();
    }

    /// Adiciona uma lista de processos a tabela de processos. Todos os novos processos vão para a fila de prontos
    public void appendProcesses(List<ProcessControlBlock> processes) {
        Path logFilePath = Paths.get("testes", String.format("log%02d.txt", quantum));
        try (BufferedWriter logFile = Files.newBufferedWriter(logFilePath)) {
            for (ProcessControlBlock p : processes) {
                processTable.put(p.PID, p);
                ready.add(p.PID);
                // Adiciona mensagem de carregamento ao log
                logFile.write(String.format("Carregando %s\n", p.name));
            }
        } catch (IOException e) {
            System.err.println("Couldn't write to logfile: " + e.getMessage());
        }
    }

    /// Começa a executar o escalonador com sua lista de processos.
    public void run() {
        Path logFilePath = Paths.get("testes", String.format("log%02d.txt", quantum));
        try (BufferedWriter logFile = Files.newBufferedWriter(logFilePath, StandardOpenOption.APPEND)) {
            // Roda enquanto houver processos ativos
            outer: while (!ready.isEmpty() || !waiting.isEmpty()) {
                // decrementa a espera de todos os processos
                waiting.forEach((p) -> processTable.get(p).decrementWaitTime());
                // acorda o processo em espera
                if (waiting.peek() != null && processTable.get(waiting.peek()).getWaitTime() == 0) {
                    var proc = waiting.remove();
                    processTable.get(proc).setState(ProcessState.READY);
                    ready.add(proc);
                }
                // pega o processo executando atualmente (se existir)
                var executingPID = ready.poll();
                if (executingPID == null) {
                    continue;
                }
                var executingProcess = processTable.get(executingPID);
                executingProcess.setState(ProcessState.EXEC);
                executingProcess.interruptions += 1;
                numQuanti += 1;

                // Adiciona mensagem de execução ao log
                logFile.write(String.format("Executando %s\n", executingProcess.name));

                int i;
                for (i = 0; i < quantum; i++) {
                    numInstructions += 1;
                    // pega a instrução do processo executando
                    String instruction = executingProcess.fetchInstruction();
                    // executa a instrução
                    switch (instruction) {
                        // atribui o valor da instrução para os registradores X ou Y
                        case String s when s.matches("[XY]=[0-9]*") -> {
                            String[] parts = s.split("=", 2);
                            if (parts.length != 2) {
                                throw new RuntimeException("Instruction in wrong format: " + instruction);
                            }
                            if (parts[0].equals("X")) {
                                executingProcess.setX(Integer.parseInt(parts[1]));
                            } else if (parts[0].equals("Y")) {
                                executingProcess.setY(Integer.parseInt(parts[1]));
                            } else {
                                throw new RuntimeException("Instruction in wrong format: " + instruction);
                            }
                        }
                        case "COM" -> {
                            // Faz nada
                        }
                        case "E/S" -> {
                            // Coloca o processo para dormir por 2 quantum
                            executingProcess.setState(ProcessState.BLOCKING);
                            executingProcess.setWaitTime(2);
                            waiting.add(executingProcess.PID);
                            // Adiciona mensagem de interrupção ao log
                            logFile.write(String.format("E/S iniciada em %s\n", executingProcess.name));
                            String ins = i == 0 ? "instrução" : "instruções";
                            logFile.write(
                                    String.format("Interrompendo %s após %d %s\n", executingProcess.name, i + 1, ins));
                            // programa bloqueou, não devolve o processo atual para a fila de prontos
                            continue outer;
                        }
                        case "SAIDA" -> {
                            // Adiciona mensagem de finalização ao log
                            logFile.write(String.format("%s terminado. X=%d. Y=%d\n", executingProcess.name,
                                    executingProcess.getX(), executingProcess.getY()));
                            // Programa acabou, não devolve o processo atual para a fila de prontos
                            continue outer;
                        }
                        default -> throw new RuntimeException("Invalid instruction: " + instruction);
                    }
                }

                // Adiciona o processo em execução de volta a fila de prontos
                executingProcess.setState(ProcessState.READY);
                ready.add(executingProcess.PID);
                // Adiciona monsagem de interrupção ao log
                logFile.write(String.format("E/S iniciada em %s\n", executingProcess.name));
                String ins = i == 1 ?  "instrução" : "instruções";
                logFile.write(String.format("Interrompendo %s após %d %s\n", executingProcess.name, i, ins));
            }
            // Exibindo as médias de interrupções e instruções por quantum, além do valor do quantum
            int numProcesses = 0;
            int numInterruptions = 0;
            for (ProcessControlBlock p : processTable.values()) {
                numProcesses++;
                numInterruptions += p.interruptions;
            }
            logFile.write(String.format("MEDIA DE INTERRUPCOES: %.2f\n", (float)numInterruptions/numProcesses));
            logFile.write(String.format("MEDIA DE INSTRUCOES: %.2f\n", (float)numInstructions/numQuanti));
            logFile.write(String.format("QUANTUM: %d\n", quantum));
        } catch (IOException e) {
            System.out.println("Couldn't write to logfile: " + e.getMessage());
        }
    }
}
