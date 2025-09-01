# Maki Desktop

This directory contains a standalone desktop implementation of the Maki game
and a simple scoreboard server.

## Running the Game

Compile and run:

```bash
javac MakiDesktop.java
java MakiDesktop
```

## Running the Scoreboard Server

The server listens on TCP port 9800 and uses the same text protocol as the
original example:

```bash
javac ScoreServerDesktop.java
java ScoreServerDesktop
```

The server keeps scores in memory for the duration of the process.
