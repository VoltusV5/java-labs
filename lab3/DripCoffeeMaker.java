// DripCoffeeMaker.java

import java.util.EnumSet;
import java.util.Set;

public final class DripCoffeeMaker extends CoffeeMachine {
    public static final int MIN_GROUND_CAPACITY = 30;
    public static final int MAX_GROUND_CAPACITY = 500;
    public static final int MIN_STRENGTH = 1;
    public static final int MAX_STRENGTH = 3;
    public static final int MIN_PORTION_ML = 100;
    public static final int MAX_PORTION_ML = 300;
    private static final int DEFAULT_WATER_CAPACITY = 1200;
    private static final int DEFAULT_GROUND_CAPACITY = 200;
    private static final int DEFAULT_STRENGTH = 2;
    private static final int DEFAULT_PORTION_ML = 200;
    private static final int MAX_FILTER_USES = 5;
    private static final int COFFEE_VOLUME_UNIT_ML = 100;
    private static final int BASE_COFFEE_GRAMS = 3;

    private int groundCapacity = DEFAULT_GROUND_CAPACITY;
    private int groundCoffee;
    private int strength = DEFAULT_STRENGTH;
    private int portionVolume = DEFAULT_PORTION_ML;
    private boolean keepWarm;
    private int filterUses;

    public DripCoffeeMaker() {
        /*
        Конструктор без параметров.
        */
        this("Philips", "Daily", DEFAULT_WATER_CAPACITY);
    }

    public DripCoffeeMaker(String manufacturer, String model, int capacity) {
        /*
        Конструктор капельной кофеварки.

        Args:
            manufacturer: название фирмы.
            model: название модели.
            capacity: объём бака в мл.
        */
        super(manufacturer, model, capacity);
        selectCoffee(CoffeeType.FILTER);
    }

    public int getGroundCapacity() { return groundCapacity; }
    public int getGroundCoffee() { return groundCoffee; }
    public int getStrength() { return strength; }
    public int getPortionVolume() { return portionVolume; }
    public boolean isKeepWarm() { return keepWarm; }
    public int getFilterUses() { return filterUses; }

    public void setGroundCapacity(int capacity) {
        /*
        Устанавливает вместимость контейнера кофе в граммах.
        */
        checkRange(capacity, MIN_GROUND_CAPACITY, MAX_GROUND_CAPACITY, "Контейнер кофе");
        if (capacity < groundCoffee) {
            throw new IllegalArgumentException("Сначала удалите кофе из контейнера.");
        }
        groundCapacity = capacity;
    }

    public void addGroundCoffee(int amount) {
        /*
        Добавляет молотый кофе.

        Args:
            amount: количество кофе в граммах.
        */
        checkAddition(amount, groundCapacity - groundCoffee, "Молотый кофе");
        groundCoffee += amount;
    }

    public void chooseStrength(int strength) {
        /*
        Выбирает крепость кофе от 1 до 3.
        */
        checkRange(strength, MIN_STRENGTH, MAX_STRENGTH, "Крепость");
        this.strength = strength;
    }

    public void choosePortion(int volume) {
        /*
        Выбирает объём порции от 100 до 300 мл.
        */
        checkRange(volume, MIN_PORTION_ML, MAX_PORTION_ML, "Объём порции");
        portionVolume = volume;
    }

    public void toggleWarming() {
        /*
        Переключает поддержание тепла во включённом устройстве.
        */
        requirePowered();
        keepWarm = !keepWarm;
    }

    public void replaceFilter() {
        /*
        Заменяет фильтр выключенного устройства.
        */
        requireOff();
        filterUses = 0;
    }

    public void discardGroundCoffee() {
        /*
        Удаляет молотый кофе из выключенного устройства.
        */
        requireOff();
        groundCoffee = 0;
    }

    @Override
    public void turnOff() {
        /*
        Выключает устройство и поддержание тепла.
        */
        super.turnOff();
        keepWarm = false;
    }

    private int calculateCoffeeAmount() {
        /*
        Вычисляет расход кофе, округляя объём порции вверх до единицы расчёта.

        Returns:
            Количество молотого кофе в граммах.
        */
        int volumeUnits = (portionVolume + COFFEE_VOLUME_UNIT_ML - 1) / COFFEE_VOLUME_UNIT_ML;
        return volumeUnits * (strength + BASE_COFFEE_GRAMS);
    }

    @Override
    public String brew() {
        /*
        Готовит порцию фильтр-кофе.

        Returns:
            Описание приготовленного напитка.

        Raises:
            IllegalStateException: если устройство не готово или не хватает кофе.
        */
        checkPreparation(portionVolume);
        int coffeeNeeded = calculateCoffeeAmount();
        if (filterUses >= MAX_FILTER_USES) {
            throw new IllegalStateException("После " + MAX_FILTER_USES + " порций нужно заменить фильтр.");
        }
        if (groundCoffee < coffeeNeeded) {
            throw new IllegalStateException("Недостаточно молотого кофе: нужно " + coffeeNeeded + " г.");
        }
        groundCoffee -= coffeeNeeded;
        filterUses++;
        finishPreparation(portionVolume);
        return "Фильтр-кофе: " + portionVolume + " мл, крепость " + strength
                + (keepWarm ? ", поддержание тепла включено." : ".");
    }

    @Override
    public void randomizeSettings() {
        /*
        Выбирает случайные температуру, крепость и объём порции.
        */
        super.randomizeSettings();
        chooseStrength(randomInt(MIN_STRENGTH, MAX_STRENGTH));
        choosePortion(randomInt(MIN_PORTION_ML, MAX_PORTION_ML));
    }

    @Override
    public String getDeviceType() { return "Капельная кофеварка"; }
    @Override
    public Set<CoffeeType> getAvailableCoffeeTypes() { return EnumSet.of(CoffeeType.FILTER); }
    @Override
    public boolean supports(CoffeeType type) { return getAvailableCoffeeTypes().contains(type); }

    @Override
    public String toString() {
        /*
        Возвращает описание капельной кофеварки.
        */
        return super.toString() + String.format(" | молотый кофе: %d/%d г | крепость: %d"
                + " | порция: %d мл | подогрев: %s | фильтр: %d/%d",
                groundCoffee, groundCapacity, strength, portionVolume, keepWarm ? "да" : "нет", filterUses, MAX_FILTER_USES);
    }
}
