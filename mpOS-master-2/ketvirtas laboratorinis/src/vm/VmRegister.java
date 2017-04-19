package vm;

/**
 * Created by irmis on 2017.03.15.
 */
public class VmRegister extends Rm.RmRegister {
    public int blocksUsed = 0;

    public VmRegister(int size, String name) {
        super(size, name);
    }
}
