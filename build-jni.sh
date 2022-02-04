#!/bin/bash
gcc -c -I"${JAVA_HOME%}\include" -I"${JAVA_HOME}\include\win32" native.c -o native.o
gcc -shared -o native.dll native.o -Wall
