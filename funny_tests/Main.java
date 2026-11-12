package funny_tests;

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Бобик", 3);
        Cat cat = new Cat("Мурка", 2);

        System.out.println("Имя собаки: " + dog.getName());
        // System.out.println(dog.name); // Ошибка компиляции: private.
        dog.showAge();

        System.out.println("Возраст из Main: " + dog.age);

        dog.eat();
        dog.eat("мясо");

        Animals first = dog;
        Animals second = cat;

        first.makeSound();
        second.makeSound();
    }
}
