// CoffeeMachine.java

import java.util.concurrent.ThreadLocalRandom;

public abstract class CoffeeMachine {
    public static final int MIN_CAPACITY = 300;
    public static final int MAX_CAPACITY = 5000;
    public static final int MAX_TEXT_LENGTH = 60;
    public static final int MIN_TEMPERATURE = 80;
    public static final int MAX_TEMPERATURE = 96;
    public static final int MIN_FOAM_LEVEL = 0;
    public static final int MAX_FOAM_LEVEL = 5;
    private static final int DEFAULT_TEMPERATURE = 90;
    private static final int CLEANING_WATER_ML = 100;
    private static final int CUPS_BEFORE_CLEANING = 10;
    private static final double MILLILITERS_PER_LITER = 1000.0;
    private static int nextId = 1;

    private final int id;
    private String manufacturer;
    private String model;
    private int waterCapacity;
    private int water;
    private boolean powered;
    private CoffeeType coffeeType;
    private int foamLevel;
    private int temperature = DEFAULT_TEMPERATURE;
    private int cupsMade;
    private int cupsSinceCleaning;

    protected CoffeeMachine(String manufacturer, String model, int waterCapacity) {
        /*
        Конструктор кофемашины с пустым баком.

        Args:
            manufacturer: название фирмы.
            model: название модели.
            waterCapacity: объём бака в мл.
        */
        this.manufacturer = validateText(manufacturer);
        this.model = validateText(model);
        checkRange(waterCapacity, MIN_CAPACITY, MAX_CAPACITY, "Объём бака");
        this.waterCapacity = waterCapacity;
        if (nextId == Integer.MAX_VALUE) {
            throw new IllegalStateException("Закончились id устройств.");
        }
        id = nextId++;
    }

