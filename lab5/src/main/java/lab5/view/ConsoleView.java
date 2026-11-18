package lab5.view;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import lab5.model.CoffeeMachine;

public final class ConsoleView {
    /*
    Выводит меню, состояния и сообщения в консоль, читает ввод в UTF-8.
    */
    private final Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
    private final PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    private final PrintStream errors = new PrintStream(System.err, true, StandardCharsets.UTF_8);

    public void line(Object message) {
        /*
        Выводит сообщение с переводом строки.
        */
        output.println(message);
    }

    public void error(String message) {
        /*
        Выводит сообщение об ошибке в поток ошибок.
        */
        errors.println("Ошибка: " + message);
    }

    public void machine(int id, CoffeeMachine machine) {
        /*
        Печатает описание кофемашины с переданным идентификатором.
        */
        line("ID " + id + " | " + machine);
    }

    public void help() {
        /*
        Выводит справку по ключам запуска.
        */
        line("""
                Лабораторная работа №5. Кофемашины
                Запуск: java -jar target/lab5.jar <ключ> [аргументы]

                  --show <файл>                         Показать состояние один раз
                  --replace <источник> <назначение>     Заменить содержимое файла
                  --watch <файл> <секунды>               Периодически выводить состояние
                  --csv <файл> <журнал.csv> <секунды>    Записывать изменения в CSV
                  --interactive                         Открыть интерактивное меню ЛР4
                  --help                                Показать справку
                """);
    }

    public void showSnapshot(List<CoffeeMachine> machines) {
        /*
        Показывает устройства из файла в порядке чтения, нумеруя их с единицы.
        */
        for (int i = 0; i < machines.size(); i++) {
            machine(i + 1, machines.get(i));
        }
    }

    public void mainMenu() {
        /*
        Выводит главное меню интерактивного режима.
        */
        line("\n1. Показать устройства\n2. Добавить устройство\n3. Удалить устройство"
                + "\n4. Изменить свойства\n5. Работа с устройством"
                + "\n6. Случайные настройки\n7. Статистика кофейни\n8. Прочитать устройства из файла\n9. Изменить имя файла\n0. Выход");
    }

    public void machineTypes() {
        /*
        Показывает доступные типы кофемашин при добавлении устройства.
        */
        line("1. Капельная кофеварка\n2. Латте-машина\n3. Кофемашина с кофемолкой\n0. Отмена");
    }

    public void editMenu() {
        /*
        Выводит меню общих настроек кофемашины.
        */
        line("1. Фирма\n2. Модель\n3. Объём бака\n4. Температура кофе"
                + "\n5. Настройки выбранного типа устройства\n0. Отмена");
    }

    public void dripSettings() {
        /*
        Показывает настройки капельной кофеварки.
        */
        line("1. Контейнер кофе\n2. Крепость\n3. Объём порции\n0. Отмена");
    }

    public void latteSettings() {
        /*
        Показывает настройки латте-машины.
        */
        line("1. Молочный бак\n2. Температура молока\n3. Доля молока"
                    + "\n4. Контейнер капсул\n5. Уровень пенки\n0. Отмена");
    }

    public void grinderSettings() {
        /*
        Показывает настройки кофемашины с кофемолкой.
        */
        line("1. Контейнер зёрен\n2. Степень помола\n3. Крепость"
                    + "\n4. Контейнер отходов\n0. Отмена");
    }

    public void operationMenu() {
        /*
        Показывает действия для работы с выбранным устройством.
        */
        line("1. Включить\n2. Выключить\n3. Выбрать кофе\n4. Настроить пенку"
                    + "\n5. Приготовить кофе\n6. Добавить воду\n7. Слить воду"
                    + "\n8. Очистить устройство\n9. Специальные действия\n10. Показать состояние\n0. Назад");
    }

    public void dripActions() {
        /*
        Выводит дополнительные действия капельной кофеварки.
        */
        line("1. Добавить молотый кофе\n2. Заменить фильтр"
                    + "\n3. Переключить подогрев\n4. Удалить молотый кофе\n0. Отмена");
    }

    public void latteActions() {
        /*
        Выводит дополнительные действия латте-машины.
        */
        line("1. Добавить молоко\n2. Добавить капсулы"
                    + "\n3. Промыть молочный модуль\n4. Слить молоко\n0. Отмена");
    }

    public void grinderActions() {
        /*
        Выводит дополнительные действия кофемашины с кофемолкой.
        */
        line("1. Добавить зёрна\n2. Измельчить порцию"
                    + "\n3. Очистить контейнер отходов\n4. Удалить зёрна и молотый кофе\n0. Отмена");
    }

    public int readInt(String prompt, int min, int max) {
        /*
        Читает целое число в заданном диапазоне, повторяя запрос при ошибке.

        Args:
            prompt: текст приглашения ко вводу.
            min: нижняя граница диапазона.
            max: верхняя граница диапазона.

        Returns:
            Целое число от min до max включительно.
        */
        while (true) {
            String line = readLine(prompt + "[" + min + "-" + max + "] ").strip();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Неверное число и переполнение обрабатываются одинаково.
            }
            output.println("Введите целое число от " + min + " до " + max + ".");
        }
    }

    public String readText(String prompt) {
        /*
        Запрашивает название до успешной проверки.

        Returns:
            Название без лишних пробелов.
        */
        while (true) {
            try {
                return CoffeeMachine.validateText(readLine(prompt));
            } catch (IllegalArgumentException e) {
                output.println(e.getMessage());
            }
        }
    }

    public String readLine(String prompt) {
        /*
        Читает строку из консоли.

        Args:
            prompt: текст приглашения ко вводу.

        Returns:
            Введённая строка.

        Raises:
            EndOfInputException: если ввод закончился.
        */
        output.print(prompt);
        if (!input.hasNextLine()) {
            throw new EndOfInputException();
        }
        return input.nextLine();
    }

    public static final class EndOfInputException extends RuntimeException {
        /*
        Сообщает контроллеру, что ввод из консоли закончился.
        */
        private static final long serialVersionUID = 1L;
    }
}
