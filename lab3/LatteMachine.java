// LatteMachine.java

public final class LatteMachine extends CoffeeMachine {
    public static final int MIN_MILK_CAPACITY = 200;
    public static final int MAX_MILK_CAPACITY = 1500;
    public static final int MIN_MILK_TEMPERATURE = 45;
    public static final int MAX_MILK_TEMPERATURE = 70;
    public static final int MIN_MILK_RATIO = 1;
    public static final int MAX_MILK_RATIO = 3;
    public static final int MIN_CAPSULE_CAPACITY = 1;
    public static final int MAX_CAPSULE_CAPACITY = 30;
    private static final int DEFAULT_WATER_CAPACITY = 1500;
    private static final int DEFAULT_MILK_CAPACITY = 600;
    private static final int DEFAULT_MILK_TEMPERATURE = 60;
    private static final int DEFAULT_MILK_RATIO = 2;
    private static final int DEFAULT_CAPSULE_CAPACITY = 10;
    private static final int DEFAULT_FOAM_LEVEL = 2;
    private static final int RINSE_WATER_ML = 50;
    private static final int COFFEE_BASE_ML = 50;

    private int milkCapacity = DEFAULT_MILK_CAPACITY;
    private int milk;
    private int milkTemperature = DEFAULT_MILK_TEMPERATURE;
    private int milkRatio = DEFAULT_MILK_RATIO;
    private int capsules;
    private int capsuleCapacity = DEFAULT_CAPSULE_CAPACITY;
    private boolean milkSystemDirty;

    public LatteMachine() {
        /*
        Конструктор без параметров.
        */
        this("DeLonghi", "Latte", DEFAULT_WATER_CAPACITY);
    }

    public LatteMachine(String manufacturer, String model, int capacity) {
        /*
        Конструктор латте-машины, использующей кофейные капсулы.

        Args:
            manufacturer: название фирмы.
            model: название модели.
            capacity: объём бака в мл.
        */
        super(manufacturer, model, capacity);
        selectCoffee(CoffeeType.LATTE);
        adjustFoam(DEFAULT_FOAM_LEVEL);
    }

    public int getMilkCapacity() { return milkCapacity; }
    public int getMilk() { return milk; }
    public int getMilkTemperature() { return milkTemperature; }
    public int getMilkRatio() { return milkRatio; }
    public int getCapsules() { return capsules; }
    public int getCapsuleCapacity() { return capsuleCapacity; }
    public boolean isMilkSystemDirty() { return milkSystemDirty; }

    public void setMilkCapacity(int capacity) {
        /*
        Устанавливает объём молочного бака в мл.
        */
        checkRange(capacity, MIN_MILK_CAPACITY, MAX_MILK_CAPACITY, "Молочный бак");
        if (capacity < milk) {
            throw new IllegalArgumentException("Сначала слейте молоко.");
        }
        milkCapacity = capacity;
    }

    public void setCapsuleCapacity(int capacity) {
        /*
        Устанавливает вместимость контейнера капсул.
        */
        checkRange(capacity, MIN_CAPSULE_CAPACITY, MAX_CAPSULE_CAPACITY, "Контейнер капсул");
        if (capacity < capsules) {
            throw new IllegalArgumentException("В контейнере уже больше капсул.");
        }
        capsuleCapacity = capacity;
    }

    public void refillMilk(int amount) {
        /*
        Добавляет молоко.

        Args:
            amount: количество молока в мл.
        */
        checkAddition(amount, milkCapacity - milk, "Молоко");
        milk += amount;
    }

    public void addCapsules(int amount) {
        /*
        Добавляет кофейные капсулы.

        Args:
            amount: количество капсул.
        */
        checkAddition(amount, capsuleCapacity - capsules, "Капсулы");
        capsules += amount;
    }

