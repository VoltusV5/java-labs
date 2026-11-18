package lab5.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lab5.model.CoffeeMachine;
import lab5.model.DripCoffeeMaker;
import lab5.model.GrinderCoffeeMachine;
import lab5.model.LatteMachine;

public final class CsvJournal implements AutoCloseable {
    /*
    Дописывает в CSV начальное состояние кофемашин и изменения между успешными чтениями.
    */
    private static final String HEADER = "timestamp,machine,property,value";
    private final BufferedWriter writer;
    private boolean needsHeader;
    private Map<Property, String> previous = Map.of();

    private record Property(String machine, String name) {
        /*
        Определяет свойство по идентификатору устройства и имени.
        */
    }

    public CsvJournal(Path path) throws IOException {
        /*
        Открывает журнал для добавления строк. Проверяет формат, если в файле уже есть данные.
        */
        boolean hasData = Files.exists(path) && Files.size(path) > 0;
        if (hasData) {
            if (!Files.isRegularFile(path)) {
                throw new IOException("Журнал должен быть обычным файлом: " + path);
            }
            checkExistingJournal(path);
        }
        needsHeader = !hasData;
        writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void checkExistingJournal(Path path) throws IOException {
        /*
        Проверяет заголовок CSV и наличие перевода строки в конце файла.
        */
        byte[] header = (HEADER + "\r\n").getBytes(StandardCharsets.UTF_8);
        try (var input = Files.newInputStream(path)) {
            if (!Arrays.equals(header, input.readNBytes(header.length))) {
                throw new IOException("У файла другой формат журнала: " + path);
            }
        }
        try (var channel = Files.newByteChannel(path)) {
            channel.position(channel.size() - 1);
            ByteBuffer lastByte = ByteBuffer.allocate(1);
            if (channel.read(lastByte) != 1 || lastByte.get(0) != '\n') {
                throw new IOException("Последняя строка журнала не завершена: " + path);
            }
        }
    }

    public void record(Instant time, List<CoffeeMachine> machines) throws IOException {
        /*
        Сравнивает текущее состояние с предыдущим и дописывает изменения в журнал.

        Args:
            time: время обнаружения изменений.
            machines: список кофемашин после успешного чтения файла.
        */
        Map<Property, String> current = snapshot(machines);
        if (current.equals(previous)) {
            return;
        }
        StringBuilder rows = new StringBuilder();
        if (needsHeader) {
            rows.append(HEADER).append("\r\n");
        }
        for (var entry : current.entrySet()) {
            if (!entry.getValue().equals(previous.get(entry.getKey()))) {
                appendRow(rows, time, entry.getKey(), entry.getValue());
            }
        }
        for (Property property : previous.keySet()) {
            if (!current.containsKey(property)) {
                appendRow(rows, time, property, "");
            }
        }
        writer.write(rows.toString());
        writer.flush();
        needsHeader = false;
        previous = current;
    }

    private static void appendRow(StringBuilder rows, Instant time, Property property, String value) {
        /*
        Добавляет строку CSV и экранирует кавычки в значении свойства.
        */
        rows.append(time).append(',').append(property.machine()).append(',').append(property.name())
                .append(",\"").append(value.replace("\"", "\"\"")).append("\"\r\n");
    }

    private static Map<Property, String> snapshot(List<CoffeeMachine> machines) {
        /*
        Собирает текущие значения свойств. Различает устройства по типу и номеру среди устройств того же типа.
        */
        Map<Property, String> values = new LinkedHashMap<>();
        Map<Class<?>, Integer> counts = new HashMap<>();
        for (CoffeeMachine machine : machines) {
            Class<?> type = machine.getClass();
            String id = type.getSimpleName() + "#" + counts.merge(type, 1, Integer::sum);
            put(values, id, "Manufacturer", machine.getManufacturer());
            put(values, id, "Model", machine.getModel());
            put(values, id, "WaterCapacity", machine.getWaterCapacity());
            put(values, id, "Water", machine.getWater());
            put(values, id, "Temperature", machine.getTemperature());
            put(values, id, "CoffeeType", machine.getCoffeeType());
            if (machine instanceof DripCoffeeMaker drip) {
                put(values, id, "GroundCapacity", drip.getGroundCapacity());
                put(values, id, "GroundCoffee", drip.getGroundCoffee());
                put(values, id, "Strength", drip.getStrength());
                put(values, id, "PortionVolume", drip.getPortionVolume());
            } else if (machine instanceof LatteMachine latte) {
                put(values, id, "MilkCapacity", latte.getMilkCapacity());
                put(values, id, "Milk", latte.getMilk());
                put(values, id, "MilkTemperature", latte.getMilkTemperature());
                put(values, id, "MilkRatio", latte.getMilkRatio());
                put(values, id, "CapsuleCapacity", latte.getCapsuleCapacity());
                put(values, id, "Capsules", latte.getCapsules());
                put(values, id, "FoamLevel", latte.getFoamLevel());
            } else if (machine instanceof GrinderCoffeeMachine grinder) {
                put(values, id, "BeanCapacity", grinder.getBeanCapacity());
                put(values, id, "Beans", grinder.getBeans());
                put(values, id, "WasteCapacity", grinder.getWasteCapacity());
                put(values, id, "GrindLevel", grinder.getGrindLevel());
                put(values, id, "Strength", grinder.getStrength());
            }
        }
        return values;
    }

    private static void put(Map<Property, String> values, String id, String name, Object value) {
        /*
        Сохраняет строковое значение свойства по ID устройства и имени свойства.
        */
        values.put(new Property(id, name), String.valueOf(value));
    }

    @Override
    public void close() throws IOException {
        /*
        Закрывает поток записи в журнал.
        */
        writer.close();
    }
}
