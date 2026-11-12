package funny_tests;

public class TeslaCar extends Car {
    public TeslaCar(String model) {
        super(model);
    }

    @Override
    public void drive() {
        System.out.println(getModel() + " едет бесшумно");
    }

    @Override
    public void drive(String speed) {
        System.out.println(getModel() + " едет бесшумно со скоростью " + speed);
    }
    
}

    