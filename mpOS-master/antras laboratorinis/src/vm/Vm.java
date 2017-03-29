package vm;

import testTools.Constants;
import testTools.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by irmis on 2017.03.15.
 */
public class Vm {
    public VmRegister ptr;
    public VmRegister r1;
    public VmRegister r2;
    public VmRegister ds;
    public VmRegister cs;
    public VmRegister sp;

    public VmStatusFlag sf;

    public int ic;

    Rm.Rm rm = null;

    public Vm(Rm.Rm rm) {
        this.rm = rm;
        ptr = new VmRegister(4, "puslapiavimo lentele");
        for (int i = 0; i < 16; i++) {
            if (rm.memory.getInt(Test.bytesToInt(rm.ptr.data), i) == 0) {
                ptr.data = Test.intToBytes(i, 4);
            }
        }
        sp = new VmRegister(1, "sp");
        r1 = new VmRegister(4, "register 1");
        r2 = new VmRegister(4, "register 2");
        ds = new VmRegister(4, "data segment");
        cs = new VmRegister(4, "code segment");
        sf = new VmStatusFlag();
        ds.data = Test.intToBytes(0, 4);
        ic = 0;
    }

    public Vm(Rm.Rm rm, byte[][] descriptor){
        this(rm);
        ptr.data = descriptor[Constants.VM_PTR_INDEX];
        sp.data = descriptor[Constants.VM_SP_INDEX];
        ds.data = descriptor[Constants.VM_DS_INDEX];
        cs.data = descriptor[Constants.VM_CS_INDEX];
        r1.data = descriptor[Constants.VM_R1_INDEX];
        r2.data = descriptor[Constants.VM_R2_INDEX];
    }

