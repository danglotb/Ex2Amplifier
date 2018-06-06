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

    public int compute(int value, int value2) {
        if (this.currentValue == value || this.currentValue == value2) {
            return 0;
        }
        if (value > value2) {
            this.currentValue = 7;
        } else {
            this.currentValue = 0;
        }
        if (this.currentValue * value == 701 && value2 + value > this.currentValue) {
            return 23;
        }
        if (value + this.currentValue - 15 > value2) {
            if (value < value2 + 3) {
                this.currentValue++;
            } else {
                this.currentValue--;
            }
        } else if (this.currentValue < value2 || this.currentValue > value + 72) {
            return this.currentValue++;
        } else {
            return this.currentValue--;
        }
        return this.currentValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }
}
