package lab5.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lab5.model.CoffeeMachine;
import lab5.model.CoffeeType;
import lab5.model.DripCoffeeMaker;
import lab5.model.GrinderCoffeeMachine;
import lab5.model.LatteMachine;
import lab5.model.MachineInventory;

public final class CoffeeMachineFileReader {
    /*
    Читает текстовый файл с кофемашинами в формате ЛР4 и собирает сведения о свойствах.
    */
    private static final int MAX_FILE_BYTES = 1024 * 1024;
    private static final Pattern BLOCKS = Pattern.compile(
                "\\b(DripCoffeeMaker|LatteMachine|GrinderCoffeeMachine)\\s*\\{([^{}]*)}");
    private static final Pattern FIELD = Pattern.compile("\\b(\\w+)[ \\t]*:[ \\t]*([^,;\\r\\n]*)");

    private int propertiesRead;
    private int propertiesMissing;
    private final List<String> problems = new ArrayList<>();

    public record Result(List<CoffeeMachine> machines, int propertiesRead, int propertiesMissing,
                         List<String> problems) {
        /*
        Хранит прочитанные кофемашины, счётчики свойств и сообщения об ошибках.
        */
        public Result {
            /*
            Копирует списки, чтобы их состав в результате не менялся при следующем чтении.
            */
            machines = List.copyOf(machines);
            problems = List.copyOf(problems);
        }
    }

    public Result read(Path file, boolean strict) throws IOException {
        /*
        Читает блоки кофемашин из файла UTF-8. Пропущенные свойства получают значения по умолчанию.

        Args:
            file: путь к входному файлу.
            strict: требуется ли полный корректный снимок для файлового режима.

        Returns:
            Список кофемашин и результаты проверки их свойств.
        */
        propertiesRead = 0;
        propertiesMissing = 0;
        problems.clear();
        List<CoffeeMachine> machines = new ArrayList<>();
        if (!Files.isRegularFile(file)) {
            throw new IOException("Обычный файл недоступен: " + file);
        }
        String text;
        try (InputStream input = Files.newInputStream(file)) {
            byte[] bytes = input.readNBytes(MAX_FILE_BYTES + 1);
            if (bytes.length > MAX_FILE_BYTES) {
                throw new IOException("Размер файла превышает 1 МиБ.");
            }
            text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new IOException("Ожидался текст в кодировке UTF-8.", e);
        }
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        Matcher matcher = BLOCKS.matcher(text);
        int end = 0;
        while (matcher.find()) {
            if (strict && !text.substring(end, matcher.start()).isBlank()) {
                throw new IOException("Повреждён или неизвестен блок кофемашины.");
            }
            end = matcher.end();
            if (machines.size() >= MachineInventory.MAX_MACHINES) {
                throw new IOException("В файле больше " + MachineInventory.MAX_MACHINES + " устройств.");
            }
            String type = matcher.group(1);
            CoffeeMachine machine;
            String[] specificFields;
            switch (type) {
                case "DripCoffeeMaker":
                    machine = new DripCoffeeMaker();
                    specificFields = new String[] {"GroundCapacity", "GroundCoffee", "Strength", "PortionVolume"};
                    break;
                case "LatteMachine":
                    machine = new LatteMachine();
                    specificFields = new String[] {"MilkCapacity", "Milk", "MilkTemperature",
                            "MilkRatio", "CapsuleCapacity", "Capsules", "FoamLevel"};
                    break;
                default:
                    machine = new GrinderCoffeeMachine();
                    specificFields = new String[] {"BeanCapacity", "Beans", "WasteCapacity", "GrindLevel", "Strength"};
                    break;
            }
            Map<String, String> fields = readFields(matcher.group(2), strict);
            String[] commonFields = {"Manufacturer", "Model", "WaterCapacity", "Water", "Temperature", "CoffeeType"};
            for (String field : commonFields) {
                readProperty(machine, fields, field);
            }
            for (String field : specificFields) {
                readProperty(machine, fields, field);
            }
            if (strict && !fields.isEmpty()) {
                throw new IOException(type + ": неизвестные свойства " + fields.keySet());
            }
            machines.add(machine);
        }
        if (strict && (machines.isEmpty() || !text.substring(end).isBlank())) {
            throw new IOException("Ожидались полные блоки кофемашин.");
        }
        if (strict && !problems.isEmpty()) {
            throw new IOException(problems.get(0));
        }
        return new Result(machines, propertiesRead, propertiesMissing, problems);
    }

