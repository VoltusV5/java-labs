package lab5.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MachineInventory {
    /*
    Хранит кофемашины интерактивного режима и выдаёт им постоянные ID в пределах сеанса.
    */
    public static final int MAX_MACHINES = 1000;
    private final Map<Integer, CoffeeMachine> machines = new LinkedHashMap<>();
    private int nextId = 1;

    public boolean isEmpty() {
        /*
        Проверяет, пуст ли список устройств.
        */
        return machines.isEmpty();
    }

    public int size() {
        /*
        Возвращает число устройств в списке.
        */
        return machines.size();
    }

    public Set<Map.Entry<Integer, CoffeeMachine>> entries() {
        /*
        Возвращает ID и устройства в порядке добавления. Состав списка через результат изменить нельзя.
        */
        return Collections.unmodifiableMap(machines).entrySet();
    }

    public CoffeeMachine get(int id) {
        /*
        Возвращает устройство по ID или null, если его нет в списке.
        */
        return machines.get(id);
    }

    public void checkSpace(int count) {
        /*
        Проверяет, хватит ли мест в списке и свободных ID для добавления устройств.
        */
        if (count > MAX_MACHINES - machines.size()) {
            throw new IllegalStateException("Предел списка: " + MAX_MACHINES + " устройств.");
        }
        if (count > Integer.MAX_VALUE - nextId) {
            throw new IllegalStateException("Закончились ID устройств.");
        }
    }

    public int add(CoffeeMachine machine) {
        /*
        Добавляет кофемашину и возвращает выданный ей ID.
        */
        checkSpace(1);
        int id = nextId++;
        machines.put(id, machine);
        return id;
    }

    public void addAll(List<CoffeeMachine> loaded) {
        /*
        Добавляет загруженные кофемашины после проверки места для всего списка.
        */
        checkSpace(loaded.size());
        for (CoffeeMachine machine : loaded) {
            machines.put(nextId++, machine);
        }
    }

    public void remove(int id) {
        /*
        Удаляет выключенное устройство по ID. ID остальных устройств сохраняются.
        */
        CoffeeMachine machine = machines.get(id);
        if (machine == null) {
            throw new IllegalArgumentException("Устройство с таким ID не найдено.");
        }
        if (machine.isPowered()) {
            throw new IllegalStateException("Перед удалением выключите устройство.");
        }
        machines.remove(id);
    }
}
