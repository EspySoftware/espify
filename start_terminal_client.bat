@echo off
REM Force ANSI support and terminal type
set SPRING_SHELL_TERMINAL_TYPE=xterm-256color
set SPRING_OUTPUT_ANSI_ENABLED=always
set SPRING_SHELL_JANSI_ENABLED=true

REM Navigate to the target directory
cd /d "C:/Users/Fernando/Documents/GitHub/espify/target"

REM Launch with proper terminal configuration
java -Dspring.shell.terminal.type=xterm-256color -jar espify-1.0.jar