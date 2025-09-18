package com.scheduler.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/// Bloco Controlador do Processo
/// Armazena informações para devolver o processo a CPU
public class ProcessControlBlock {
    /// Program Counter
    int PC;
    /// Estado do processo (Ready, Exec, Block)
    ProcessState state;
    /// Registradores comuns
    int X;
    int Y;
    /// Linhas do programa
    List<String> program;

    public ProcessControlBlock(List<String> program) {
        this.program = program;
        this.PC = 0;
        this.X = 0;
        this.Y = 0;
        this.state = ProcessState.READY;
    }
}


