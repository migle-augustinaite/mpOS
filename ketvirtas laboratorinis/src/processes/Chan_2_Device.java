package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class Chan_2_Device extends Process {
    public Chan_2_Device(){
        ID = "Chan_2_Device";
        PID = 7;
        PPID = 1;
        priority = 70;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
