// GrinderCoffeeMachine.java

import java.util.EnumSet;
import java.util.Set;

public final class GrinderCoffeeMachine extends CoffeeMachine {
    public static final int MIN_BEAN_CAPACITY = 30;
    public static final int MAX_BEAN_CAPACITY = 1000;
    public static final int MIN_WASTE_CAPACITY = 20;
    public static final int MAX_WASTE_CAPACITY = 300;
    public static final int MIN_GRIND_LEVEL = 1;
    public static final int MAX_GRIND_LEVEL = 5;
    public static final int MIN_STRENGTH = 1;
    public static final int MAX_STRENGTH = 3;
    private static final int DEFAULT_WATER_CAPACITY = 1800;
    private static final int DEFAULT_BEAN_CAPACITY = 300;
    private static final int DEFAULT_WASTE_CAPACITY = 100;
    private static final int DEFAULT_GRIND_LEVEL = 3;
    private static final int DEFAULT_STRENGTH = 2;
    private static final int GROUND_COFFEE_CAPACITY = 30;
    private static final int BASE_COFFEE_GRAMS = 6;
    private static final int COFFEE_GRAMS_PER_STRENGTH = 2;
    private static final int ESPRESSO_WATER_ML = 40;
    private static final int AMERICANO_WATER_ML = 150;

    private int beanCapacity = DEFAULT_BEAN_CAPACITY;
    private int beans;
    private int grindLevel = DEFAULT_GRIND_LEVEL;
    private int strength = DEFAULT_STRENGTH;
    private int groundCoffee;
    private int wasteCapacity = DEFAULT_WASTE_CAPACITY;
    private int waste;

    public GrinderCoffeeMachine() {
        /*
        Конструктор без параметров.
        */
        this("DeLonghi", "Magnifica", DEFAULT_WATER_CAPACITY);
    }

    public GrinderCoffeeMachine(String manufacturer, String model, int capacity) {
        /*
        Конструктор автоматической кофемашины с кофемолкой.

        Args:
            manufacturer: название фирмы.
            model: название модели.
            capacity: объём бака в мл.
        */
        super(manufacturer, model, capacity);
        selectCoffee(CoffeeType.ESPRESSO);
    }

    public int getBeanCapacity() { return beanCapacity; }
    public int getBeans() { return beans; }
    public int getGrindLevel() { return grindLevel; }
    public int getStrength() { return strength; }
    public int getGroundCoffee() { return groundCoffee; }
    public int getWasteCapacity() { return wasteCapacity; }
    public int getWaste() { return waste; }

    public void setBeanCapacity(int capacity) {
        /*
        Устанавливает вместимость контейнера зёрен в граммах.
        */
        checkRange(capacity, MIN_BEAN_CAPACITY, MAX_BEAN_CAPACITY, "Контейнер зёрен");
        if (capacity < beans) {
            throw new IllegalArgumentException("Сначала удалите зёрна из контейнера.");
        }
        beanCapacity = capacity;
    }

    public void setWasteCapacity(int capacity) {
        /*
        Устанавливает вместимость контейнера отходов в граммах.
        */
        checkRange(capacity, MIN_WASTE_CAPACITY, MAX_WASTE_CAPACITY, "Контейнер отходов");
        if (capacity < waste) {
            throw new IllegalArgumentException("Сначала очистите контейнер отходов.");
        }
        wasteCapacity = capacity;
    }

    public void addBeans(int amount) {
        /*
        Добавляет кофейные зёрна.

        Args:
            amount: количество зёрен в граммах.
        */
        checkAddition(amount, beanCapacity - beans, "Зёрна");
        beans += amount;
    }

    public void chooseGrind(int level) {
        /*
        Выбирает степень помола от 1 до 5 на выключенном устройстве.
        */
        checkRange(level, MIN_GRIND_LEVEL, MAX_GRIND_LEVEL, "Степень помола");
        requireOff();
        grindLevel = level;
    }

