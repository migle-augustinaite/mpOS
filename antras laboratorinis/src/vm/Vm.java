package vm;

import Rm.InterruptType;
import testTools.Constants;
import testTools.Test;
import utils.OsLogger;

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
        //System.out.println("ADRR");
        StringBuilder sb = new StringBuilder();
        long temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        temp += temp2;

        sb.append("; result: " + temp);

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

        OsLogger.writeToLog("ADRR; " + sb);
    }

    public void ad(int x, int y) {
        StringBuilder sb = new StringBuilder();
        //System.out.println("AD " + x + y);
        long temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp2 = buffer.getInt();
        sb.append(temp2);

        temp += temp2;

        sb.append("; result: " + temp);

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
        OsLogger.writeToLog("AD " + x + " " + y + "; " + sb);
    }

    public void sbrr() {
        StringBuilder sb = new StringBuilder();

        //System.out.println("SBRR");
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values ");
        sb.append(temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();
        sb.append(temp2);

        temp -= temp2;

        sb.append("; result: " + temp);
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
        OsLogger.writeToLog("SBRR; " + sb);
    }

    public void sb(int x, int y) {
        StringBuilder sb = new StringBuilder();
        //System.out.println("SB " + x + y);
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp2 = buffer.getInt();
        temp -= temp2;

        sb.append(temp2);
        sb.append("; result: " + temp);
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
        OsLogger.writeToLog("SB" + x + " " + y  + "; " + sb);
    }

    public void mlrr() {
        StringBuilder sb = new StringBuilder();
        long temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        temp = temp * temp2;

        sb.append("; result: " + temp);
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
        OsLogger.writeToLog("MLRR; " + sb);
    }

    public void ml(int x, int y) {
        //System.out.println("ML " + x + y);
        StringBuilder sb = new StringBuilder();
        long temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        try {
            buffer = ByteBuffer.wrap(getData(x, y));
            temp2 = buffer.getInt();
            temp *= temp2;

            sb.append(temp2);
            sb.append("; result: "+ temp);

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
        OsLogger.writeToLog("ML" + x + " " + y + "; " + sb);

    }

    public void dvrr(){
        //System.out.println("DVRR");
        StringBuilder sb = new StringBuilder();
        int temp, temp2, temp3;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        if (temp2 == 0) {
            //TODO:set interupt to division by zero
        } else {
            buffer = ByteBuffer.allocate(4);
            buffer.putInt(temp / temp2);
            temp3 = buffer.getInt();

            sb.append("; result: " + temp3 + ", ");

            r1.data = ByteBuffer.allocate(4).putInt(temp3).array();
            buffer = ByteBuffer.allocate(4);
            buffer.putInt((int) temp % temp2);

            temp3 = buffer.getInt();

            sb.append(temp3);

            r2.data = ByteBuffer.allocate(4).putInt(temp3).array();
            if (temp == 0) {
                sf.setZf(1);
            }
        }

        OsLogger.writeToLog("DVRR; " + sb);
    }

    public void dv(int x, int y){
        //System.out.println("DV " + x + y);
        StringBuilder sb = new StringBuilder();
        int temp, temp2, temp3;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        try {
            buffer = ByteBuffer.wrap(getData(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }

        temp2 = buffer.getInt();

        sb.append(temp2);

        if (temp2 == 0) {
            //TODO:set interupt to division by zero
        } else {
            //buffer.clear();
            //buffer = ByteBuffer.allocate(5);
            //buffer.putInt(temp / temp2);
            temp3 = temp / temp2;

            sb.append("; result: " + temp3 + ", ");

            r1.data = ByteBuffer.allocate(4).putInt(temp3).array();
            //buffer = ByteBuffer.allocate(4);
            //buffer.putInt((int) temp % temp2);

            temp3 = (int) temp % temp2;

            sb.append(temp3);

            r2.data = ByteBuffer.allocate(4).putInt(temp3).array();
            if (temp == 0) {
                sf.setZf(1);
            }
        }

        OsLogger.writeToLog("DV" + x + " " + y + "; " + sb);

    }

    public void and() {
        StringBuilder sb = new StringBuilder();
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: ");
        sb.append(temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        temp = temp & temp2;

        sb.append("; result: " + temp);

        //buffer.putInt(temp);
        r1.data = ByteBuffer.allocate(4).putInt(temp).array();
        if (temp == 0) {
            sf.setZf(1);
        }

        OsLogger.writeToLog("AND: " + sb);
    }

    public void or() {
        StringBuilder sb = new StringBuilder();
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: " + temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        temp = temp | temp2;

        sb.append("; result: " + temp);

//        buffer.putInt(temp);
        r1.data = ByteBuffer.allocate(4).putInt(temp).array();
        if (temp == 0) {
            sf.setZf(1);
        }

        OsLogger.writeToLog("OS: " + sb);
    }

    public void xor() {
        StringBuilder sb = new StringBuilder();
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: " + temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        temp = temp ^ temp2;

        sb.append("; result: " + temp);

        //buffer.putInt(temp);
        r1.data = ByteBuffer.allocate(4).putInt(temp).array();
        if (temp == 0) {
            sf.setZf(1);
        }

        OsLogger.writeToLog("XOR: " + sb);
    }

    public void not() {
        StringBuilder sb = new StringBuilder();
        int temp;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();
        sb.append("value: " + temp);

        temp = ~temp;

        sb.append("; result: " + temp);

//        buffer.putInt(temp);
        r1.data = ByteBuffer.allocate(4).putInt(temp).array();
        if (temp == 0) {
            sf.setZf(1);
        }

        OsLogger.writeToLog("NOT; " + sb);
    }

    public void cmp() {
        StringBuilder sb = new StringBuilder();
        int temp, temp2;
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        temp = buffer.getInt();

        sb.append("values: " + temp + ", ");

        buffer = ByteBuffer.wrap(r2.data);
        temp2 = buffer.getInt();

        sb.append(temp2);

        if (temp < temp2) {

            sb.append("; result: lower");

            sf.setZf(0);
            sf.setCf(1);
        } else if (temp > temp2) {

            sb.append("; result: higher");

            sf.setZf(0);
            sf.setCf(0);
        } else {

            sb.append("; result: equal");

            sf.setZf(1);
        }
    }

    public void lw(int x, int y) {
        StringBuilder sb = new StringBuilder();
        //System.out.println("LW " + x + y);
        int temp = 0;
        ByteBuffer buffer = null;
        try {
            buffer = ByteBuffer.wrap(getData(x, y));
            temp = buffer.getInt();
            sb.append("value: " + temp + " or " + new String(Test.intToBytes(temp, 4)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        r1.data = Test.intToBytes(temp, 4);

        OsLogger.writeToLog("LW" + x + " " + y + "; " + sb);
    }

    public void sw(int x, int y) {
        StringBuilder sb = new StringBuilder();
        //System.out.println("SW " + x + y);

        sb.append("register: " + r1.getDataInt() + " or " + new String(r1.data));

        try {
            saveData(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OsLogger.writeToLog("SW" + x + " " + y + "; " + sb);
    }

    public void mov1() {
        ByteBuffer buffer = ByteBuffer.wrap(r1.data);
        r2.data = buffer.array();

        OsLogger.writeToLog("MOV1; " + "r1: " + r1.getDataInt() + "; r2: " + r2.getDataInt());
    }

    public void mov2() {
        ByteBuffer buffer = ByteBuffer.wrap(r2.data);
        r1.data = buffer.array();

        OsLogger.writeToLog("MOV1; " + "r1: " + r1.getDataInt() + "; r2: " + r2.getDataInt());
    }

    public void prnt() {
        StringBuilder sb = new StringBuilder();
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
            sb.append(bytes);
            System.out.println(new String(bytes));
        }

        OsLogger.writeToLog("PRNT: " + sb);
    }

    public void prns() {
        System.out.println(r1.getDataInt());

        OsLogger.writeToLog("PRNS: " + r1.getDataInt());
    }

    public void push() {

    }

    public void pop() {

    }

    public void jm(int x, int y) {
        ic = 16 * x + y;

        OsLogger.writeToLog("JM" + x + " " + y + "; ic: " + ic);
    }

    public void je(int x, int y) {
        int oldIc = ic;
        if(sf.getZf()){
            ic = 16 * x + y;
        }
        OsLogger.writeToLog("JE" + x + " " + y + "; old ic: " + oldIc + ", new ic: " + ic);
    }

    public void ja(int x, int y) {
        int oldIc = ic;
        if(!sf.getCf() && !sf.getZf()){
            ic = 16 * x + y;
        }
        OsLogger.writeToLog("JA" + x + " " + y + "; old ic: " + oldIc + ", new ic: " + ic);
    }

    public void jl(int x, int y) {
        int oldIc = ic;
        if(sf.getCf()){
            ic = 16 * x + y;
        }
        OsLogger.writeToLog("JL" + x + " " + y + "; old ic: " + oldIc + ", new ic: " + ic);
    }
    public void fo(int x, int y) {
        int pos = rm.getFilePos(x, y, true);
        if(pos >= 0)
            r1.data = Test.intToBytes(pos, 4);
        else
            Rm.Rm.setPI(InterruptType.INCORRECT_FILE_NAME);

        OsLogger.writeToLog("FO; handler: " + r1.getDataInt());
    }

    public void fc() {
        rm.closeFile(r1.data);
        if(r1.getDataInt() < 0){
            Rm.Rm.setPI(InterruptType.INCORRECT_FILE_HANLDE);
        }
        OsLogger.writeToLog("FC; handler: " + r1.getDataInt());
    }

    public void fd() {
        OsLogger.writeToLog("FD; handler: " + r1.getDataInt());
        if(r1.getDataInt() < 0){
            Rm.Rm.setPI(InterruptType.INCORRECT_FILE_HANLDE);
        } else
            rm.deleteFile(r1.data);
    }

    public void fr(int x, int y) {
        //r2.data = Test.intToBytes(1, 4);
        if(r1.getDataInt() < 0){
            Rm.Rm.setPI(InterruptType.INCORRECT_FILE_HANLDE);
        } else
            rm.fileRead(r1.data, x, y, r2.data, this);

        OsLogger.writeToLog("FR" + x + ", " + y + "; handler: " + r1.getDataInt());
    }

    public void fw(int x, int y) {
        if(r1.getDataInt() < 0){
            Rm.Rm.setPI(InterruptType.INCORRECT_FILE_HANLDE);
        } else {
            try {
                byte[] data = getData(x, y);
                if (r2.getDataInt() < 255)
                    rm.fileWrite(r1.data, data, r2.data);
                else
                    System.out.println("File is not big enough");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        OsLogger.writeToLog("FW" + x + ", " + y + "; handler: " + r1.getDataInt());
    }

    public void halt() {
        //System.out.println("HALT");
        OsLogger.writeToLog("HALT");
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
