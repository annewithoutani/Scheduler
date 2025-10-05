package com.scheduler.process;


// Estado de um Processo
// Usado para determinar em qual fila o processo se encontra
public enum ProcessState {
    READY,
    EXEC,
    BLOCKING
}
