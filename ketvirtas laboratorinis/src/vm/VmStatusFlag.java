package vm;

/**
 * Created by irmis on 2017.03.15.
 */
public class VmStatusFlag {
    byte sf;

    public boolean getCf() {
        return (sf & 4) > 0;
    }

    public void setCf(int cf) {
        if (cf > 0) {
            sf = (byte) (sf | 4);
        } else {
            sf = (byte) (sf & 251);
        }
    }

    public boolean getOf() {
        return (sf & 1) > 0;
    }

    public void setOf(int of) {
        if (of > 0) {
            sf = (byte) (sf | 1);
        } else {
            sf = (byte) (sf & 254);
        }
    }

    public boolean getZf() {
        return (sf & 2) > 0;
    }

    public void setZf(int zf) {
        if (zf > 0) {
            sf = (byte) (sf | 2);
        } else {
            sf = (byte) (sf & 253);
        }
    }

    public VmStatusFlag() {
        this.sf = 0;
    }

    public void print() {
        String temp = "00000000" + Integer.toBinaryString(sf);
        System.out.println(temp.substring(temp.length() - 8));
    }
}