    public void chooseMilkTemperature(int temperature) {
        /*
        Выбирает температуру молока от 45 до 70 градусов.
        */
        checkRange(temperature, MIN_MILK_TEMPERATURE, MAX_MILK_TEMPERATURE, "Температура молока");
        milkTemperature = temperature;
    }

    public void chooseMilkRatio(int ratio) {
        /*
        Выбирает отношение молока к 50 мл кофе: от 1 до 3.
        */
        checkRange(ratio, MIN_MILK_RATIO, MAX_MILK_RATIO, "Доля молока");
        milkRatio = ratio;
    }

    public void rinseMilkSystem() {
        /*
        Промывает молочный модуль, расходуя 50 мл воды.
        */
        requirePowered();
        if (getWater() < RINSE_WATER_ML) {
            throw new IllegalStateException("Для промывки нужно " + RINSE_WATER_ML + " мл воды.");
        }
        useWater(RINSE_WATER_ML);
        milkSystemDirty = false;
    }

    @Override
    protected boolean hasMilkSystem() {
        /*
        Проверяет наличие молочного модуля.
        */
        return true;
    }

    public void discardMilk() {
        /*
        Сливает молоко из выключенного устройства.
        */
        requireOff();
        milk = 0;
    }

    @Override
    public String brew() {
        /*
        Готовит выбранный напиток из капсулы и молока.

        Returns:
            Описание приготовленного напитка.

        Raises:
            IllegalStateException: если не хватает ингредиентов или нужна промывка.
        */
        int waterNeeded = COFFEE_BASE_ML;
        int milkNeeded = getCoffeeType() == CoffeeType.ESPRESSO ? 0 : COFFEE_BASE_ML * milkRatio;
        checkPreparation(waterNeeded);
        if (capsules == 0) {
            throw new IllegalStateException("Добавьте кофейную капсулу.");
        }
        if (milkNeeded > 0 && milkSystemDirty) {
            throw new IllegalStateException("Перед новым молочным напитком промойте молочный модуль.");
        }
        if (milk < milkNeeded) {
            throw new IllegalStateException("Недостаточно молока: нужно " + milkNeeded + " мл.");
        }
        capsules--;
        milk -= milkNeeded;
        if (milkNeeded > 0) {
            milkSystemDirty = true;
        }
        finishPreparation(waterNeeded);
        return getCoffeeType() + ": " + (waterNeeded + milkNeeded) + " мл"
                + (milkNeeded > 0 ? ", молоко " + milkTemperature + " °C, пенка " + getFoamLevel() : "") + ".";
    }

    @Override
    public void randomizeSettings() {
        /*
        Выбирает случайные настройки кофе, молока и пенки.
        */
        super.randomizeSettings();
        chooseMilkTemperature(randomInt(MIN_MILK_TEMPERATURE, MAX_MILK_TEMPERATURE));
        chooseMilkRatio(randomInt(MIN_MILK_RATIO, MAX_MILK_RATIO));
        adjustFoam(randomInt(MIN_FOAM_LEVEL, MAX_FOAM_LEVEL));
    }

    @Override
    public String getDeviceType() { return "Латте-машина"; }
    @Override
    public CoffeeType[] getAvailableCoffeeTypes() {
        return new CoffeeType[] {CoffeeType.ESPRESSO, CoffeeType.LATTE, CoffeeType.CAPPUCCINO};
    }
    @Override
    public boolean supports(CoffeeType type) {
        return type == CoffeeType.ESPRESSO || type == CoffeeType.LATTE || type == CoffeeType.CAPPUCCINO;
    }

    @Override
    public String toString() {
        /*
        Возвращает описание латте-машины.
        */
        return super.toString() + String.format(" | молоко: %d/%d мл | температура молока: %d °C"
                + " | доля молока: %d | капсулы: %d/%d | нужна промывка: %s",
                milk, milkCapacity, milkTemperature, milkRatio, capsules, capsuleCapacity,
                milkSystemDirty ? "да" : "нет");
    }
}
