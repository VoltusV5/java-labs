package funny_tests;

public class Dog extends Animals {
    public Dog(String name, int age) {
        super(name, age);
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + ": Гав!");
    }

    public void showAge() {
        System.out.println("Возраст собаки: " + age);
        // System.out.println(name); // Ошибка компиляции: name — private.
    }
}
