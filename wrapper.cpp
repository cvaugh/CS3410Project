#include <windows.h>

int main(int argc, char *argv[]) {
    ShellExecute(0, "open", "cmd.exe", "/C javaw -jar DS_Project.jar -b", 0, SW_HIDE);
    return 0;
}