    public void chooseStrength(int strength) {
        /*
        Выбирает крепость от 1 до 3.
        */
        checkRange(strength, MIN_STRENGTH, MAX_STRENGTH, "Крепость");
        this.strength = strength;
    }

    private int calculateCoffeeAmount() {
        /*
        Вычисляет расход кофе по выбранной крепости.

        Returns:
            Количество кофе в граммах.
        */
        return BASE_COFFEE_GRAMS + strength * COFFEE_GRAMS_PER_STRENGTH;
    }

    public void grindPortion() {
        /*
        Измельчает зёрна для одной порции, учитывая крепость.
        */
        requirePowered();
        int amount = calculateCoffeeAmount();
        if (groundCoffee + amount > GROUND_COFFEE_CAPACITY) {
            throw new IllegalStateException("В камере помола нет места: сначала приготовьте кофе.");
        }
        if (beans < amount) {
            throw new IllegalStateException("Недостаточно зёрен: нужно " + amount + " г.");
        }
        beans -= amount;
        groundCoffee += amount;
    }

    public void emptyWaste() {
        /*
        Очищает контейнер отходов выключенного устройства.
        */
        requireOff();
        waste = 0;
    }

    public void discardBeans() {
        /*
        Удаляет зёрна и молотый кофе из выключенного устройства.
        */
        requireOff();
        beans = 0;
        groundCoffee = 0;
    }

    @Override
    public String brew() {
        /*
        Готовит кофе, автоматически измельчая недостающие зёрна.

        Returns:
            Описание приготовленного напитка.

        Raises:
            IllegalStateException: если не хватает ингредиентов или места для отходов.
        */
        int waterNeeded = getCoffeeType() == CoffeeType.AMERICANO ? AMERICANO_WATER_ML : ESPRESSO_WATER_ML;
        int coffeeNeeded = calculateCoffeeAmount();
        checkPreparation(waterNeeded);
        int beansNeeded = Math.max(0, coffeeNeeded - groundCoffee);
        if (beans < beansNeeded) {
            throw new IllegalStateException("Недостаточно зёрен: дополнительно нужно " + beansNeeded + " г.");
        }
        if (wasteCapacity - waste < coffeeNeeded) {
            throw new IllegalStateException("Очистите контейнер отходов.");
        }
        beans -= beansNeeded;
        groundCoffee = Math.max(0, groundCoffee - coffeeNeeded);
        waste += coffeeNeeded;
        finishPreparation(waterNeeded);
        return getCoffeeType() + ": " + waterNeeded + " мл, крепость " + strength
                + ", степень помола " + grindLevel + ".";
    }

    @Override
    public void randomizeSettings() {
        /*
        Выбирает случайные настройки, включая помол выключенного устройства.
        */
        requireOff();
        super.randomizeSettings();
        chooseGrind(randomInt(MIN_GRIND_LEVEL, MAX_GRIND_LEVEL));
        chooseStrength(randomInt(MIN_STRENGTH, MAX_STRENGTH));
    }

    @Override
    public String getDeviceType() { return "Кофемашина с кофемолкой"; }
    @Override
    public Set<CoffeeType> getAvailableCoffeeTypes() {
        return EnumSet.of(CoffeeType.ESPRESSO, CoffeeType.AMERICANO);
    }
    @Override
    public boolean supports(CoffeeType type) {
        return getAvailableCoffeeTypes().contains(type);
    }

    @Override
    public String toString() {
        /*
        Возвращает описание кофемашины с кофемолкой.
        */
        return super.toString() + String.format(" | зёрна: %d/%d г | помол: %d | крепость: %d"
                + " | молотый кофе: %d/%d г | отходы: %d/%d г",
                beans, beanCapacity, grindLevel, strength, groundCoffee, GROUND_COFFEE_CAPACITY, waste, wasteCapacity);
    }
}
