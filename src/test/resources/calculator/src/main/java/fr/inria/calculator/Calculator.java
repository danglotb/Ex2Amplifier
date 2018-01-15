package fr.inria.calculator;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/10/17
 */
public class Calculator {

    private int currentValue;

    public Calculator(int value) {
        this.currentValue = value;
    }

    public void accumulate(int value) {
        if (this.currentValue % 3 == 0) {
            this.currentValue += value;
        } else {
            this.currentValue += 2 * value;
        }
    }

    public boolean compareTo(int value) {
        if (this.currentValue > value) {
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        if (this.currentValue % 5 == 0) {
            if (this.currentValue % 2 == 0) {
                this.currentValue = 0;
            }
        } else {
            if ((this.currentValue * 7) % 23 == 0) {
                this.currentValue += 21;
            }
        }
    }

    public int aggregate(Calculator calculator) {
        this.accumulate(calculator.currentValue);
        calculator.reset();
        calculator.accumulate(this.currentValue);
        this.reset();
        return this.currentValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }
}
