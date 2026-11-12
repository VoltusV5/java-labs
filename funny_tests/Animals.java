package funny_tests;

public class Animals {
    private String name;
    protected int age;

    public Animals(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void makeSound() {
        System.out.println("**Звук**");
    }

    public void eat() {
        System.out.println(name + " ест");
    }

    public void eat(String food) {
        System.out.println(name + " ест: " + food);
    }
}
