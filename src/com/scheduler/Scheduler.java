package com.scheduler;

import com.scheduler.process.ProcessControlBlock;
import com.scheduler.process.ProcessState;

import java.util.*;

public class Scheduler {
    private int quantum;
    private List<ProcessControlBlock> processTable;

    private Queue<Integer> ready;
    private Queue<Integer> waiting;

    private int executingProcessId;

    public Scheduler(int quantum) {
        this.quantum = quantum;
        processTable = new ArrayList<>();
        ready = new LinkedList<>();
        waiting = new LinkedList<>();
    }

    /// Adiciona um processo a tabela de processos. Todos os novos processos vão para a fila de prontos
    public void addProcess(ProcessControlBlock process) {
        processTable.add(process);
        ready.add(processTable.size() - 1);
    }

    /// Adiciona uma lista de processos a tabela de processos. Todos os novos processos vão para a fila de prontos
    public void appendProcesses(List<ProcessControlBlock> processes) {
        processTable.addAll(processes);
        // TODO: adicionar a lista a fila de prontos
    }

    /// Começa a executar o escalonador com sua lista de processos.
    public void run() {
        while (!ready.isEmpty() ||  !waiting.isEmpty()) {
            // pega o processo executando atualmente
            // acorda processos dormindo
            for (int i = 0; i < quantum; i++) {
                // pega a instrução do processo executando

                // executa a instrução
                // 1. [XY]=?. Atualiza o PCB
                // 2. COM faz nada
                // 3. E/S bloqueia o processo, manda para a fila de espera e encerra o loop maior antes
                // 4. SAIDA encerra o processo, retira da lista de processo e encerra o loop mais cedo
            }

            //  adiciona o processo em execução de volta a fila de prontos
        }
    }
}
