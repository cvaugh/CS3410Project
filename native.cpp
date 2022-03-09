#include <stdio.h> 
#include <windows.h>
#include "bin/jni/cs3410_project_filesystem_Main.h"

#define SHELL_EXTENSION_REGISTERED 0
#define SHELL_EXTENSION_ALREADY_REGISTERED 1
#define SHELL_EXTENSION_DEREGISTERED 2
#define SHELL_EXTENSION_REGISTRATION_FAILED 3
#define SHELL_EXTENSION_DEREGISTRATION_FAILED 4

#define FS_EXT ".fs"
#define FS_PROGID "CS3410Project.fs"
#define FS_PROGID_SZ "CS3410Project.fs\0"

#define MIME_TYPE "application/vnd.filesystem\0"

// Credit to Josh Patterson "NTDLS" on GitHub
// https://github.com/NTDLS/NSWFL/blob/master/NSWFL_Registry.Cpp
// NSWFL, licensed under LGPL v3
bool CreateRegistryKey(HKEY hKeyRoot, LPCTSTR pszSubKey) {
    HKEY hKey;
    DWORD dwFunc;
    LONG lRet;

    SECURITY_DESCRIPTOR SD;
    SECURITY_ATTRIBUTES SA;


    if(!InitializeSecurityDescriptor(&SD, SECURITY_DESCRIPTOR_REVISION)) return false;


    if(!SetSecurityDescriptorDacl(&SD, true, 0, false)) return false;

    SA.nLength = sizeof(SA);
    SA.lpSecurityDescriptor = &SD;
    SA.bInheritHandle = false;

    lRet = RegCreateKeyEx(
        hKeyRoot,
        pszSubKey,
        0,
        (LPTSTR) NULL,
        REG_OPTION_NON_VOLATILE,
        KEY_WRITE,
        &SA,
        &hKey,
        &dwFunc
        );

    if(lRet == ERROR_SUCCESS) {
        RegCloseKey(hKey);
        hKey = (HKEY)NULL;
        return true;
    }

    SetLastError((DWORD)lRet);
    return false;
}

bool Set_StringRegistryValue(HKEY hKeyRoot, LPCTSTR pszSubKey, LPCTSTR pszValue, LPCTSTR pszString) {
    HKEY  hKey;
    LONG  lRes;
    DWORD dwSize = lstrlen(pszString) * sizeof(TCHAR);

    lRes = RegOpenKeyEx(hKeyRoot, pszSubKey, 0, KEY_WRITE, &hKey);

    if(lRes != ERROR_SUCCESS) {
        SetLastError(lRes);
        return false;
    }

    lRes = RegSetValueEx(hKey, pszValue, 0, REG_SZ, (unsigned char*) pszString, dwSize);

    RegCloseKey(hKey);

    if(lRes != ERROR_SUCCESS) {
        SetLastError(lRes);
        return false;
    }

    return true;
}

void setKey();

void RegisterKeys() {
    Set_StringRegistryValue(HKEY_CLASSES_ROOT, FS_EXT, "", FS_PROGID_SZ);
    Set_StringRegistryValue(HKEY_CLASSES_ROOT, FS_EXT, "Content Type", MIME_TYPE);
    Set_StringRegistryValue(HKEY_CLASSES_ROOT, FS_EXT, "PerceivedType", "compressed\0");
    CreateRegistryKey(HKEY_CLASSES_ROOT, FS_PROGID);
    Set_StringRegistryValue(HKEY_CLASSES_ROOT, FS_PROGID, "", "File System Container\0");
    TCHAR iconPath[MAX_PATH];
    GetCurrentDirectory(MAX_PATH, iconPath);
    strcat(iconPath, "\\tree.ico\0");
    HKEY fsKey;
    RegOpenKeyEx(HKEY_CLASSES_ROOT, FS_PROGID, 0, KEY_WRITE, &fsKey);
    CreateRegistryKey(fsKey, "DefaultIcon");
    Set_StringRegistryValue(fsKey, "DefaultIcon", "", iconPath);
    
    RegOpenKeyEx(HKEY_CLASSES_ROOT, "Applications", 0, KEY_WRITE, &fsKey);
    CreateRegistryKey(fsKey, "CS3410Project\\shell\\open\\command");
    RegOpenKeyEx(fsKey, "CS3410Project\\shell\\open", 0, KEY_WRITE, &fsKey);
    Set_StringRegistryValue(fsKey, "command", "", "\"java -jar DS_Project.jar -b\" %1");
    
    RegOpenKeyEx(HKEY_CURRENT_USER, "Software", 0, KEY_WRITE, &fsKey);
    CreateRegistryKey(fsKey, "Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExt\\.fs\\UserChoice");
    RegOpenKeyEx(HKEY_CURRENT_USER, "Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExt\\.fs", 0, KEY_WRITE, &fsKey);
    Set_StringRegistryValue(fsKey, "UserChoice", "ProgId", "Applications\\CS3410Project");
}

JNIEXPORT void JNICALL Java_cs3410_project_filesystem_Main_registerShellExtensionHandler(JNIEnv *env, jclass cls) {
    jmethodID setShellExtensionStatus = env->GetStaticMethodID(cls, "setShellExtensionStatus", "(I)V");
    
    int r = -1;
    HKEY hKey;
    long exists = RegOpenKeyEx(HKEY_CLASSES_ROOT, FS_EXT, 0, KEY_READ, &hKey);
    if(exists == ERROR_SUCCESS) {
        r = SHELL_EXTENSION_ALREADY_REGISTERED;
    } else {
        if(CreateRegistryKey(HKEY_CLASSES_ROOT, FS_EXT)) {
            RegisterKeys();
            r = SHELL_EXTENSION_REGISTERED;
        } else {
            r = SHELL_EXTENSION_REGISTRATION_FAILED;
        }
    }

    env->CallStaticVoidMethod(cls, setShellExtensionStatus, r);
}
