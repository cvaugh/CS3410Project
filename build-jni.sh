#!/bin/bash
g++ -c -I"${JAVA_HOME%}\include" -I"${JAVA_HOME}\include\win32" native.cpp -o native.o
g++ -shared -o native.dll native.o -Wall
