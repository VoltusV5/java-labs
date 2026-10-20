// CoffeeType.java

public enum CoffeeType {
    FILTER("Фильтр-кофе"),
    ESPRESSO("Эспрессо"),
    AMERICANO("Американо"),
    LATTE("Латте"),
    CAPPUCCINO("Капучино");

    private final String title;

    CoffeeType(String title) {
        /*
        Создаёт тип кофе.

        Args:
            title: название напитка.
        */
        this.title = title;
    }

    @Override
    public String toString() {
        /*
        Возвращает название напитка.
        */
        return title;
    }
}
