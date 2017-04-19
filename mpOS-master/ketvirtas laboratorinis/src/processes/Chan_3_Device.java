package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class Chan_3_Device extends Process{
    public Chan_3_Device(){
        ID = "Chan_3_Device";
        PID = 8;
        PPID = 1;
        priority = 65;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
