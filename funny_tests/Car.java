package funny_tests;

public class Car {
    private String model;

    public Car(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void drive() {
        System.out.println(model + " едет");
    }

    public void drive(String speed) {
        System.out.println(model + " едет со скоростью " + speed);
    }

}