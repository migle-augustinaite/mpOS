package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class StartStop extends Process {

    public StartStop(){
        ID = "StartStop";
        PID = 1;
        PPID = -42; //No parents :(
        priority = 100;

        STATE = State.READY;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
