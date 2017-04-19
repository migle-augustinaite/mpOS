package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class JobGovernor extends Process{
    public JobGovernor(){
        ID = "JobGovernor";
        PID = 2;
        PPID = 1;
        priority = 99;

        STATE = State.READY;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
