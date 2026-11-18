package lab5;

import lab5.controller.CommandController;
import lab5.view.ConsoleView;

public final class Main {
    /*
    Точка входа в консольное приложение лабораторной работы.
    */
    public static void main(String[] args) {
        /*
        Передаёт аргументы контроллеру и возвращает его код ошибки при неуспешном запуске.

        Args:
            args: ключ режима и его параметры.
        */
        int status = new CommandController(new ConsoleView()).run(args);
        if (status != 0) {
            System.exit(status);
        }
    }
}
