#include <stdio.h> 
#include "bin/jni/cs3410_project_filesystem_Main.h"

JNIEXPORT void JNICALL Java_cs3410_project_filesystem_Main_registerShellExtensionHandler(JNIEnv *env, jclass cls) {
    jmethodID setShellExtensionStatus = (*env)->GetStaticMethodID(env, cls, "setShellExtensionStatus", "(I)V");

    (*env)->CallStaticVoidMethod(env, cls, setShellExtensionStatus, 0);
}