    public void adrr() {
        System.out.println("ADRR");
        long temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp += buffer.getInt();
        if (temp > Integer.MAX_VALUE) {
            temp -= Integer.MAX_VALUE;
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void ad(int x, int y) {
        System.out.println("AD " + x + y);
        long temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp += buffer.getInt();
        if (temp > Integer.MAX_VALUE) {
            temp -= Integer.MAX_VALUE;
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void sbrr() {
        System.out.println("SBRR");
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp -= buffer.getInt();
        if (temp < 0) {
            temp += Integer.MAX_VALUE;
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void sb(int x, int y) {
        System.out.println("SB " + x + y);
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp -= buffer.getInt();
        if (temp < 0) {
            temp += Integer.MAX_VALUE;
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void mlrr() {
        long temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp = temp * buffer.getInt();
        if (temp > Integer.MAX_VALUE) {
            temp = temp - (Integer.MAX_VALUE * (temp / Integer.MAX_VALUE));
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void ml(int x, int y) {
        System.out.println("ML " + x + y);
        long temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (temp > Integer.MAX_VALUE) {
            temp = temp - (Integer.MAX_VALUE * (temp / Integer.MAX_VALUE));
            sf.setCf(1);
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }

    }

    public void dvrr(){
        System.out.println("DVRR");
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        if (buffer.getInt() == 0) {
            //TODO:set interupt to division by zero
        } else {
            buffer = ByteBuffer.allocate(4);
            buffer.putInt(temp / buffer.getInt());
            r1.data = buffer.array();
            buffer = ByteBuffer.allocate(4);
            buffer.putInt((int) temp % buffer.getInt());
            r2.data = buffer.array();
            if (temp == 0) {
                sf.setZf(1);
            }
        }
    }

    public void dv(int x, int y){
        System.out.println("DV " + x + y);
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (buffer.getInt() == 0) {
            //TODO:set interupt to division be zero
        } else {
            buffer = ByteBuffer.allocate(4);
            buffer.putInt(temp / buffer.getInt());
            r1.data = buffer.array();
            buffer = ByteBuffer.allocate(4);
            buffer.putInt((int) temp % buffer.getInt());
            r2.data = buffer.array();
            if (temp == 0) {
                sf.setZf(1);
            }
        }

    }

    public void and() {
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp = temp & buffer.getInt();
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void or() {
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp = temp | buffer.getInt();
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void xor() {
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        temp = temp ^ buffer.getInt();
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void not() {
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = ~buffer.getInt();
        buffer.putInt(temp);
        r1.data = buffer.array();
        if (temp == 0) {
            sf.setZf(1);
        }
    }

    public void cmp() {
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        buffer = ByteBuffer.wrap(r2.data);
        if (temp < buffer.getInt()) {
            sf.setZf(0);
            sf.setCf(1);
        } else if (temp > buffer.getInt()) {
            sf.setZf(0);
            sf.setCf(0);
        } else {
            sf.setZf(1);
        }
    }

    public void lw(int x, int y) {
        System.out.println("LW " + x + y);
        ByteBuffer buffer = null;
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        r1.data = buffer.array();
    }

    public void sw(int x, int y) {
        System.out.println("SW " + x + y);
        try {
            saveData(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mov1() {
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        r2.data = buffer.array();
    }

    public void mov2() {
        ByteBuffer buffer = ByteBuffer.wrap(r2.data);
        r1.data = buffer.array();
    }

    public void prnt() {
        ByteBuffer buffer = null;
        byte[] bytes = null;
        try {
            buffer = ByteBuffer.wrap(r2.data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int length = buffer.getInt();

        for(int i = 0; i < length; i++){
            try {
                bytes = getData((r1.getDataInt() + i)/16, (r1.getDataInt() + i)%16);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(new String(bytes));
        }
    }

    public void prns() {
        System.out.println(r1.getDataInt());
    }

    public void push() {

    }

    public void pop() {

    }

    public void jm(int x, int y) {
        ic = 16 * x + y;
    }

    public void je(int x, int y) {
        if(sf.getZf()){
            ic = 16 * x + y;
        }
    }

    public void ja(int x, int y) {
        if(!sf.getCf() && !sf.getZf()){
            ic = 16 * x + y;
        }
    }

    public void jl(int x, int y) {
        if(sf.getCf()){
            ic = 16 * x + y;
        }
    }
    public void fo(int x, int y) {
        int pos = rm.getFilePos(x, y, true);
        r1.data = Test.intToBytes(pos, 4);
    }

    public void fc() {
        rm.closeFile(r1.data);
    }

    public void fd() {

    }

    public void fr(int x, int y) {
        //r2.data = Test.intToBytes(1, 4);
        rm.fileRead(r1.data, x, y, r2.data, this);
    }

    public void fw(int x, int y) {
        try {
            byte[] data  = getData(x, y);
            if(r2.getDataInt() < 255)
                rm.fileWrite(r1.data, data, r2.data);
            else
                System.out.println("File is not big enough");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void halt() {
        System.out.println("HALT");
    }

    private byte[] getData(int x, int y) throws Exception {
        byte[][] rmPtrTable = rm.memory.memory[Rm.Rm.ptr.getDataInt()];
        byte[][] vmPtrTable = rm.memory.memory[Test.bytesToInt(rmPtrTable[ptr.getDataInt()])];
        byte[][] temp;
        int pos = (x * 16 + y) * 2;
        if(pos >= cs.getDataInt()){
            throw new Exception("Going too far");
        }
        x = pos / 16;
        y = pos % 16;
        temp = rm.memory.memory[Test.bytesToInt(vmPtrTable[x])];
        return temp[y];
    }

    private void saveData(int x, int y) throws Exception {
        byte[][] rmPtrTable = rm.memory.memory[Rm.Rm.ptr.getDataInt()];
        byte[][] vmPtrTable = rm.memory.memory[Test.bytesToInt(rmPtrTable[ptr.getDataInt()])];
        int pos = (x * 16 + y) * 2;
        if(pos >= cs.getDataInt()){
            throw new Exception("Going too far");
        }
        rm.memory.memory[Test.bytesToInt(vmPtrTable[pos / 16])][pos % 16] = r1.data;

    }
    public void saveData(int x, int y, byte[] bytes) throws Exception {
        byte[][] rmPtrTable = rm.memory.memory[Rm.Rm.ptr.getDataInt()];
        byte[][] vmPtrTable = rm.memory.memory[Test.bytesToInt(rmPtrTable[ptr.getDataInt()])];
        int pos = (x * 16 + y) * 2;
        if(pos >= cs.getDataInt()){
            throw new Exception("Going too far");
        }
        rm.memory.memory[Test.bytesToInt(vmPtrTable[pos / 16])][pos % 16] = bytes;

    }

}
