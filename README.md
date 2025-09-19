# Escalador de Processos

Esse projeto faz parte de um Exercício de Programação da matéria de Sistemas Operacionais.

## Objetivos

Dado um conjunto de programas e um _quantum_, simular um escalonador de processos com algoritmo _Round Robin_. Gerar logs para cada ação o escalonador e por fim uma média das ações feitas pelo sistema.

## Requisitos
É necessário ter as seguintes dependências instaladas para rodar o projeto.
1. Java VM +22
2. Git
3. Make

### Como rodar

Clone este repositório.
```shell
git clone https://github.com/caio-bernardo/Scheduler
```

Na pasta do projeto, execute o comando `make` para compilar o código. Ou vá direto para o comando `make run` para executá-lo.

## Tarefas
- [x] Ler os arquivos e criar os processos
- [ ] Rodar os processos
- [ ] Criar o sistema de interrupção, com mudança de contexto (Round Robin)
- [ ] Logs do escalonador
- [ ] Calcular média de ações (ver o pdf)
- [ ] Armazenar em um arquivo saida.txt