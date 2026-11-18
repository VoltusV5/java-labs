class Animal {
    String name;

    Animal(String name) {
        this.name = name;
    }

    void speak() {
        System.out.println(name);
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Гав");
    }
}

class Cat extends Animal {
    Cat(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Мяу");
    }
}

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Шарик");
        dog.speak();
        Cat cat = new Cat("Мурзик");
        cat.speak();
    }
}