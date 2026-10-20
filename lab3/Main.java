// Main.java

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final int MAX_MACHINES = 1000;
    private static final List<CoffeeMachine> machines = new ArrayList<>();

    public static void main(String[] args) {
        /*
        Обработка главного меню.
        */
        machines.add(new DripCoffeeMaker());
        machines.add(new LatteMachine());
        machines.add(new GrinderCoffeeMachine());
        System.out.println("Лабораторная работа №3. Автоматические кофемашины");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Выберите действие: ", 0, 7);
                try {
                    switch (choice) {
                        case 1 -> showMachines();
                        case 2 -> addMachine();
                        case 3 -> deleteMachine();
                        case 4 -> editMachine();
                        case 5 -> operateMachine();
                        case 6 -> randomizeMachine();
                        case 7 -> showStatistics();
                        case 0 -> running = false;
                        default -> throw new IllegalStateException("Такого пункта в меню нет.");
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
            System.out.println("Программа завершена.");
        } catch (EndOfInputException e) {
            System.out.println("\nВвод завершён. Программа закрыта.");
        }
    }

    private static void printMenu() {
        /*
        Выводит главное меню.
        */
        System.out.println("\n1. Показать устройства\n2. Добавить устройство\n3. Удалить устройство"
                + "\n4. Изменить свойства\n5. Работа с устройством"
                + "\n6. Случайные настройки\n7. Статистика кофейни\n0. Выход");
    }

    private static void showMachines() {
        /*
        Выводит устройства из общего списка.
        */
        if (machines.isEmpty()) {
            System.out.println("Нет устройств.");
            return;
        }
        for (CoffeeMachine machine : machines) {
            System.out.println(machine);
        }
    }

    private static CoffeeMachine selectMachine() {
        /*
        Запрашивает постоянный идентификатор устройства.

        Returns:
            Найденная кофемашина.

        Raises:
            IllegalStateException: если список пуст.
            EndOfInputException: если ввод закончился.
        */
        if (machines.isEmpty()) {
            throw new IllegalStateException("Список пуст. Сначала добавьте устройство.");
        }
        showMachines();
        while (true) {
            int id = readInt("ID устройства: ", 1, Integer.MAX_VALUE);
            for (CoffeeMachine machine : machines) {
                if (machine.getId() == id) {
                    return machine;
                }
            }
            System.out.println("Устройство с таким ID не найдено. Выберите ID из списка.");
        }
    }

    private static void addMachine() {
        /*
        Добавляет устройство выбранного класса с пустыми контейнерами.
        */
        if (machines.size() >= MAX_MACHINES) {
            throw new IllegalStateException("Достигнут предел: " + MAX_MACHINES + " устройств.");
        }
        System.out.println("1. Капельная кофеварка\n2. Латте-машина\n3. Кофемашина с кофемолкой\n0. Отмена");
        int type = readInt("Тип: ", 0, 3);
        if (type == 0) {
            return;
        }
        String manufacturer = readText("Фирма: ");
        String model = readText("Модель: ");
        int capacity = readInt("Объём бака (мл): ", CoffeeMachine.MIN_CAPACITY, CoffeeMachine.MAX_CAPACITY);
        CoffeeMachine machine = switch (type) {
            case 1 -> new DripCoffeeMaker(manufacturer, model, capacity);
            case 2 -> new LatteMachine(manufacturer, model, capacity);
            case 3 -> new GrinderCoffeeMachine(manufacturer, model, capacity);
            default -> throw new IllegalArgumentException("Неизвестный тип устройства.");
        };
        machines.add(machine);
        System.out.println("Устройство добавлено. ID: " + machine.getId());
    }

    private static void deleteMachine() {
        /*
        Удаляет выключенное устройство из списка.
        */
        CoffeeMachine machine = selectMachine();
        if (machine.isPowered()) {
            throw new IllegalStateException("Перед удалением выключите устройство.");
        }
        machines.remove(machine);
        System.out.println("Устройство удалено. ID остальных устройств сохранены.");
    }

    private static void editMachine() {
        /*
        Изменяет общее свойство или открывает настройки выбранного класса.
        */
        CoffeeMachine machine = selectMachine();
        System.out.println("1. Фирма\n2. Модель\n3. Объём бака\n4. Температура кофе"
                + "\n5. Настройки выбранного типа устройства\n0. Отмена");
        int choice = readInt("Свойство: ", 0, 5);
        switch (choice) {
            case 1 -> machine.setManufacturer(readText("Фирма: "));
            case 2 -> machine.setModel(readText("Модель: "));
            case 3 -> machine.setWaterCapacity(readInt("Объём бака (мл): ",
                    Math.max(CoffeeMachine.MIN_CAPACITY, machine.getWater()), CoffeeMachine.MAX_CAPACITY));
            case 4 -> machine.setTemperature(readInt("Температура кофе (°C): ",
                    CoffeeMachine.MIN_TEMPERATURE, CoffeeMachine.MAX_TEMPERATURE));
            case 5 -> editSpecificSettings(machine);
            case 0 -> { return; }
        }
        System.out.println(machine);
    }

    private static void editSpecificSettings(CoffeeMachine machine) {
        /*
        Изменяет настройки конкретного устройства.

        Args:
            machine: выбранная кофемашина.
        */
        if (machine instanceof DripCoffeeMaker drip) {
            System.out.println("1. Контейнер кофе\n2. Крепость\n3. Объём порции\n0. Отмена");
            switch (readInt("Свойство: ", 0, 3)) {
                case 1 -> drip.setGroundCapacity(readInt("Вместимость (г): ",
                        Math.max(DripCoffeeMaker.MIN_GROUND_CAPACITY, drip.getGroundCoffee()),
                        DripCoffeeMaker.MAX_GROUND_CAPACITY));
                case 2 -> drip.chooseStrength(readInt("Крепость: ",
                        DripCoffeeMaker.MIN_STRENGTH, DripCoffeeMaker.MAX_STRENGTH));
                case 3 -> drip.choosePortion(readInt("Объём порции (мл): ",
                        DripCoffeeMaker.MIN_PORTION_ML, DripCoffeeMaker.MAX_PORTION_ML));
                case 0 -> { return; }
            }
        } else if (machine instanceof LatteMachine latte) {
            System.out.println("1. Молочный бак\n2. Температура молока\n3. Доля молока"
                    + "\n4. Контейнер капсул\n5. Уровень пенки\n0. Отмена");
            switch (readInt("Свойство: ", 0, 5)) {
                case 1 -> latte.setMilkCapacity(readInt("Объём (мл): ",
                        Math.max(LatteMachine.MIN_MILK_CAPACITY, latte.getMilk()), LatteMachine.MAX_MILK_CAPACITY));
                case 2 -> latte.chooseMilkTemperature(readInt("Температура молока (°C): ",
                        LatteMachine.MIN_MILK_TEMPERATURE, LatteMachine.MAX_MILK_TEMPERATURE));
                case 3 -> latte.chooseMilkRatio(readInt("Доля молока: ",
                        LatteMachine.MIN_MILK_RATIO, LatteMachine.MAX_MILK_RATIO));
                case 4 -> latte.setCapsuleCapacity(readInt("Вместимость (шт.): ",
                        Math.max(LatteMachine.MIN_CAPSULE_CAPACITY, latte.getCapsules()),
                        LatteMachine.MAX_CAPSULE_CAPACITY));
                case 5 -> latte.adjustFoam(readInt("Уровень пенки: ",
                        CoffeeMachine.MIN_FOAM_LEVEL, CoffeeMachine.MAX_FOAM_LEVEL));
                case 0 -> { return; }
            }
        } else if (machine instanceof GrinderCoffeeMachine grinder) {
            System.out.println("1. Контейнер зёрен\n2. Степень помола\n3. Крепость"
                    + "\n4. Контейнер отходов\n0. Отмена");
            switch (readInt("Свойство: ", 0, 4)) {
                case 1 -> grinder.setBeanCapacity(readInt("Вместимость (г): ",
                        Math.max(GrinderCoffeeMachine.MIN_BEAN_CAPACITY, grinder.getBeans()),
                        GrinderCoffeeMachine.MAX_BEAN_CAPACITY));
                case 2 -> grinder.chooseGrind(readInt("Степень помола: ",
                        GrinderCoffeeMachine.MIN_GRIND_LEVEL, GrinderCoffeeMachine.MAX_GRIND_LEVEL));
                case 3 -> grinder.chooseStrength(readInt("Крепость: ",
                        GrinderCoffeeMachine.MIN_STRENGTH, GrinderCoffeeMachine.MAX_STRENGTH));
                case 4 -> grinder.setWasteCapacity(readInt("Вместимость (г): ",
                        Math.max(GrinderCoffeeMachine.MIN_WASTE_CAPACITY, grinder.getWaste()),
                        GrinderCoffeeMachine.MAX_WASTE_CAPACITY));
                case 0 -> { return; }
            }
        }
    }

    private static void operateMachine() {
        /*
        Обрабатывает меню выбранного устройства до возврата в главное меню.
        */
        CoffeeMachine machine = selectMachine();
        boolean running = true;
        while (running) {
            System.out.println("\nУстройство ID " + machine.getId() + ": " + machine.getDeviceType());
            System.out.println("1. Включить\n2. Выключить\n3. Выбрать кофе\n4. Настроить пенку"
                    + "\n5. Приготовить кофе\n6. Добавить воду\n7. Слить воду"
                    + "\n8. Очистить устройство\n9. Специальные действия\n10. Показать состояние\n0. Назад");
            int choice = readInt("Действие: ", 0, 10);
            try {
                switch (choice) {
                    case 1 -> machine.turnOn();
                    case 2 -> machine.turnOff();
                    case 3 -> selectCoffee(machine);
                    case 4 -> machine.adjustFoam(readInt("Уровень пенки: ",
                        CoffeeMachine.MIN_FOAM_LEVEL, CoffeeMachine.MAX_FOAM_LEVEL));
                    case 5 -> System.out.println(machine.brew());
                    case 6 -> addWater(machine);
                    case 7 -> machine.drainWater();
                    case 8 -> machine.clean();
                    case 9 -> specialAction(machine);
                    case 10 -> System.out.println(machine);
                    case 0 -> running = false;
                }
                if (choice != 0) {
                    System.out.println("Действие завершено.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void selectCoffee(CoffeeMachine machine) {
        /*
        Выбирает напиток из доступных для устройства.

        Args:
            machine: выбранная кофемашина.
        */
        CoffeeType[] types = machine.getAvailableCoffeeTypes();
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i]);
        }
        machine.selectCoffee(types[readInt("Напиток: ", 1, types.length) - 1]);
    }

    private static void addWater(CoffeeMachine machine) {
        /*
        Запрашивает количество воды в пределах свободного места в баке.
        */
        int free = machine.getWaterCapacity() - machine.getWater();
        if (free == 0) {
            throw new IllegalStateException("Бак уже заполнен.");
        }
        machine.refillWater(readInt("Вода (мл): ", 1, free));
    }

    private static void specialAction(CoffeeMachine machine) {
        /*
        Выполняет действие, характерное для выбранного класса.

        Args:
            machine: выбранная кофемашина.
        */
        if (machine instanceof DripCoffeeMaker drip) {
            System.out.println("1. Добавить молотый кофе\n2. Заменить фильтр"
                    + "\n3. Переключить подогрев\n4. Удалить молотый кофе\n0. Отмена");
            switch (readInt("Действие: ", 0, 4)) {
                case 1 -> drip.addGroundCoffee(readAmount("Кофе (г): ",
                        drip.getGroundCapacity() - drip.getGroundCoffee()));
                case 2 -> drip.replaceFilter();
                case 3 -> drip.toggleWarming();
                case 4 -> drip.discardGroundCoffee();
                case 0 -> { return; }
            }
        } else if (machine instanceof LatteMachine latte) {
            System.out.println("1. Добавить молоко\n2. Добавить капсулы"
                    + "\n3. Промыть молочный модуль\n4. Слить молоко\n0. Отмена");
            switch (readInt("Действие: ", 0, 4)) {
                case 1 -> latte.refillMilk(readAmount("Молоко (мл): ",
                        latte.getMilkCapacity() - latte.getMilk()));
                case 2 -> latte.addCapsules(readAmount("Капсулы (шт.): ",
                        latte.getCapsuleCapacity() - latte.getCapsules()));
                case 3 -> latte.rinseMilkSystem();
                case 4 -> latte.discardMilk();
                case 0 -> { return; }
            }
        } else if (machine instanceof GrinderCoffeeMachine grinder) {
            System.out.println("1. Добавить зёрна\n2. Измельчить порцию"
                    + "\n3. Очистить контейнер отходов\n4. Удалить зёрна и молотый кофе\n0. Отмена");
            switch (readInt("Действие: ", 0, 4)) {
                case 1 -> grinder.addBeans(readAmount("Зёрна (г): ",
                        grinder.getBeanCapacity() - grinder.getBeans()));
                case 2 -> grinder.grindPortion();
                case 3 -> grinder.emptyWaste();
                case 4 -> grinder.discardBeans();
                case 0 -> { return; }
            }
        }
    }

    private static void randomizeMachine() {
        /*
        Выбирает случайные настройки с помощью метода конкретного класса.
        */
        CoffeeMachine machine = selectMachine();
        machine.randomizeSettings();
        System.out.println("Новые настройки: " + machine);
    }

    private static void showStatistics() {
        /*
        Выводит статистику текущих устройств и самую активную кофемашину.
        */
        if (machines.isEmpty()) {
            System.out.println("Нет устройств.");
            return;
        }
        long totalCups = 0;
        int powered = 0;
        int best = 0;
        for (CoffeeMachine machine : machines) {
            totalCups += machine.getCupsMade();
            if (machine.isPowered()) {
                powered++;
            }
            best = Math.max(best, machine.getCupsMade());
        }
        System.out.println("Устройств: " + machines.size() + "; включено: " + powered
                + "; приготовлено чашек текущими устройствами: " + totalCups);
        if (best == 0) {
            System.out.println("Пока никто не приготовил кофе.");
            return;
        }
        System.out.println("Самые активные устройства (по " + best + " чашек):");
        for (CoffeeMachine machine : machines) {
            if (machine.getCupsMade() == best) {
                System.out.println("ID " + machine.getId() + ": " + machine.getManufacturer()
                        + " " + machine.getModel());
            }
        }
    }

    private static int readAmount(String prompt, int free) {
        /*
        Запрашивает количество ингредиента, если контейнер не заполнен.
        */
        if (free <= 0) {
            throw new IllegalStateException("Контейнер уже заполнен.");
        }
        return readInt(prompt, 1, free);
    }

    private static int readInt(String prompt, int min, int max) {
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
            System.out.println("Введите целое число от " + min + " до " + max + ".");
        }
    }

    private static String readText(String prompt) {
        /*
        Запрашивает название до успешной проверки.

        Returns:
            Название без лишних пробелов.
        */
        while (true) {
            try {
                return CoffeeMachine.validateText(readLine(prompt));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readLine(String prompt) {
        /*
        Читает строку из консоли.

        Args:
            prompt: текст приглашения ко вводу.

        Returns:
            Введённая строка.

        Raises:
            EndOfInputException: если ввод закончился.
        */
        System.out.print(prompt);
        if (!INPUT.hasNextLine()) {
            throw new EndOfInputException();
        }
        return INPUT.nextLine();
    }

    private static class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
