// CoffeeMaker.java


public class CoffeeMaker {
    public static final int MIN_CAPACITY = 50;
    public static final int MAX_CAPACITY = 10000;
    public static final int MIN_CUP_VOLUME = 20;
    public static final int MAX_MANUFACTURER_LENGTH = 60;
    private static final int DEFAULT_CAPACITY = 1000;
    private static final int DEFAULT_CUP_VOLUME = 200;
    private static final String DEFAULT_MANUFACTURER = "Без марки";

    private String manufacturer;
    private int capacity;
    private int cupVolume;
    private int water;

    public CoffeeMaker() {
        /*
        Конструктор без параметров.
        */
        this(DEFAULT_MANUFACTURER, DEFAULT_CAPACITY, DEFAULT_CUP_VOLUME);
    }

    public CoffeeMaker(String manufacturer, int capacity, int cupVolume) {
        /*
        Конструктор кофеварки.

        Args:
            manufacturer: название фирмы.
            capacity: объём бака в мл.
            cupVolume: объём чашки в мл.
        */
        setManufacturer(manufacturer);
        setCapacity(capacity);
        setCupVolume(cupVolume);
    }

    public String getManufacturer() {
        return manufacturer;
    }
    public int getCapacity() {
        return capacity;
    }
    public int getCupVolume() {
        return cupVolume;
    }
    public int getWater() {
        return water;
    }

    public void setManufacturer(String manufacturer) {
        /*
        Устанавливает название фирмы.

        Args:
            manufacturer: название фирмы.

        Raises:
            IllegalArgumentException: в случае ошибки.
        */
        if (manufacturer == null) {
            throw new IllegalArgumentException("Фирма не может быть null.");
        }
        String cleaned = manufacturer.strip().replaceAll("\\s+", " ");
        if (cleaned.isEmpty() || cleaned.length() > MAX_MANUFACTURER_LENGTH) {
            throw new IllegalArgumentException("Некорректное название фирмы.");
        }
        this.manufacturer = cleaned;
    }

    public void setCapacity(int capacity) {
        /*
        Устанавливает объём бака.

        Args:
            capacity: объём бака в мл.

        Raises:
            IllegalArgumentException: в случае ошибки.
        */
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Неверный объем бака.");
        }
        if (capacity < cupVolume || capacity < water) {
            throw new IllegalArgumentException("Бак не может быть меньше чашки или текущего запаса воды.");
        }
        this.capacity = capacity;
    }

    public void setCupVolume(int cupVolume) {
        /*
        Устанавливает количество воды, расходуемое на одну чашку.

        Args:
            cupVolume: объём чашки в мл.

        Raises:
            IllegalArgumentException: в случае ошибки.
        */
        if (cupVolume < MIN_CUP_VOLUME || cupVolume > capacity) {
            throw new IllegalArgumentException(
                "Объём чашки должен быть от " + MIN_CUP_VOLUME + " мл до объёма бака."
            );
        }
        this.cupVolume = cupVolume;
    }

    public void refill(int amount) {
        /*
        Добавляет воду.

        Args:
            amount: количество добавляемой воды в мл.

        Raises:
            IllegalArgumentException: в случае ошибки.
        */
        if (amount <= 0 || amount > capacity - water) {
            throw new IllegalArgumentException("Количество воды должно быть от 1 до "
                    + (capacity - water) + " мл. Свободно: " + (capacity - water) + " мл.");
        }
        water += amount;
    }

    public void brew() {
        /*
        Готовит чашку.

        Raises:
            IllegalStateException: в случае ошибки.
        */
        if (water < cupVolume) {
            throw new IllegalStateException("Недостаточно воды: нужно " + cupVolume
                    + " мл, доступно " + water + " мл.");
        }
        water -= cupVolume;
    }

    @Override
    public String toString() {
        /*
        Описание кофеварки.

        Returns:
            Описание фирмы, объёмов бака и чашки, а также запаса воды
        */
        return String.format("Фирма: %s; бак: %d мл; чашка: %d мл; вода: %d мл",
            manufacturer, capacity, cupVolume, water);
    }
}
