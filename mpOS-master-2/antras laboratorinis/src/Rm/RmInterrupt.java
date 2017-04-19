package Rm;

/**
 * Created by blitZ on 3/8/2017.
 */
public class RmInterrupt extends Exception {
    public InterruptType type;

    public RmInterrupt(InterruptType type) {
        super();
        this.type = type;
    }
}