    private Map<String, String> readFields(String text, boolean strict) throws IOException {
        /*
        Разбирает пары «имя: значение» внутри одного блока. В строгом режиме проверяет разделители и повторы.
        */
        Map<String, String> fields = new LinkedHashMap<>();
        Matcher matcher = FIELD.matcher(text);
        int end = 0;
        while (matcher.find()) {
            if (strict && !isFieldSeparator(text.substring(end, matcher.start()))) {
                throw new IOException("Неверный формат свойства в блоке кофемашины.");
            }
            end = matcher.end();
            String name = matcher.group(1);
            String previous = fields.put(name, matcher.group(2).strip());
            if (strict && previous != null) {
                throw new IOException("Свойство указано дважды: " + name);
            }
        }
        if (strict && !isFieldSeparator(text.substring(end))) {
            throw new IOException("Неверный формат свойства в блоке кофемашины.");
        }
        return fields;
    }

    private boolean isFieldSeparator(String text) {
        /*
        Проверяет, что между свойствами есть только пробельные символы, запятые или точки с запятой.
        */
        return text.replace(";", "").replace(",", "").isBlank();
    }

    private void readProperty(CoffeeMachine machine, Map<String, String> fields, String name) {
        /*
        Применяет свойство и обновляет счётчики чтения. Некорректное значение добавляет в список ошибок.
        */
        String value = fields.remove(name);
        if (value == null) {
            propertiesMissing++;
            return;
        }
        try {
            setProperty(machine, name, value);
            propertiesRead++;
        } catch (IllegalArgumentException e) {
            problems.add("Некорректное свойство " + name + ": " + value);
        }
    }

    private void setProperty(CoffeeMachine machine, String name, String value) {
        /*
        Преобразует значение и вызывает соответствующий метод кофемашины.
        Args:
          machine: изменяемая кофемашина.
          name: имя поддерживаемого свойства.
          value: строковое значение из файла.
        Raises:
          IllegalArgumentException: если значение имеет неверный формат
          или недопустимо для выбранного устройства.
         */
        switch (name) {
            case "Manufacturer": machine.setManufacturer(value); return;
            case "Model": machine.setModel(value); return;
            case "WaterCapacity": machine.setWaterCapacity(Integer.parseInt(value)); return;
            case "Water":
                int water = Integer.parseInt(value);
                if (water != 0) machine.refillWater(water);
                return;
            case "Temperature": machine.setTemperature(Integer.parseInt(value)); return;
            case "CoffeeType": machine.selectCoffee(CoffeeType.valueOf(value)); return;
        }
        int number = Integer.parseInt(value);
        if (machine instanceof DripCoffeeMaker drip) {
            switch (name) {
                case "GroundCapacity": drip.setGroundCapacity(number); break;
                case "GroundCoffee": if (number != 0) drip.addGroundCoffee(number); break;
                case "Strength": drip.chooseStrength(number); break;
                case "PortionVolume": drip.choosePortion(number); break;
            }
        } else if (machine instanceof LatteMachine latte) {
            switch (name) {
                case "MilkCapacity": latte.setMilkCapacity(number); break;
                case "Milk": if (number != 0) latte.refillMilk(number); break;
                case "MilkTemperature": latte.chooseMilkTemperature(number); break;
                case "MilkRatio": latte.chooseMilkRatio(number); break;
                case "CapsuleCapacity": latte.setCapsuleCapacity(number); break;
                case "Capsules": if (number != 0) latte.addCapsules(number); break;
                case "FoamLevel": latte.adjustFoam(number); break;
            }
        } else if (machine instanceof GrinderCoffeeMachine grinder) {
            switch (name) {
                case "BeanCapacity": grinder.setBeanCapacity(number); break;
                case "Beans": if (number != 0) grinder.addBeans(number); break;
                case "WasteCapacity": grinder.setWasteCapacity(number); break;
                case "GrindLevel": grinder.chooseGrind(number); break;
                case "Strength": grinder.chooseStrength(number); break;
            }
        }
    }
}
