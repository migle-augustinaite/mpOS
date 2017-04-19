package processes;

import java.lang.*;

/**
 * Created by blitZ on 4/7/2017.
 */
public class ProcessKiller extends java.lang.Process {
    public ProcessKiller(){
        ID = "ProcessKiller";
        PID = 9;
        PPID = 1;
        priority = 89;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
