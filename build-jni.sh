#!/bin/bash
gcc -c -I"${JAVA_HOME%}\include" -I"${JAVA_HOME}\include\win32" cs3410_project_filesystem_Main.cpp -o cs3410_project_filesystem_Main.o
gcc -shared -o native.dll cs3410_project_filesystem_Main.o -Wall
