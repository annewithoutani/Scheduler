package com.scheduler;

import com.scheduler.process.ProcessControlBlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
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

        Scheduler sched = new Scheduler(quantum);
        sched.appendProcesses(processes);

        sched.run();
    }
}
