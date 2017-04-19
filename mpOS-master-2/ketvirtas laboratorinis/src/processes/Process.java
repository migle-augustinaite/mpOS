package processes;

import Rm.RmRegister;
import Rm.RmStatusFlag;

/**
 * Created by blitZ on 4/7/2017.
 */
public abstract class Process {
    String ID;
    int PID;
    int PPID;
    int RES;
    int CRES;
    int priority;

    State STATE;

    public RmRegister PTR;
    public RmRegister R1;
    public RmRegister R2;
    public RmRegister DS;
    public RmRegister CS;
    public RmStatusFlag SF;

    public int IC;

    public abstract void doProcess();
}
