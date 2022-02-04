#include <stdio.h> 
#include "bin/jni/cs3410_project_filesystem_Main.h"

#define SHELL_EXTENSION_REGISTERED 0
#define SHELL_EXTENSION_ALREADY_REGISTERED 1
#define SHELL_EXTENSION_DEREGISTERED 2

JNIEXPORT void JNICALL Java_cs3410_project_filesystem_Main_registerShellExtensionHandler(JNIEnv *env, jclass cls) {
    jmethodID setShellExtensionStatus = env->GetStaticMethodID(cls, "setShellExtensionStatus", "(I)V");

    env->CallStaticVoidMethod(cls, setShellExtensionStatus, SHELL_EXTENSION_REGISTERED);
}
