package com.scheduler.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/// Bloco Controlador do Processo
/// Armazena informações para devolver o processo a CPU
public class ProcessControlBlock {
    String name;
    /// Program Counter
    int PC;
    /// Estado do processo (Ready, Exec, Block)
    ProcessState state;
    int SleepTime;

    /// Registradores comuns
    int X;
    int Y;
    /// Linhas do programa
    List<String> program;

    public ProcessControlBlock(String name, List<String> program) {
        this.name = name;
        this.program = program;
        this.PC = 0;
        this.X = 0;
        this.Y = 0;
        this.SleepTime = 0;
        this.state = ProcessState.READY;
    }

    /// Cria um PCB a partir de um arquivo de instruções.
    public static ProcessControlBlock fromFile(Path filePath) throws IOException {
        var lines = Files.readAllLines(filePath);
        var procName = lines.removeFirst();
        return new ProcessControlBlock(procName, lines);
    }

    public String fetchInstruction() {
        var inst = program.get(PC);
        PC = Math.min(PC + 1, program.size());
        return inst;
    }


    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void setSleepTime(int SleepTime) {
        this.SleepTime = Math.max(SleepTime, 0);
    }


    public int getSleepTime() {
        return this.SleepTime;
    }

    public void decrementSleepTime() {
        this.SleepTime = Math.max(0, this.SleepTime - 1);
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }
}


