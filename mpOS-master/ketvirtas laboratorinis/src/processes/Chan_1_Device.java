package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class Chan_1_Device extends Process {
    public Chan_1_Device(){
        ID = "Chan_1_Device";
        PID = 4;
        PPID = 1;
        priority = 90;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
