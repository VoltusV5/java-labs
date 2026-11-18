package lab5.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lab5.io.CoffeeMachineFileReader;
import lab5.io.CsvJournal;
import lab5.io.MachineFiles;
import lab5.model.CoffeeMachine;
import lab5.view.ConsoleView;

public final class CommandController {
    /*
    Разбирает команду запуска и управляет выбранным режимом.
    */
    private final ConsoleView view;
    private final CoffeeMachineFileReader reader = new CoffeeMachineFileReader();

    public CommandController(ConsoleView view) {
        /*
        Создаёт контроллер с представлением для вывода результатов и ошибок.
        */
        this.view = view;
    }

    public int run(String[] args) {
        /*
        Проверяет команду и запускает выбранный режим.

        Returns:
            0 при успешном завершении, 1 при ошибке доступа к файлу, 2 при неверном вызове.
        */
        String command = args.length == 0 ? "--help" : args[0];
        int expected = switch (command) {
            case "--help", "help", "--interactive" -> 0;
            case "--show" -> 1;
            case "--replace", "--watch" -> 2;
            case "--csv" -> 3;
            default -> -1;
        };
        if (expected < 0) {
            view.error("Неизвестный ключ: " + command + ". Используйте --help.");
            return 2;
        }
        if (args.length > 0 && args.length != expected + 1) {
            view.error("Неверное количество аргументов для " + command + ". Используйте --help.");
            return 2;
        }

        try {
            switch (command) {
                case "--help", "help" -> view.help();
                case "--interactive" -> new InteractiveController(view).run();
                case "--show" -> view.showSnapshot(reader.read(filePath(args[1]), true).machines());
                case "--replace" -> {
                    MachineFiles.replace(filePath(args[1]), filePath(args[2]));
                    view.line("Файл заменён: " + args[2]);
                }
                case "--watch" -> monitor(filePath(args[1]), period(args[2]), null);
                case "--csv" -> {
                    Path source = filePath(args[1]);
                    Path output = filePath(args[2]);
                    int seconds = period(args[3]);
                    MachineFiles.requireDifferent(source, output);
                    try (CsvJournal journal = new CsvJournal(output)) {
                        view.line("Журнал: " + output);
                        monitor(source, seconds, journal);
                    }
                }
            }
            return 0;
        } catch (IllegalArgumentException e) {
            view.error(e.getMessage() + " Используйте --help.");
            return 2;
        } catch (IOException | SecurityException e) {
            view.error(e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    private void monitor(Path path, int seconds, CsvJournal journal) throws IOException, InterruptedException {
        /*
        Сразу читает файл и повторяет чтение через заданный интервал до остановки.

        Args:
            path: путь к файлу с кофемашинами.
            seconds: пауза между чтениями в секундах.
            journal: журнал CSV или null для вывода в консоль.
        */
        view.line("Чтение " + path + " каждые " + seconds + " с. Для остановки нажмите Ctrl+C.");
        while (!Thread.currentThread().isInterrupted()) {
            List<CoffeeMachine> machines = readForMonitoring(path);
            if (machines != null) {
                Instant observedAt = Instant.now();
                if (journal == null) {
                    view.line(observedAt);
                    view.showSnapshot(machines);
                } else {
                    journal.record(observedAt, machines);
                }
            }
            TimeUnit.SECONDS.sleep(seconds);
        }
    }

    private List<CoffeeMachine> readForMonitoring(Path path) {
        /*
        Читает состояние для очередного цикла наблюдения.

        Returns:
            Список кофемашин или null после сообщения об ошибке чтения.
        */
        try {
            return reader.read(path, true).machines();
        } catch (IOException | SecurityException e) {
            view.error("Не удалось прочитать " + path + ": " + e.getMessage());
            return null;
        }
    }

    private Path filePath(String argument) {
        /*
        Преобразует непустой аргумент команды в путь к файлу.
        */
        if (argument.isBlank()) {
            throw new IllegalArgumentException("Путь к файлу не должен быть пустым.");
        }
        return Path.of(argument);
    }

    private int period(String argument) {
        /*
        Преобразует аргумент команды в положительный период в секундах.
        */
        try {
            int seconds = Integer.parseInt(argument);
            if (seconds > 0) {
                return seconds;
            }
        } catch (NumberFormatException e) {
            // Некорректное число и выход за предел int.
        }
        throw new IllegalArgumentException("Период должен быть целым числом секунд от 1 до " + Integer.MAX_VALUE + ".");
    }
}