    public int getId() { return id; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public int getWaterCapacity() { return waterCapacity; }
    public double getWaterCapacityLiters() { return waterCapacity / MILLILITERS_PER_LITER; }
    public int getWater() { return water; }
    public boolean isPowered() { return powered; }
    public CoffeeType getCoffeeType() { return coffeeType; }
    public int getFoamLevel() { return foamLevel; }
    public int getTemperature() { return temperature; }
    public int getCupsMade() { return cupsMade; }

    public final void setManufacturer(String manufacturer) {
        /*
        Устанавливает название фирмы.

        Args:
            manufacturer: название фирмы.
        */
        this.manufacturer = validateText(manufacturer);
    }

    public final void setModel(String model) {
        /*
        Устанавливает название модели.

        Args:
            model: название модели.
        */
        this.model = validateText(model);
    }

    public final void setWaterCapacity(int capacity) {
        /*
        Устанавливает объём бака.

        Args:
            capacity: новый объём в мл.
        */
        checkRange(capacity, MIN_CAPACITY, MAX_CAPACITY, "Объём бака");
        if (capacity < water) {
            throw new IllegalArgumentException("Сначала слейте воду: новый бак меньше текущего запаса воды.");
        }
        waterCapacity = capacity;
    }

    public final void setTemperature(int temperature) {
        /*
        Устанавливает температуру приготовления.

        Args:
            temperature: температура в градусах.
        */
        checkRange(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE, "Температура");
        this.temperature = temperature;
    }

    public void turnOn() {
        /*
        Включает устройство.
        */
        if (powered) {
            throw new IllegalStateException("Устройство уже включено.");
        }
        powered = true;
    }

    public void turnOff() {
        /*
        Выключает устройство.
        */
        if (!powered) {
            throw new IllegalStateException("Устройство уже выключено.");
        }
        powered = false;
    }

    public final void selectCoffee(CoffeeType type) {
        /*
        Выбирает доступный для устройства тип кофе.

        Args:
            type: тип кофе.
        */
        if (type == null || !supports(type)) {
            throw new IllegalArgumentException("Этот напиток недоступен для устройства.");
        }
        coffeeType = type;
    }

    public final void adjustFoam(int level) {
        /*
        Настраивает уровень молочной пенки.

        Args:
            level: уровень от 0 до 5.
        */
        checkRange(level, MIN_FOAM_LEVEL, MAX_FOAM_LEVEL, "Уровень пенки");
        if (!hasMilkSystem() && level != 0) {
            throw new IllegalArgumentException("У этого устройства нет молочного модуля.");
        }
        foamLevel = level;
    }

    public final void refillWater(int amount) {
        /*
        Добавляет воду в бак.

        Args:
            amount: количество воды в мл.
        */
        checkAddition(amount, waterCapacity - water, "Вода");
        water += amount;
    }

    public final void drainWater() {
        /*
        Сливает воду из выключенного устройства.
        */
        requireOff();
        water = 0;
    }

    public final void clean() {
        /*
        Очищает устройство, расходуя 100 мл воды.
        */
        requirePowered();
        if (water < CLEANING_WATER_ML) {
            throw new IllegalStateException("Для очистки нужно " + CLEANING_WATER_ML + " мл воды.");
        }
        water -= CLEANING_WATER_ML;
        cupsSinceCleaning = 0;
    }

    public void randomizeSettings() {
        /*
        Выбирает случайную температуру и доступный напиток.
        */
        temperature = randomInt(MIN_TEMPERATURE, MAX_TEMPERATURE);
        CoffeeType[] types = getAvailableCoffeeTypes();
        selectCoffee(types[randomInt(0, types.length - 1)]);
    }

    protected final void checkPreparation(int waterNeeded) {
        /*
        Проверяет готовность устройства до расходования ингредиентов.

        Args:
            waterNeeded: количество воды для напитка в мл.
        */
        requirePowered();
        if (coffeeType == null) {
            throw new IllegalStateException("Сначала выберите тип кофе.");
        }
        if (cupsSinceCleaning >= CUPS_BEFORE_CLEANING) {
            throw new IllegalStateException("После " + CUPS_BEFORE_CLEANING + " чашек требуется очистка устройства.");
        }
        if (cupsMade == Integer.MAX_VALUE) {
            throw new IllegalStateException("Достигнут предел счётчика чашек.");
        }
        if (water < waterNeeded) {
            throw new IllegalStateException("Недостаточно воды: нужно " + waterNeeded + " мл.");
        }
    }

    protected final void finishPreparation(int waterUsed) {
        /*
        Расходует воду и увеличивает счётчики после проверки всех ингредиентов.

        Args:
            waterUsed: расход воды в мл.
        */
        water -= waterUsed;
        cupsMade++;
        cupsSinceCleaning++;
    }

    protected final void useWater(int amount) {
        /*
        Расходует воду без увеличения счётчика чашек.

        Args:
            amount: количество воды в мл.
        */
        checkRange(amount, 1, waterCapacity, "Расход воды");
        if (water < amount) {
            throw new IllegalStateException("Недостаточно воды.");
        }
        water -= amount;
    }

    protected boolean hasMilkSystem() {
        /*
        Проверяет наличие молочного модуля.
        */
        return false;
    }

    protected final void requirePowered() {
        /*
        Проверяет, что устройство включено.
        */
        if (!powered) {
            throw new IllegalStateException("Сначала включите устройство.");
        }
    }

    protected final void requireOff() {
        /*
        Проверяет, что устройство выключено.
        */
        if (powered) {
            throw new IllegalStateException("Сначала выключите устройство.");
        }
    }

    public static String validateText(String text) {
        /*
        Проверяет и очищает название.

        Args:
            text: введённое название.

        Returns:
            Название без лишних пробелов.
        */
        if (text == null || text.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Название не должно содержать управляющие символы.");
        }
        String cleaned = text.strip().replaceAll("\\s+", " ");
        if (cleaned.isEmpty() || cleaned.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Название должно содержать от 1 до " + MAX_TEXT_LENGTH + " символов.");
        }
        return cleaned;
    }

    protected static void checkRange(int value, int min, int max, String name) {
        /*
        Проверяет число в заданном диапазоне.

        Args:
            value: проверяемое число.
            min: нижняя граница.
            max: верхняя граница.
            name: название величины.
        */
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + ": допустимо от " + min + " до " + max + ".");
        }
    }

    protected static void checkAddition(int amount, int free, String name) {
        /*
        Проверяет количество добавляемого ингредиента.

        Args:
            amount: добавляемое количество.
            free: свободное место.
            name: название ингредиента.
        */
        if (amount <= 0 || amount > free) {
            throw new IllegalArgumentException(name + ": свободно " + free
                    + ", добавляемое количество должно быть положительным и не больше свободного места.");
        }
    }

    protected static int randomInt(int min, int max) {
        /*
        Возвращает случайное целое число в заданных границах.
        */
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    // Название конкретного типа устройства.
    public abstract String getDeviceType();
    // Напитки, доступные для конкретного типа устройства.
    public abstract CoffeeType[] getAvailableCoffeeTypes();
    // Проверка поддержки напитка конкретным типом устройства.
    public abstract boolean supports(CoffeeType type);
    // Приготовление напитка по правилам конкретного типа устройства.
    public abstract String brew();

    @Override
    public String toString() {
        /*
        Возвращает общую информацию об устройстве.
        */
        return String.format("ID %d | %s | %s %s | %s | вода: %d/%d мл (бак %.2f л)"
                + " | кофе: %s | пенка: %d | температура: %d °C | чашек: %d | до очистки: %d",
                id, getDeviceType(), manufacturer, model, powered ? "включено" : "выключено",
                water, waterCapacity, getWaterCapacityLiters(), coffeeType == null ? "не выбран" : coffeeType,
                foamLevel, temperature, cupsMade, CUPS_BEFORE_CLEANING - cupsSinceCleaning);
    }
}
