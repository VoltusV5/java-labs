public interface Soundble {
    void sound();
}

public class Animal {
    private String name;
    protected int age;

    public void eat() {
        System.out.println("Животное ест");
    }

    public boolean eat() {

    }

    public int eat(String food) {
        System.out.println("Животное ест " + food);
        return 1;
    }

    public int eat(int a, short b) {
        System.out.println("Животное ест " + food);
        return 1;
    }

    public int eat(short a, int b) {
        System.out.println("Животное ест " + food);
        return 1;
    }

    public String eat(String wrwer) {
        System.out.println("Животное ест " + food);
    }
}

public class Cat extends Animal implements Soundble {
    
    @Override
    public void eat() {
        System.out.println("Кошка решила поесть");
    }
    @Override
    public void sound() {
        System.out.println("Мяу");
    }
    
}


public abstract class Car {
    public abstract void start();
    public abstract void stop();
}

public class TeslaCar extends Car , Cat{
    @Override
    public void start() {
        System.out.println("TeslaCar is starting silently.");
    }

    @Override
    public void stop() {
        System.out.println("TeslaCar is stopping.");
    }
}