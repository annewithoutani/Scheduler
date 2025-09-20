package com.scheduler;

import com.scheduler.process.ProcessControlBlock;
import com.scheduler.process.ProcessState;

import java.util.*;

public class Scheduler {
    private final int quantum;

    // TODO: criar tabela de processos, onde todos os processos são armazenados e gerenciar de acordo.
    private final Queue<ProcessControlBlock> ready;
    private final Queue<ProcessControlBlock> waiting;

    public Scheduler(int quantum) {
        this.quantum = quantum;
        ready = new LinkedList<>();
        waiting = new LinkedList<>();
    }

    /// Adiciona uma lista de processos a tabela de processos. Todos os novos processos vão para a fila de prontos
    public void appendProcesses(List<ProcessControlBlock> processes) {
        ready.addAll(processes);
    }

    /// Começa a executar o escalonador com sua lista de processos.
    public void run() {
        // Roda enquanto houver processos ativos
        outer: while (!ready.isEmpty() && !waiting.isEmpty()) {
            // decrementa o sono de todos os processos
            waiting.forEach(ProcessControlBlock::decrementSleepTime);
            // acorda o processo dormindo
            if (waiting.peek() != null && waiting.peek().getSleepTime() == 0) {
                var proc = waiting.remove();
                proc.setState(ProcessState.READY);
                ready.add(proc);
            }
            // pega o processo executando atualmente (se existir)
            var executingProcess = ready.poll();
            if (executingProcess == null) {
                continue;
            }

            executingProcess.setState(ProcessState.EXEC);

            for (int i = 0; i < quantum; i++) {
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
                        executingProcess.setSleepTime(2);
                        waiting.add(executingProcess);
                        // programa bloqueou, não devolve o processo atual para a fila de prontos
                        continue outer;
                    }
                    case "SAIDA" -> {
                        // programa acabou, não devolve o processo atual para a fila de prontos
                        continue outer;
                    }
                    default -> throw new RuntimeException("Invalid instruction: " + instruction);
                }
            }

            //  adiciona o processo em execução de volta a fila de prontos
            executingProcess.setState(ProcessState.READY);
            ready.add(executingProcess);
        }
    }
}
