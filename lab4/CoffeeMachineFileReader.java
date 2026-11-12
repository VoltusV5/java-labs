import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoffeeMachineFileReader {
    private final InputStream input;
    private int propertiesRead;
    private int propertiesMissing;
    private int propertiesInvalid;

    public CoffeeMachineFileReader(InputStream input) {
        /*
        Создаёт читатель данных кофемашин.

        Args:
            input: поток с текстовыми данными.
         */
        this.input = input;
    }

    public int getPropertiesRead() {
        /*
        Возвращает количество успешно прочитанных свойств.
        */
        return propertiesRead;
    }

    public int getPropertiesMissing() {
        /*
        Возвращает количество отсутствующих свойств.
        */
        return propertiesMissing;
    }

    public int getPropertiesInvalid() {
        /*
        Возвращает количество свойств с некорректными значениями.
        */
        return propertiesInvalid;
    }

    public List<CoffeeMachine> readMachines() throws IOException {
        /*
        Читает поток, находит блоки известных классов и создаёт кофемашины.

        Returns:
          Список созданных кофемашин.
        Raises:
          IOException: если не удалось прочитать поток.
        */
        propertiesRead = 0;
        propertiesMissing = 0;
        propertiesInvalid = 0;
        List<CoffeeMachine> machines = new ArrayList<>();
        String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Pattern blocks = Pattern.compile(
                "\\b(DripCoffeeMaker|LatteMachine|GrinderCoffeeMachine)\\s*\\{([^{}]*)}");
        Matcher matcher = blocks.matcher(text);
        while (matcher.find()) {
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
            Map<String, String> fields = readFields(matcher.group(2));
            String[] commonFields = {"Manufacturer", "Model", "WaterCapacity", "Water", "Temperature", "CoffeeType"};
            for (String field : commonFields) {
                readProperty(machine, fields, field);
            }
            for (String field : specificFields) {
                readProperty(machine, fields, field);
            }
            machines.add(machine);
        }
        return machines;
    }

    private Map<String, String> readFields(String text) {
        /*
        Находит пары «имя: значение» в тексте одного блока.
        Args:
          text: содержимое блока без фигурных скобок.
        Returns:
          Таблица имён свойств и их строковых значений.
         */
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = Pattern.compile("\\b(\\w+)[ \\t]*:[ \\t]*([^,;\\r\\n]*)").matcher(text);
        while (matcher.find()) {
            fields.put(matcher.group(1), matcher.group(2).strip());
        }
        return fields;
    }

    private void readProperty(CoffeeMachine machine, Map<String, String> fields, String name) {
        /*
        Применяет одно ожидаемое свойство и обновляет счётчики чтения.

        Args:
          machine: кофемашина, для которой читается свойство.
          fields: найденные в блоке имена и значения.
          name: имя ожидаемого свойства.
         */
        if (!fields.containsKey(name)) {
            propertiesMissing++;
            return;
        }
        try {
            setProperty(machine, name, fields.get(name));
            propertiesRead++;
        } catch (IllegalArgumentException e) {
            propertiesInvalid++;
            System.out.println("Некорректное свойство " + name + ": " + fields.get(name));
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
