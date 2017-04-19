package processes;

/**
 * Created by blitZ on 4/7/2017.
 */
public class GetPutData extends Process{
    public GetPutData(){
        ID = "GetPutData";
        PID = 6;
        PPID = 1;
        priority = 85;

        STATE = State.BLOCKED;
    }

    @Override
    public void doProcess() {
        STATE = State.RUNNING;







        STATE = State.BLOCKED;
    }
}
