package com.scheduler;

import com.scheduler.process.ProcessControlBlock;
import com.scheduler.process.ProcessState;

import java.util.*;
import java.io.*;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

public class Scheduler {
    private final int quantum;

    private HashMap<Integer, ProcessControlBlock> processTable;
    private final Queue<Integer> ready; // Lista de processos prontos
    private final Queue<Integer> waiting; // Lista de processos bloqueados

    public Scheduler(int quantum) {
        this.quantum = quantum;
        this.processTable = new HashMap<>();
        ready = new LinkedList<>();
        waiting = new LinkedList<>();
    }

    /// Adiciona uma lista de processos a tabela de processos. Todos os novos processos vão para a fila de prontos
    public void appendProcesses(List<ProcessControlBlock> processes) {
        Path logFilePath = Paths.get(String.format("log%02d.txt", quantum));
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
        Path logFilePath = Paths.get(String.format("log%02d.txt", quantum));
        try (BufferedWriter logFile = Files.newBufferedWriter(logFilePath, StandardOpenOption.APPEND)) {
            // Roda enquanto houver processos ativos
            outer: while (!ready.isEmpty() || !waiting.isEmpty()) {
                // decrementa o sono de todos os processos
                waiting.forEach( (p) -> {processTable.get(p).decrementWaitTime();} );
                // acorda o processo dormindo
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
                // Adiciona mensagem de execução ao log
                logFile.write(String.format("Executando %s\n", executingProcess.name));
                
                int i;
                for (i = 0; i < quantum; i++) {
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
                            String ins = i == 0 ? new String("instrução") : new String("instruções");
                            logFile.write(String.format("Interrompendo %s após %d %s\n", executingProcess.name, i+1, ins));
                            // programa bloqueou, não devolve o processo atual para a fila de prontos
                            continue outer;
                        }
                        case "SAIDA" -> {
                            // Adiciona mensagem de finalização ao log
                            logFile.write(String.format("%s terminado. X=%d. Y=%d\n", executingProcess.name, executingProcess.getX(), executingProcess.getY()));
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
                String ins = i == 0 ? new String("instrução") : new String("instruções");
                logFile.write(String.format("Interrompendo %s após %d %s\n", executingProcess.name, i+1, ins));
            }
        } catch (IOException e) {
            System.out.println("Couldn't write to logfile: " + e.getMessage());
        }
    }
}
