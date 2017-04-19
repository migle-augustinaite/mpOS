package processes;

import java.lang.*;

/**
 * Created by blitZ on 4/7/2017.
 */
public class VM extends java.lang.Process {
    public VM(){
        ID = "VM";
        PID = 12;
        PPID = 1;
        priority = 50;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
