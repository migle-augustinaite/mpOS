package Rm;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import testTools.Constants;
import testTools.Test;
import utils.OsLogger;
import vm.Vm;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Created by blitZ on 3/8/2017.
 */
public class Rm {
    public static boolean stepMode = false;

    RmRegister mode;
    public static RmRegister ptr;
    RmRegister r1;
    RmRegister r2;
    RmRegister ch1;
    RmRegister ch2;
    RmRegister ch3;

    static RmInterrupt si;
    static RmInterrupt pi;

    public HDD hdd;
    public Memory memory;

    public RmStatusFlag sf;

    int timer = 10;
    int ic;
    int ti;

    public Rm() {
        mode = new RmRegister(1, "mode");
        ptr = new RmRegister(2, "ptr");
        r1 = new RmRegister(4, "register 1");
        r2 = new RmRegister(4, "register 2");
        ch1 = new RmRegister(1, "channel 1");
        ch2 = new RmRegister(1, "channel 2");
        ch3 = new RmRegister(1, "channel 3");
        si = new RmInterrupt(InterruptType.NO_INTERRUPT);
        pi = new RmInterrupt(InterruptType.NO_INTERRUPT);
        sf = new RmStatusFlag();
        ic = 0;
        ti = 10;
        hdd = new HDD();
        memory = new Memory(this);
    }

