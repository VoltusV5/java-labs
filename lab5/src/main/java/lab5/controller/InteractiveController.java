package lab5.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lab5.io.CoffeeMachineFileReader;
import lab5.model.CoffeeMachine;
import lab5.model.CoffeeType;
import lab5.model.DripCoffeeMaker;
import lab5.model.GrinderCoffeeMachine;
import lab5.model.LatteMachine;
import lab5.model.MachineInventory;
import lab5.view.ConsoleView;
import lab5.view.ConsoleView.EndOfInputException;

public final class InteractiveController {
    /*
    Обрабатывает действия интерактивного меню и управляет списком кофемашин.
    */
    private final ConsoleView view;
    private final MachineInventory machines = new MachineInventory();
    private String fileName = "machines.txt";

    public InteractiveController(ConsoleView view) {
        /*
        Создаёт контроллер с консольным представлением для ввода и вывода.
        */
        this.view = view;
    }

    public void run() {
        /*
        Обработка главного меню.
        */
        machines.add(new DripCoffeeMaker());
        machines.add(new LatteMachine());
        machines.add(new GrinderCoffeeMachine());
        view.line("Лабораторная работа №5. Интерактивный режим");
        try {
            boolean running = true;
            while (running) {
                view.mainMenu();
                int choice = view.readInt("Выберите действие: ", 0, 9);
                try {
                    switch (choice) {
                        case 1 -> showMachines();
                        case 2 -> addMachine();
                        case 3 -> deleteMachine();
                        case 4 -> editMachine();
                        case 5 -> operateMachine();
                        case 6 -> randomizeMachine();
                        case 7 -> showStatistics();
                        case 8 -> loadMachines();
                        case 9 -> changeFileName();
                        case 0 -> running = false;
                        default -> throw new IllegalStateException("Такого пункта в меню нет.");
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    view.error(e.getMessage());
                }
            }
            view.line("Программа завершена.");
        } catch (EndOfInputException e) {
            view.line("\nВвод завершён. Программа закрыта.");
        }
    }

    private void changeFileName() {
        /*
        Изменяет имя или путь к файлу для следующего чтения.
        */
        String name = view.readLine("Имя или путь к файлу: ").strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Имя файла не должно быть пустым.");
        }
        fileName = name;
    }

    private void loadMachines() {
        /*
        Читает кофемашины из выбранного файла, выводит результаты чтения
        и добавляет найденные объекты в общий список.
        */
        view.line("Чтение файла: " + fileName);
        try {
            var result = new CoffeeMachineFileReader().read(Path.of(fileName), false);
            view.line("Найдено объектов: " + result.machines().size());
            view.line("Успешно прочитано свойств: " + result.propertiesRead());
            view.line("Не найдено свойств: " + result.propertiesMissing());
            view.line("Некорректных свойств: " + result.problems().size());
            result.problems().forEach(view::line);
            machines.addAll(result.machines());
            view.line("Добавлено устройств: " + result.machines().size());
        } catch (IOException e) {
            view.error("Не удалось прочитать файл: " + e.getMessage());
        }
    }

    private void showMachines() {
        /*
        Выводит устройства из общего списка.
        */
        if (machines.isEmpty()) {
            view.line("Нет устройств.");
            return;
        }
        machines.entries().forEach(entry -> view.machine(entry.getKey(), entry.getValue()));
    }

    private int selectMachine() {
        /*
        Запрашивает постоянный идентификатор устройства.

        Returns:
            Идентификатор найденной кофемашины.

        Raises:
            IllegalStateException: если список пуст.
            EndOfInputException: если ввод закончился.
        */
        if (machines.isEmpty()) {
            throw new IllegalStateException("Список пуст. Сначала добавьте устройство.");
        }
        showMachines();
        while (true) {
            int id = view.readInt("ID устройства: ", 1, Integer.MAX_VALUE);
            if (machines.get(id) != null) {
                return id;
            }
            view.line("Устройство с таким ID не найдено. Выберите ID из списка.");
        }
    }

    private void addMachine() {
        /*
        Добавляет устройство выбранного класса с пустыми контейнерами.
        */
        machines.checkSpace(1);
        view.machineTypes();
        int type = view.readInt("Тип: ", 0, 3);
        if (type == 0) {
            return;
        }
        String manufacturer = view.readText("Фирма: ");
        String model = view.readText("Модель: ");
        int capacity = view.readInt("Объём бака (мл): ", CoffeeMachine.MIN_CAPACITY, CoffeeMachine.MAX_CAPACITY);
        CoffeeMachine machine = switch (type) {
            case 1 -> new DripCoffeeMaker(manufacturer, model, capacity);
            case 2 -> new LatteMachine(manufacturer, model, capacity);
            case 3 -> new GrinderCoffeeMachine(manufacturer, model, capacity);
            default -> throw new IllegalArgumentException("Неизвестный тип устройства.");
        };
        int id = machines.add(machine);
        view.line("Устройство добавлено. ID: " + id);
    }

    private void deleteMachine() {
        /*
        Удаляет выключенное устройство из списка.
        */
        machines.remove(selectMachine());
        view.line("Устройство удалено. ID остальных устройств сохранены.");
    }

