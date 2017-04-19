package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class Loader extends Process{
    public Loader(){
        ID = "Loader";
        PID = 3;
        PPID = 1;
        priority = 96;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