    public void start(String programName) {
        byte[][] vmDescriptor = getVmDescriptor(programName);
        if (vmDescriptor == null) {
            //System.out.println("No such program");
            OsLogger.writeToLog("No program named " + programName + " to start");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        Vm vm = new Vm(this, vmDescriptor);
        int[] codeBeginning = getCodeBeginning(vm);
        int[] indexes;
        //System.out.println("row: " + codeBeginning[0]);
        //System.out.println("column: " + codeBeginning[1]);
        int row = codeBeginning[0];
        int column = codeBeginning[1];
        byte[] command;
        boolean cont;// continue
        while (true) {
            command = getCommand(vm, row, column);
            if(stepMode){
                showRegister(vm);
                System.out.println("Next command: " + new String(command));
                while(true) {
                    String line = scanner.nextLine();
                    if (line.toUpperCase().equals("SHOW CS")) {
                        showCseg(programName);
                    } else if (line.toUpperCase().equals("SHOW DS")) {
                        showDseg(programName);
                    } else if (line.toUpperCase().equals("FINISH")) {
                        stepMode = false;
                        break;
                    }
                    if (line.equals("")) {
                        break;
                    }
                }
            }
            cont = executeCommand(vm, command);
            vm.ic++;
            timer--;
            if (!cont) {
                break;
            }
            indexes = getNextIndexes(vm);
            row = indexes[0];
            column = indexes[1];
            test(vm);
        }
    }

    public void load(String programName) throws Exception {
        if (getVmDescriptor(programName) != null) {
            setSI(InterruptType.DUPLICATE_NAME);
            OsLogger.writeToLog("There already is program loaded with this name " + programName);
            return;
        }

        int ret = 0;
        int vmPtr = getVmPtr();// assign space in rm ptr. Value can be from 0 to 15
        if (vmPtr == -1) {
            setSI(InterruptType.OUT_OF_MEMORY);
            OsLogger.writeToLog("Out of space to add vm to pages table, vm name is " + programName);
            return;
        }
        Vm vm = new Vm(this);// create vm only after we know that there are enough space in rm ptr
        byte[][] rmTable = memory.memory[Test.bytesToInt(Rm.ptr.data)];
        vm.ptr.data = Test.intToBytes(vmPtr, 4);
        rmTable[vmPtr] = Test.intToBytes(memory.getFreeBlock(), 4);
        try {
            long pos = findFilePos(programName);
            if(pos < 0) {
                setSI(InterruptType.INCORRECT_FILE_NAME);
                OsLogger.writeToLog("No program named " + programName);
                return;
            }
            while (true) {
                String line = hdd.file.readLine();
                ret = memory.addToVm(line, vm, programName, ret);
                if (ret == Constants.HALT)
                    break;
                else if(ret == Constants.NO_MEMORY){
                    setSI(InterruptType.OUT_OF_MEMORY);
                    //System.out.println("No memory");
                    OsLogger.writeToLog("No memory for vm, named " + programName);
                    removeVm(vm);// TODO: This needs to be processed in interrupt
                }
            }
        } catch (IOException e) {
            removeVm(vm);
            System.out.println("IO Exception");
        } catch (Exception e) {
            setPI(InterruptType.UNDEFINED_OPERATION_WHILE_LOADING);
            removeVm(vm);// TODO: This needs to be processed in interrupt
            OsLogger.writeToLog("Removed vm named " + programName + ", because of incorrect code");
            return;
        }
        OsLogger.writeToLog("Load for program " + programName + " ended");
        byte[][] vmListTable = memory.memory[memory.vmList];
        byte[][] vmDescriptor = memory.memory[Test.bytesToInt(vmListTable[vm.ptr.getDataInt()])];
        vmDescriptor[Constants.VM_NAME_INDEX] = programName.getBytes();
        vmDescriptor[Constants.VM_CS_INDEX] = vm.cs.data;
        vmDescriptor[Constants.VM_DS_INDEX] = vm.ds.data;
        vmDescriptor[Constants.VM_PTR_INDEX] = vm.ptr.data;
    }

    private long findFilePos(String programName) {
        byte[] word = new byte[5];
        byte[] pName = programName.getBytes();
        int counter = 0;
        long blockPosition = 0;
        try {
            hdd.file.seek(0);
            while (true) {
                hdd.file.read(word, 0, 5);
                if (pName[0] == word[0] && pName[1] == word[1]) {
                    blockPosition = hdd.getBlockPosition();
                    break;
                }
                counter++;
                if (counter % 16 == 0) {
                    hdd.file.read(word, 0, 1);
                }
                if(counter > 256){
                    return -1;
                }
            }
            hdd.file.seek(1296);
        } catch (Exception ex) {
            ex.printStackTrace();
        }// Found which file it is
        goToFilePosition(counter);
        return blockPosition;
    }

    public int getFilePos(int x, int y, boolean open) {
        byte[] word = new byte[5];
        byte[] pName = new byte[2];
        pName[0] = (byte) x;
        pName[1] = (byte) y;
        int counter = 0;
        try {
            hdd.file.seek(0);
            while (true) {
                hdd.file.read(word, 0, 5);
                if (pName[0] == word[0] && pName[1] == word[1]) {
                    hdd.file.seek(hdd.file.getFilePointer() - 3);
                    hdd.file.writeByte('1');
                    break;
                }
                counter++;
                if (counter % 16 == 0) {
                    hdd.file.read(word, 0, 1);
                }
                if(counter > 256){
                    return -1;
                }
            }
            hdd.file.seek(1296);
        } catch (Exception ex) {
            ex.printStackTrace();
        }// Found which file it is


        return counter;
    }

    public void closeFile(byte[] handler){
        byte[] word = new byte[5];
        int hndl = Test.bytesToInt(handler);
        int counter = 0;
        try {
            hdd.file.seek(0);
            for(int i = 0; i < hndl; i++) {
                hdd.file.read(word, 0, 5);
                counter++;
                if (counter % 16 == 0) {
                    hdd.file.read(word, 0, 1);
                }
            }
            hdd.file.seek(hdd.file.getFilePointer() + 2);
            hdd.file.writeByte('0');
        } catch (Exception ex) {
            ex.printStackTrace();
        }// Found which file it is

    }

    public void fileRead(byte[] dr1, int x, int y, byte[] dr2, Vm vm){
        //DR1 - file handler
        // 10*x + y - vieta, i kuria rasysim duomenu segmente
        //DR2 - adresas is kurio skaitysim
        int dr1Int = ByteBuffer.wrap(dr1).getInt();
        try {
            hdd.file.seek(1296);
        } catch (IOException e) {
            e.printStackTrace();
        }
        goToFilePosition(dr1Int);
        try {
            byte[] temp = new byte[5];
            int readPosition = Test.bytesToInt(dr2);
            for(int i = 0; i < readPosition; i++){
                if((i + 1) % 16 == 0)
                    hdd.file.readByte();
                hdd.file.read(temp, 0, 5);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] word = new byte[4];
        try {
            hdd.file.read(word, 0, 4);
            try {
                vm.saveData(x, y, word);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToFilePosition(int handler){
        int blockNr = 0;
        try {// now go to that file
            hdd.file.seek(1296);
            while (blockNr < handler) {
                String line = hdd.file.readLine();
                if (line.equals("$$$$")) {
                    blockNr++;
                    for (int i = 0; i < 16; i++) {
                        hdd.file.readLine();
                    }
                }
            }
            hdd.file.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fileWrite(byte[] dr1, byte[] data, byte[] dr2){
        goToFilePosition(Test.bytesToInt(dr1));
        try {
            byte[] temp = new byte[5];
            int writePosition = Test.bytesToInt(dr2);
            for(int i = 0; i < writePosition; i++){
                if((i + 1) % 16 == 0)
                    hdd.file.readByte();
                hdd.file.read(temp, 0, 5);
            }
            hdd.file.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(byte[] dr1){
        int hndl = Test.bytesToInt(dr1);
        byte[] word = new byte[5];
        int counter = 0;
        try {
            hdd.file.seek(0);

            for(int i = 0; i < hndl; i++) {
                hdd.file.read(word, 0, 5);
                counter++;
                if (counter % 16 == 0) {
                    hdd.file.read(word, 0, 1);
                }
            }
            hdd.file.write("0000".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        goToFilePosition(Test.bytesToInt(dr1));
        byte[] newLine = "0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000\r\n".getBytes();
        for(int i = 0; i < 16; i++){
            try {
                hdd.file.write(newLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getVmPtr() {
        byte[][] rmTable = memory.memory[Test.bytesToInt(Rm.ptr.data)];
        int vmPtr = -1;
        for (int i = 0; i < 16; i++) {
            if (Test.bytesToInt(rmTable[i]) == 0) {
                vmPtr = i;
                break;
            }
        }
        return vmPtr;
    }

    public void showBlock(String programName) {
        byte[][] vmListTable = memory.memory[memory.vmList];
        byte[][] vmDescriptor;
        for(int i = 0; i < 16; i++){
            vmDescriptor = memory.memory[Test.bytesToInt(vmListTable[i])];
            if(namesEqual(vmDescriptor[Constants.VM_NAME_INDEX], programName.getBytes()))
            {
                byte[][] rmTable = memory.memory[Test.bytesToInt(Rm.ptr.data)];
                memory.showTrackMemory(ByteBuffer.wrap(rmTable[i]).getInt());
                break;
            }
        }
    }

    public void showCseg(String programName){
        memory.showCodeSegment(this, programName);
    }

    public void showDseg(String programName){
        memory.showDataSegment(this, programName);
    }

    public byte[][] getVmDescriptor(String programName){
        byte[][] vmListTable = memory.memory[memory.vmList];
        byte[][] vmDescriptor;
        for(int i = 0; i < 16; i++){
            vmDescriptor = memory.memory[Test.bytesToInt(vmListTable[i])];
            if(namesEqual(vmDescriptor[Constants.VM_NAME_INDEX], programName.getBytes()))
            {
                return vmDescriptor;
            }
        }
        return null;
    }

    private void removeVm(Vm vm) {
        int rmPtr = Test.bytesToInt(Rm.ptr.data);
        byte[][] rmPtrTable = memory.memory[rmPtr];
        int vmPtr = Test.bytesToInt(vm.ptr.data);
        byte[][] vmPtrTable = memory.memory[Test.bytesToInt(rmPtrTable[vmPtr])];
        rmPtrTable[vmPtr] = Test.intToBytes(0, 4);
        for (int i = 0; i < 16; i++) {
            byte[][] track = memory.memory[Test.bytesToInt(vmPtrTable[i])];
            vmPtrTable[i] = Test.intToBytes(0, 4);
            for (int j = 0; j < 16; j++) {
                track[j] = Test.intToBytes(0, 4);
            }
        }
        byte[][] vmListTable = memory.memory[memory.vmList];
        byte[][] vmDescriptor = memory.memory[Test.bytesToInt(vmListTable[vm.ptr.getDataInt()])];
        for(int i = 0; i < 16; i++){
            vmDescriptor[i] = Test.intToBytes(0, 4);
        }
    }

    public void removeVm(String programName){
        int rmPtr = Test.bytesToInt(Rm.ptr.data);
        byte[][] rmPtrTable = memory.memory[rmPtr];
        byte[][] descriptor = getVmDescriptor(programName);
        if(descriptor == null){
            System.out.println("No such program");
            return;
        }
        int vmPtr = Test.bytesToInt(descriptor[Constants.VM_PTR_INDEX]);
        byte[][] vmPtrTable = memory.memory[Test.bytesToInt(rmPtrTable[vmPtr])];
        rmPtrTable[vmPtr] = Test.intToBytes(0, 4);
        for (int i = 0; i < 16; i++) {
            byte[][] track = memory.memory[Test.bytesToInt(vmPtrTable[i])];
            vmPtrTable[i] = Test.intToBytes(0, 4);
            for (int j = 0; j < 16; j++) {
                track[j] = Test.intToBytes(0, 4);
            }
        }
        for(int i = 0; i < 16; i++){
            descriptor[i] = Test.intToBytes(0, 4);
        }

    }

    private int[] getCodeBeginning(Vm vm){
        int temp = Test.bytesToInt(vm.cs.data);
        int[] ret = new int[2];
        ret[0] = temp / 16;// row
        ret[1] = temp % 16;// column
        return ret;
    }

    private byte[] getCommand(Vm vm, int row, int col){
        byte[] ret;
        byte[][] rmPtrTable = memory.memory[ptr.getDataInt()];
        byte[][] vmPtrTable = memory.memory[Test.bytesToInt(rmPtrTable[vm.ptr.getDataInt()])];
        byte[][] rowInTable = memory.memory[Test.bytesToInt(vmPtrTable[row])];
        ret = rowInTable[col];
        return ret;
    }

    private int[] getNextIndexes(Vm vm){
        int temp = Test.bytesToInt(vm.cs.data) + vm.ic;
        int[] ret = new int[2];
        ret[0] = temp / 16;// row
        ret[1] = temp % 16;// column
        return ret;
    }

    private boolean executeCommand(Vm vm, byte[] command){// returns false if command is HALT
        String cmd = new String(command);
        switch(cmd){
            case "ADRR":
                vm.adrr();
                return true;
            case "SBRR":
                vm.sbrr();
                return true;
            case "MLRR":
                vm.mlrr();
                return true;
            case "DVRR":
                vm.dvrr();
                return true;
            case "MOV1":
                vm.mov1();
                return true;
            case "MOV2":
                vm.mov2();
                return true;
            case "PRNT":
                vm.prnt();
                return true;
            case "PRNS":
                vm.prns();
                return true;
            case "PUSH":
                vm.push();
                return true;
            case "HALT":
                vm.halt();
                return false;
        }
        switch (cmd.substring(0, 3)){
            case "AND":
                vm.and();
                return true;
            case "XOR":
                vm.xor();
                return true;
            case "NOT":
                vm.not();
                return true;
            case "CMP":
                vm.cmp();
                return true;
            case "POP":
                vm.pop();
                return true;
        }
        switch (cmd.substring(0, 2)){
            case "AD":
                vm.ad(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "SB":
                vm.sb(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "ML":
                vm.ml(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "DV":
                vm.dv(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "LW":
                vm.lw(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "SW":
                vm.sw(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "JM":
                vm.jm(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "JE":
                vm.je(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "JA":
                vm.ja(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "JL":
                vm.jl(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "FO":
                vm.fo(cmd.charAt(2), cmd.charAt(3));
                return true;
            case "FR":
                vm.fr(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "FW":
                vm.fw(Test.hexToInt(cmd.toUpperCase().charAt(2)), Test.hexToInt(cmd.toUpperCase().charAt(3)));
                return true;
            case "OR":
                vm.or();
                return true;
            case "FC":
                vm.fc();
                return true;
            case "FD":
                vm.fd();
                return true;
        }
        setSI(InterruptType.UNDEFINED_OPERATION);
        System.out.println("SOMETHING HORRIBLE JUST HAPPENED");
        return false;
    }

    private void showRegister(Vm vm){
        int numOfSpaces = 11;
        byte[][] realTable = memory.memory[ptr.getDataInt()];
        int vmPtr = Test.bytesToInt(realTable[vm.ptr.getDataInt()]);
        System.out.println("REGISTERS: PTR         R1         R2         DS         CS         TI         IC");
        System.out.println(String.format("   %" + numOfSpaces + "s" + "%" + numOfSpaces + "s" +"%" + numOfSpaces + "s"
                        +"%" + numOfSpaces + "s" +"%" + numOfSpaces + "s"  +"%" + numOfSpaces + "s"
                        +"%" + numOfSpaces + "s",
                String.valueOf(vmPtr),
                String.valueOf(vm.r1.getDataInt()),
                String.valueOf(vm.r2.getDataInt()),
                String.valueOf(vm.ds.getDataInt()),
                String.valueOf(vm.cs.getDataInt()),
                timer,
                vm.ic));
    }
    private void test(Vm vm){
        if(si.type != InterruptType.NO_INTERRUPT){
            System.out.println("System interrupt");
        }
        if(pi.type != InterruptType.NO_INTERRUPT){
            System.out.println("Program interrupt");
        }
        if(timer == 0){
            System.out.println("Timer interupt");
            timer = 10;
        }
        if(si.type == InterruptType.DUPLICATE_NAME){
            System.out.println("Interrupt: Duplicate name");
            OsLogger.writeToLog("Interrupt: Duplicate name");
        }
        if(si.type == InterruptType.INCORRECT_FILE_NAME){
            System.out.println("Interrupt: Incorrect file name");
            OsLogger.writeToLog("Interrupt: Incorrect file name");
        }
        if(si.type == InterruptType.OUT_OF_MEMORY){
            System.out.println("Interrupt: Out of memory");
            OsLogger.writeToLog("Interrupt: Out of memory");
            // TODO: remove vm
        }
        if(si.type == InterruptType.UNDEFINED_OPERATION_WHILE_LOADING){
            System.out.println("Interrupt: Undefined operation while loading");
            OsLogger.writeToLog("Interrupt: Undefined operation while loading");
            // TODO: remove vm
        }
        if(pi.type == InterruptType.INCORRECT_FILE_HANLDE){
            System.out.println("Interrupt: Incorrect file handle");
            OsLogger.writeToLog("Interrupt: Incorrect file handle");
        }
    }

    public static void setSI(InterruptType interupt){
        si.type = interupt;
    }

    public static void setPI(InterruptType interrupt){
        pi.type = interrupt;
    }

    private boolean namesEqual(byte[] arr1, byte[] arr2){
        return arr1[0] == arr2[0] && arr1[1] == arr2[1];
    }

}
