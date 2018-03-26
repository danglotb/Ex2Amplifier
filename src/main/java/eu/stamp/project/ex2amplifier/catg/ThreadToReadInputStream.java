package eu.stamp.project.ex2amplifier.catg;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
class ThreadToReadInputStream extends Thread {

    private final PrintStream output;
    private final InputStream input;

    ThreadToReadInputStream(PrintStream output, InputStream input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public synchronized void start() {
        int read;
        try {
            while ((read = this.input.read()) != -1) {
                this.output.print((char) read);
            }
        } catch (Exception ignored) {
            //ignored
        } finally {
            this.interrupt();
        }
    }
}
