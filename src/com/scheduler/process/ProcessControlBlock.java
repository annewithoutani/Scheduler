package com.scheduler.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/// Bloco Controlador do Processo
/// Armazena informações para devolver o processo a CPU
public class ProcessControlBlock {
    private static int nextPID = 1;
    public final int PID;
    public final String name;
    /// Program Counter
    int PC;
    /// Estado do processo (Ready, Exec, Block)
    ProcessState state;
    int waitTime;

    /// Registradores comuns
    int X;
    int Y;
    /// Linhas do programa
    List<String> program;

    public ProcessControlBlock(String name, List<String> program) {
        this.PID = nextPID;
        nextPID += 1;
        this.name = name;
        this.program = program;
        this.PC = 0;
        this.X = 0;
        this.Y = 0;
        this.waitTime = 0;
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

    public void setWaitTime(int SleepTime) {
        this.waitTime = Math.max(SleepTime, 0);
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void decrementWaitTime() {
        this.waitTime = Math.max(0, this.waitTime - 1);
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