    private void editMachine() {
        /*
        Изменяет общее свойство или открывает настройки выбранного класса.
        */
        int id = selectMachine();
        CoffeeMachine machine = machines.get(id);
        view.editMenu();
        int choice = view.readInt("Свойство: ", 0, 5);
        switch (choice) {
            case 1 -> machine.setManufacturer(view.readText("Фирма: "));
            case 2 -> machine.setModel(view.readText("Модель: "));
            case 3 -> machine.setWaterCapacity(view.readInt("Объём бака (мл): ",
                    Math.max(CoffeeMachine.MIN_CAPACITY, machine.getWater()), CoffeeMachine.MAX_CAPACITY));
            case 4 -> machine.setTemperature(view.readInt("Температура кофе (°C): ",
                    CoffeeMachine.MIN_TEMPERATURE, CoffeeMachine.MAX_TEMPERATURE));
            case 5 -> editSpecificSettings(machine);
            case 0 -> {
                return;
            }
        }
        view.machine(id, machine);
    }

    private void editSpecificSettings(CoffeeMachine machine) {
        /*
        Изменяет настройки конкретного устройства.

        Args:
            machine: выбранная кофемашина.
        */
        if (machine instanceof DripCoffeeMaker drip) {
            view.dripSettings();
            switch (view.readInt("Свойство: ", 0, 3)) {
                case 1 -> drip.setGroundCapacity(view.readInt("Вместимость (г): ",
                        Math.max(DripCoffeeMaker.MIN_GROUND_CAPACITY, drip.getGroundCoffee()),
                        DripCoffeeMaker.MAX_GROUND_CAPACITY));
                case 2 -> drip.chooseStrength(view.readInt("Крепость: ",
                        DripCoffeeMaker.MIN_STRENGTH, DripCoffeeMaker.MAX_STRENGTH));
                case 3 -> drip.choosePortion(view.readInt("Объём порции (мл): ",
                        DripCoffeeMaker.MIN_PORTION_ML, DripCoffeeMaker.MAX_PORTION_ML));
                case 0 -> {
                    return;
                }
            }
        } else if (machine instanceof LatteMachine latte) {
            view.latteSettings();
            switch (view.readInt("Свойство: ", 0, 5)) {
                case 1 -> latte.setMilkCapacity(view.readInt("Объём (мл): ",
                        Math.max(LatteMachine.MIN_MILK_CAPACITY, latte.getMilk()), LatteMachine.MAX_MILK_CAPACITY));
                case 2 -> latte.chooseMilkTemperature(view.readInt("Температура молока (°C): ",
                        LatteMachine.MIN_MILK_TEMPERATURE, LatteMachine.MAX_MILK_TEMPERATURE));
                case 3 -> latte.chooseMilkRatio(view.readInt("Доля молока: ",
                        LatteMachine.MIN_MILK_RATIO, LatteMachine.MAX_MILK_RATIO));
                case 4 -> latte.setCapsuleCapacity(view.readInt("Вместимость (шт.): ",
                        Math.max(LatteMachine.MIN_CAPSULE_CAPACITY, latte.getCapsules()),
                        LatteMachine.MAX_CAPSULE_CAPACITY));
                case 5 -> latte.adjustFoam(view.readInt("Уровень пенки: ",
                        CoffeeMachine.MIN_FOAM_LEVEL, CoffeeMachine.MAX_FOAM_LEVEL));
                case 0 -> {
                    return;
                }
            }
        } else if (machine instanceof GrinderCoffeeMachine grinder) {
            view.grinderSettings();
            switch (view.readInt("Свойство: ", 0, 4)) {
                case 1 -> grinder.setBeanCapacity(view.readInt("Вместимость (г): ",
                        Math.max(GrinderCoffeeMachine.MIN_BEAN_CAPACITY, grinder.getBeans()),
                        GrinderCoffeeMachine.MAX_BEAN_CAPACITY));
                case 2 -> grinder.chooseGrind(view.readInt("Степень помола: ",
                        GrinderCoffeeMachine.MIN_GRIND_LEVEL, GrinderCoffeeMachine.MAX_GRIND_LEVEL));
                case 3 -> grinder.chooseStrength(view.readInt("Крепость: ",
                        GrinderCoffeeMachine.MIN_STRENGTH, GrinderCoffeeMachine.MAX_STRENGTH));
                case 4 -> grinder.setWasteCapacity(view.readInt("Вместимость (г): ",
                        Math.max(GrinderCoffeeMachine.MIN_WASTE_CAPACITY, grinder.getWaste()),
                        GrinderCoffeeMachine.MAX_WASTE_CAPACITY));
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void operateMachine() {
        /*
        Обрабатывает меню выбранного устройства до возврата в главное меню.
        */
        int id = selectMachine();
        CoffeeMachine machine = machines.get(id);
        boolean running = true;
        while (running) {
            view.line("\nУстройство ID " + id + ": " + machine.getDeviceType());
            view.operationMenu();
            int choice = view.readInt("Действие: ", 0, 10);
            try {
                switch (choice) {
                    case 1 -> machine.turnOn();
                    case 2 -> machine.turnOff();
                    case 3 -> selectCoffee(machine);
                    case 4 -> machine.adjustFoam(view.readInt("Уровень пенки: ",
                            CoffeeMachine.MIN_FOAM_LEVEL, CoffeeMachine.MAX_FOAM_LEVEL));
                    case 5 -> view.line(machine.brew());
                    case 6 -> addWater(machine);
                    case 7 -> machine.drainWater();
                    case 8 -> machine.clean();
                    case 9 -> specialAction(machine);
                    case 10 -> view.machine(id, machine);
                    case 0 -> running = false;
                }
                if (choice != 0) {
                    view.line("Действие завершено.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                view.error(e.getMessage());
            }
        }
    }

    private void selectCoffee(CoffeeMachine machine) {
        /*
        Выбирает напиток из доступных для устройства.

        Args:
            machine: выбранная кофемашина.
        */
        Set<CoffeeType> types = machine.getAvailableCoffeeTypes();
        int number = 1;
        for (CoffeeType type : types) {
            view.line(number++ + ". " + type);
        }
        int choice = view.readInt("Напиток: ", 1, types.size());
        for (CoffeeType type : types) {
            if (choice == 1) {
                machine.selectCoffee(type);
                return;
            }
            choice--;
        }
    }

    private void addWater(CoffeeMachine machine) {
        /*
        Запрашивает количество воды в пределах свободного места в баке.
        */
        int free = machine.getWaterCapacity() - machine.getWater();
        if (free == 0) {
            throw new IllegalStateException("Бак уже заполнен.");
        }
        machine.refillWater(view.readInt("Вода (мл): ", 1, free));
    }

    private void specialAction(CoffeeMachine machine) {
        /*
        Выполняет действие, характерное для выбранного класса.

        Args:
            machine: выбранная кофемашина.
        */
        if (machine instanceof DripCoffeeMaker drip) {
            view.dripActions();
            switch (view.readInt("Действие: ", 0, 4)) {
                case 1 -> drip.addGroundCoffee(readAmount("Кофе (г): ",
                        drip.getGroundCapacity() - drip.getGroundCoffee()));
                case 2 -> drip.replaceFilter();
                case 3 -> drip.toggleWarming();
                case 4 -> drip.discardGroundCoffee();
                case 0 -> {
                    return;
                }
            }
        } else if (machine instanceof LatteMachine latte) {
            view.latteActions();
            switch (view.readInt("Действие: ", 0, 4)) {
                case 1 -> latte.refillMilk(readAmount("Молоко (мл): ",
                        latte.getMilkCapacity() - latte.getMilk()));
                case 2 -> latte.addCapsules(readAmount("Капсулы (шт.): ",
                        latte.getCapsuleCapacity() - latte.getCapsules()));
                case 3 -> latte.rinseMilkSystem();
                case 4 -> latte.discardMilk();
                case 0 -> {
                    return;
                }
            }
        } else if (machine instanceof GrinderCoffeeMachine grinder) {
            view.grinderActions();
            switch (view.readInt("Действие: ", 0, 4)) {
                case 1 -> grinder.addBeans(readAmount("Зёрна (г): ",
                        grinder.getBeanCapacity() - grinder.getBeans()));
                case 2 -> grinder.grindPortion();
                case 3 -> grinder.emptyWaste();
                case 4 -> grinder.discardBeans();
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void randomizeMachine() {
        /*
        Выбирает случайные настройки с помощью метода конкретного класса.
        */
        int id = selectMachine();
        CoffeeMachine machine = machines.get(id);
        machine.randomizeSettings();
        view.line("Новые настройки");
        view.machine(id, machine);
    }

    private void showStatistics() {
        /*
        Выводит статистику текущих устройств и самую активную кофемашину.
        */
        if (machines.isEmpty()) {
            view.line("Нет устройств.");
            return;
        }
        long totalCups = 0;
        int powered = 0;
        int best = 0;
        for (var entry : machines.entries()) {
            CoffeeMachine machine = entry.getValue();
            totalCups += machine.getCupsMade();
            if (machine.isPowered()) {
                powered++;
            }
            best = Math.max(best, machine.getCupsMade());
        }
        view.line("Устройств: " + machines.size() + "; включено: " + powered
                + "; приготовлено чашек текущими устройствами: " + totalCups);
        if (best == 0) {
            view.line("Пока никто не приготовил кофе.");
            return;
        }
        view.line("Самые активные устройства (по " + best + " чашек):");
        for (var entry : machines.entries()) {
            CoffeeMachine machine = entry.getValue();
            if (machine.getCupsMade() == best) {
                view.line("ID " + entry.getKey() + ": " + machine.getManufacturer()
                        + " " + machine.getModel());
            }
        }
    }

    private int readAmount(String prompt, int free) {
        /*
        Запрашивает количество ингредиента, если контейнер не заполнен.
        */
        if (free <= 0) {
            throw new IllegalStateException("Контейнер уже заполнен.");
        }
        return view.readInt(prompt, 1, free);
    }
}
