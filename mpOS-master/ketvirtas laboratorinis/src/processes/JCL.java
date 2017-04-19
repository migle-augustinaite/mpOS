package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class JCL extends Process {
    public JCL(){
        ID = "JCL";
        PID = 11;
        PPID = 1;
        priority = 69;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
