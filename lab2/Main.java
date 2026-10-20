// Main.java

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final int MAX_MAKERS = 1000;  // Максимальное количество кофеварок.
    private static CoffeeMaker[] makers = {
        new CoffeeMaker(),
        new CoffeeMaker("DeLonghi", 1500, 150),
        new CoffeeMaker("Philips", 1200, 200),
        new CoffeeMaker("DeLonghi", 2000, 250)
    };

    public static void main(String[] args) {
        /*
        Обработка меню
        */
        System.out.println("Лабораторная работа №2. Кофеварки");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Выберите действие: ", 0, 7);
                try {
                    switch (choice) {
                        case 1 -> showMakers();
                        case 2 -> editMaker();
                        case 3 -> addMaker();
                        case 4 -> deleteMaker();
                        case 5 -> showLeastCommonManufacturers();
                        case 6 -> refillMaker();
                        case 7 -> brewCoffee();
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
        Выводит меню.
        */
        System.out.println("\n1. Показать кофеварки\n2. Изменить свойства кофеварки"
                + "\n3. Добавить кофеварку\n4. Удалить кофеварку"
                + "\n5. Найти наименее распространённую фирму"
                + "\n6. Добавить воду\n7. Приготовить кофе\n0. Выход");
    }

    private static void showMakers() {
        /*
        Выводит кофеварки
        */
        if (makers.length == 0) {
            System.out.println("Нет кофеварок.");
            return;
        }
        for (int i = 0; i < makers.length; i++) {
            System.out.println((i + 1) + ". " + makers[i]);
        }
    }

    private static int selectIndex() {
        /*
        Запрашивает номер кофеварки.

        Returns:
            Индекс выбранной кофеварки.

        Raises:
            IllegalStateException: если массив пуст.
            EndOfInputException: если ввод закончился.
        */
        if (makers.length == 0) {
            throw new IllegalStateException("Массив пуст. Сначала добавьте кофеварку.");
        }
        showMakers();
        return readInt("Номер кофеварки: ", 1, makers.length) - 1;
    }

    private static void editMaker() {
        /*
        Изменяет выбранное свойство кофеварки.

        Raises:
            IllegalStateException: если массив пуст.
            EndOfInputException: если ввод закончился.
        */
        CoffeeMaker maker = makers[selectIndex()];
        System.out.println("1. Фирма\n2. Объём бака\n3. Объём чашки\n0. Отмена");
        switch (readInt("Свойство: ", 0, 3)) {
            case 1 -> maker.setManufacturer(readManufacturer());
            case 2 -> maker.setCapacity(readInt("Новый объём бака (мл): ",
                    Math.max(CoffeeMaker.MIN_CAPACITY,
                            Math.max(maker.getCupVolume(), maker.getWater())),
                    CoffeeMaker.MAX_CAPACITY));
            case 3 -> maker.setCupVolume(readInt("Новый объём чашки (мл): ",
                    CoffeeMaker.MIN_CUP_VOLUME, maker.getCapacity()));
            case 0 -> { return; }
        }
        System.out.println("Свойство изменено: " + maker);
    }

    private static void addMaker() {
        /*
        Добавляет кофеварку.

        Raises:
            IllegalStateException: если достигнут предел количества кофеварок.
            EndOfInputException: если ввод закончился.
        */
        if (makers.length >= MAX_MAKERS) {
            throw new IllegalStateException("Достигнут предел: " + MAX_MAKERS + " кофеварок.");
        }
        String manufacturer = readManufacturer();
        int capacity = readInt("Объём бака (мл): ", CoffeeMaker.MIN_CAPACITY, CoffeeMaker.MAX_CAPACITY);
        int cupVolume = readInt("Объём чашки (мл): ", CoffeeMaker.MIN_CUP_VOLUME, capacity);
        CoffeeMaker maker = new CoffeeMaker(manufacturer, capacity, cupVolume);
        makers = Arrays.copyOf(makers, makers.length + 1);
        makers[makers.length - 1] = maker;
        System.out.println("Кофеварка добавлена под номером " + makers.length + ".");
    }

    private static void deleteMaker() {
        /*
        Удаляет выбранную кофеварку.

        Raises:
            IllegalStateException: если массив пуст.
            EndOfInputException: если ввод закончился.
        */
        int index = selectIndex();
        CoffeeMaker[] updated = new CoffeeMaker[makers.length - 1];
        System.arraycopy(makers, 0, updated, 0, index);
        System.arraycopy(makers, index + 1, updated, index, makers.length - index - 1);
        makers = updated;
        System.out.println("Кофеварка удалена.");
    }

    private static void showLeastCommonManufacturers() {
        /*
        Выводит наименее распространённые фирмы.
        */
        if (makers.length == 0) {
            System.out.println("Массив пуст.");
            return;
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (CoffeeMaker maker : makers) {
            String name = maker.getManufacturer();
            String key = name.toLowerCase(Locale.ROOT);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            names.putIfAbsent(key, name);
        }
        int minimum = makers.length;
        for (int count : counts.values()) {
            minimum = Math.min(minimum, count);
        }
        System.out.println("Наименее распространённые фирмы:");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == minimum) {
                System.out.println("- " + names.get(entry.getKey()));
            }
        }
    }

    private static void refillMaker() {
        /*
        Добавляет воду в выбранную кофеварку.

        Raises:
            IllegalStateException: если массив пуст.
            EndOfInputException: если ввод закончился.
        */
        CoffeeMaker maker = makers[selectIndex()];
        int free = maker.getCapacity() - maker.getWater();
        if (free == 0) {
            System.out.println("Бак уже заполнен.");
            return;
        }
        maker.refill(readInt("Количество добавляемой воды (мл): ", 1, free));
        System.out.println("Вода добавлена. В баке " + maker.getWater() + " мл.");
    }

    private static void brewCoffee() {
        /*
        Готовит одну чашку кофе.

        Raises:
            IllegalStateException: если массив пуст или воды недостаточно.
            EndOfInputException: если ввод закончился.
        */
        CoffeeMaker maker = makers[selectIndex()];
        maker.brew();
        System.out.println("Приготовлена чашка " + maker.getCupVolume()
                + " мл. Осталось воды: " + maker.getWater() + " мл.");
    }

    private static int readInt(String prompt, int min, int max) {
        /*
        Читает целое число в заданном диапазоне.

        Args:
            prompt: текст приглашения ко вводу.
            min: нижняя граница диапазона.
            max: верхняя граница диапазона.

        Returns:
            Целое число от min до max включительно.
        */
        while (true) {
            String line = readLine(prompt + "[" + min + "–" + max + "] ").strip();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) { }
            System.out.println("Введите целое число от " + min + " до " + max + ".");
        }
    }

    private static String readManufacturer() {
        /*
        Запрашивает название фирмы.

        Returns:
            Название фирмы.
        */
        while (true) {
            String name = readLine("Фирма: ");
            try {
                CoffeeMaker validation = new CoffeeMaker();
                validation.setManufacturer(name);
                return validation.getManufacturer();
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
        */
        System.out.print(prompt);
        if (!INPUT.hasNextLine()) {
            throw new EndOfInputException();
        }
        return INPUT.nextLine();
    }

    private static class EndOfInputException extends RuntimeException { }
}
