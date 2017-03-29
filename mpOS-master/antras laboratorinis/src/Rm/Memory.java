package Rm;

import jdk.nashorn.internal.runtime.regexp.joni.encoding.IntHolder;
import testTools.Constants;
import testTools.Test;
import vm.Vm;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Created by blitZ on 3/8/2017.
 */
public class Memory {
    public byte[][][] memory;
    HDD hdd;
    boolean prevDW = false;
    boolean prevDT = false;

    public int vmList = 0;

    public Memory(Rm rm) {
        memory = new byte[256][16][4];
        rm.ptr.data = ByteBuffer.allocate(4).putInt(1).array(); //rm.ptr yra registras, jo data
        // laukas nustatomas i 1 bloka
        // Tas laukas rodys i bloka, kuriame yra puslapiu lentele
        vmList = getFreeBlock();
        byte[][] vmListTable = memory[vmList];
        for(int i = 0; i < 16; i++){
            vmListTable[i] = Test.intToBytes(getFreeBlock(), 4);
        }
        hdd = rm.hdd;

    }

    public void addToSupervisor(byte[][] block, boolean isCode, String name) throws IOException {
        memory[0] = block;
        hdd.writeToDisk(name, memory[0], 16);
    }

    private int parse(boolean isCode) {
        if (isCode) {
            for (int i = 0; i < 16; i++) {
                if (memory[0][i][0] == 0)
                    return i;
                if (memory[0][i].length < 3) {
                    byte[] t = memory[0][i];
                    memory[0][i] = new byte[4];
                    for (int j = 0; j < t.length; j++) {
                        memory[0][i][j] = t[j];
                    }
                    for (int j = t.length; j < 4; j++) {
                        memory[0][i][j] = '0';
                    }
                }
            }

        } else {
            for (int i = 0; i < 8; i++) {
                if (memory[0][i * 2][0] == 0)
                    return i * 2;
            }
        }
        return 16;
    }

    public int addToVm(String line, Vm vm, String programName, int lastCommand) throws Exception {
        byte[][] pagesTable = memory[Test.bytesToInt(Rm.ptr.data)];
        String[] words = line.split(" ");
        int ret = 0;
        int i = 0;
        if (prevDW) {
            memory[0][0] = ByteBuffer.allocate(4).putInt(Integer.parseInt(words[0])).array();
            prevDW = false;
            i++;
            vm.ic++;
        } else if (prevDT) {
            i++;
            vm.ic++;
            memory[0][0] = words[0].getBytes();
            prevDT = false;
        } else {
            ret = Constants.CSEG;
        }
        for (i = i; i < words.length; i++) {
            vm.ic++;
            if (words[i].equals("DSEG")) {
                memory[0][i] = "DSEG".getBytes();
                i++;
                vm.ic++;
                ret = Constants.DSEG;
            }
            if (ret == Constants.DSEG || ret == Constants.DW || ret == Constants.DT) {
                if (words[i].equals("DW00")) {
                    memory[0][i] = "DW00".getBytes();
                    ++i;
                    if (i > 15) {
                        prevDW = true;
                        break;
                    }
                    vm.ic++;
                    memory[0][i] = ByteBuffer.allocate(4).putInt(Integer.parseInt(words[i])).array();// This should work
                    ret = Constants.DW;
                } else if (words[i].equals("DT00")) {
                    memory[0][i] = "DT00".getBytes();
                    ++i;
                    if (i > 15) {
                        prevDT = true;
                        break;
                    }
                    vm.ic++;
                    memory[0][i] = words[i].getBytes();
                    ret = Constants.DT;
                } else if (words[i].equals("CSEG")) {
                    memory[0][i] = "CSEG".getBytes();
                    ret = Constants.CSEG;
                    vm.cs.data = Test.intToBytes(vm.ic, 4);
                } else {
                    memory[0][i] = words[i].getBytes();
                }
            } else {
                if (words[i].equals("HALT")) {
                    memory[0][i] = "HALT".getBytes();
                    if (parseInSupervisor(lastCommand)) {
                        if(addToVm(vm, programName) == Constants.NO_MEMORY)
                            return Constants.NO_MEMORY;
                    } else {
                        throw new Exception("WHAT ARE YOU DOING");
                    }
                    return Constants.HALT;
                }
                memory[0][i] = words[i].getBytes();
            }
        }
        if (parseInSupervisor(lastCommand))
            addToVm(vm, programName);
        else throw new Exception("WTF ARE YOU DOING");

        //showTrackMemory(Test.bytesToInt(pagesTable[Test.bytesToInt(vm.ptr.data)]));
        return ret;
    }

    private int addToVm(Vm vm, String programName) {
        byte[][] pagesTable = memory[Test.bytesToInt(Rm.ptr.data)];
        /*if(vm.ptr.blocksUsed == 0) {// if this is first track in the vm, then we need first to assign block in rm
            for (int i = 0; i < 16; i++) {
                if (Test.bytesToInt(pagesTable[i]) == 0) {
                    pagesTable[i] = Test.intToBytes(getFreeBlock(), 4);
                    vm.ptr.data = pagesTable[i];
                    break;
                }
            }
        }*/
        // vm already has assigned ptr, so just add these blocks where is free

        //copyBlock(memory[0], memory[freeBlock]);
        //memory[Test.bytesToInt(Rm.ptr.data)][vm.ptr.blocksUsed] = Test.intToBytes(freeBlock, 4);
        //memory[Test.bytesToInt(pagesTable[Test.bytesToInt(vm.ptr.data)])][vm.ptr.blocksUsed] = Test.intToBytes(freeBlock, 4);
        int rmPageTablePtr = Test.bytesToInt(Rm.ptr.data);
        int vmPageTablePtr = Test.bytesToInt(vm.ptr.data);
        /*if (vm.ptr.blocksUsed == 0) {
            pagesTable[vmPageTablePtr] = Test.intToBytes(getFreeBlock(), 4);
        }*/
        //int temp = Test.bytesToInt(memory[rmPageTablePtr][Test.bytesToInt(memory[vmPageTablePtr][vm.ptr.blocksUsed])]);
        byte[][] vmPageTable = memory[Test.bytesToInt(memory[rmPageTablePtr][vmPageTablePtr])];
        int freeBlock = getFreeBlock();
        if(freeBlock == Constants.NO_MEMORY)
            return Constants.NO_MEMORY;
        vmPageTable[vm.ptr.blocksUsed] = Test.intToBytes(freeBlock, 4);
        //memory[vmPageTablePtr][vm.ptr.blocksUsed] = Test.intToBytes(freeBlock, 4);
        copyBlock(memory[0], memory[freeBlock]);
        vm.ptr.blocksUsed++;
        return 0;
        //System.out.println(Test.bytesToInt(pagesTable[vm.ptr.blocksUsed - 1]));
    }

    private void copyBlock(byte[][] source, byte[][] dest) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 4; j++) {
                dest[i][j] = source[i][j];
            }
        }
    }

    public int getFreeBlock() {
        int[] usedBlocks = getUsed();
        for (int i = 1; i < 256; i++) {
            if (usedBlocks[i] == 0)
                return i;
        }
        return -1;// TODO need to implement this
    }

    private int[] getUsed() {
        int[] blocks = new int[256];
        for (int i = 0; i < 256; i++) {
            blocks[i] = 0;
        }
        int ptr = Test.bytesToInt(Rm.ptr.data);
        blocks[ptr] = 1;// rm ptr is already used
        blocks[vmList] = 1;// vm list is already use also
        for(int i = 0; i < 16; i++){
            int temp = Test.bytesToInt(memory[vmList][i]);
            blocks[temp] = 1;// also give space for all vm descriptors
        }

        byte[][] rmPtrBlock = memory[ptr];
        byte[][] vmPtrBlock;

        for (int i = 0; i < 16; i++) {
            //vmPtrBlock = memory[Test.bytesToInt(rmPtrBlock[Test.bytesToInt(rmPtrBlock[i])])];
            int vmPtr = Test.bytesToInt(rmPtrBlock[i]);
            blocks[vmPtr] = 1;
            vmPtrBlock = memory[vmPtr];
            if (vmPtr != 0) {
                for (int j = 0; j < 16; j++) {
                    int temp = Test.bytesToInt(vmPtrBlock[j]);
                    try {
                        blocks[temp]++;// TODO change to "blocks[temp] = 1;"
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return blocks;
    }

    private boolean parseInSupervisor(int lastCommand) throws Exception {
        int i = 0;
        IntHolder temp = new IntHolder();
        temp.value = 0;
        if (lastCommand == Constants.DW) {// last was DW
            temp.value = Constants.DW;
            int value = ByteBuffer.wrap(memory[0][i]).getInt();
            if (value < 0) {
                System.out.println("VALUE TOO HIGH!!!!!!");
                throw new Exception("Value too high");
            }
            i++;
        } else if (lastCommand == Constants.DT) {// last was DT
            temp.value = Constants.DT;
            i++;
        } else if (lastCommand == Constants.CSEG) {
            temp.value = Constants.CSEG;
        }
        for (i = i; i < 16; i++) {
            if (ByteBuffer.wrap(memory[0][i]).getInt() != 0) {
                if (!isValidCommad(memory[0][i], temp)) {
                    return false;
                }
                if (temp.value == Constants.DW || temp.value == Constants.DT) {
                    i++;
                }
                if (temp.value == Constants.HALT) {
                    break;
                }
            }
        }
        return true;
        /*for(int i = 0; i < 16; i++){
            for(int j = 0; j < 4; j++){
                System.out.print((char)memory[0][i][j]);
            }
            System.out.print(" ");
        }*/

    }

    boolean isValidCommad(byte[] command, IntHolder temp) {
        String cmd = new String(command);
        if (temp.value == Constants.CSEG) {
            switch (cmd.substring(0, 2)) {
                case "AD":
                    if (cmd.equals("ADRR")) {
                        return true;
                    } else if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "SB":
                    if (cmd.equals("SBRR")) {
                        return true;
                    } else if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "ML":
                    if (cmd.equals("MLRR")) {
                        return true;
                    } else if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "DV":
                    if (cmd.equals("DVRR")) {
                        return true;
                    } else if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "LW":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "SW":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "JM":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "JE":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "JA":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "JL":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "FO":
                    return true;
                case "FR":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
                case "FW":
                    if (isHex(cmd.toUpperCase().charAt(2)) && isHex(cmd.toUpperCase().charAt(3))) {
                        return true;
                    }
                    break;
            }

            switch (cmd) {
                case "HALT":
                    temp.value = Constants.HALT;
                    return true;
                case "AND":
                    return true;
                case "OR":
                    return true;
                case "XOR":
                    return true;
                case "NOT":
                    return true;
                case "CMP":
                    return true;
                case "MOV1":
                    return true;
                case "MOV2":
                    return true;
                case "PRNT":
                    return true;
                case "PRNS":
                    return true;
                case "PUSH":
                    return true;
                case "POP0":
                    return true;
                case "FC00":
                    return true;
                case "FD00":
                    return true;
            }
        } else {
            switch (cmd) {
                case "CSEG":
                    temp.value = Constants.CSEG;
                    return true;
                case "DSEG":
                    temp.value = Constants.DSEG;
                    return true;
            }
        }
        if (temp.value == Constants.DSEG || temp.value == Constants.DW || temp.value == Constants.DT) {
            switch (cmd) {
                case "DW00":
                    temp.value = Constants.DW;
                    return true;
                case "DT00":
                    temp.value = Constants.DW;
                    return true;
            }
        }

        return false;
    }

    private boolean isHex(char c) {
        return (((int) c >= 48 && (int) c <= 57) || ((int) c >= 65) && ((int) c <= 70));
    }

    public int getInt(int i, int j) {
        return ByteBuffer.wrap(memory[i][j]).getInt();
    }

    public void showDataSegment(Rm rm, String programName){
        byte [][] vmDescriptor = rm.getVmDescriptor(programName);
        if ( vmDescriptor != null ) {
            int ptrIndex = Test.bytesToInt(vmDescriptor[Constants.VM_PTR_INDEX]);
            int dsIndex = Test.bytesToInt(vmDescriptor[Constants.VM_DS_INDEX]);
            int csIndex = Test.bytesToInt(vmDescriptor[Constants.VM_CS_INDEX]);
            byte[][] rmTable = memory[Test.bytesToInt(Rm.ptr.data)];
            byte[][] vmTable = memory[Test.bytesToInt(rmTable[ptrIndex])];
            int lastCommand = 0;
            for (int i = 0; i < 16; i++) { // pereina vm ptr bloku lentele
                int vmBlockNumber = Test.bytesToInt(vmTable[i]);
                if (vmBlockNumber != 0){
                    for (int j = 0; j < 16; j++){
                        if (lastCommand == 1) {
                            ByteBuffer buffer2 = ByteBuffer.wrap(memory[vmBlockNumber][j]);
                            int t2 = buffer2.getInt();
                            System.out.print(t2 + " ");
                            lastCommand = 0;
                            j++;
                        } else if (lastCommand == 2) {
                            String command = new String(memory[vmBlockNumber][j]);
                            System.out.print(command + " ");
                            lastCommand = 0;
                            j++;
                        }
                        if (isEqual(vmBlockNumber, j, "DSEG")) {
                            System.out.print("DSEG ");
                        } else if (isEqual(vmBlockNumber, j, "DW00")) {
                            System.out.print("DW00 ");
                            j++;
                            if (j < 16) {
                                ByteBuffer buffer2 = ByteBuffer.wrap(memory[vmBlockNumber][j]);
                                int t2 = buffer2.getInt();
                                System.out.print(t2 + " ");
                            } else
                                lastCommand = 1;

                        } else if (isEqual(vmBlockNumber, j, "DT00")) {
                            System.out.print("DT00 ");
                            j++;
                            if (j < 16) {
                                String command = new String(memory[vmBlockNumber][j]);
                                System.out.print(command + " ");
                            } else
                                lastCommand = 2;
                        } else if (isEqual(vmBlockNumber, j, "CSEG")) {
                            while (j < 16) {
                                System.out.print("0000" + " ");
                                j++;
                            }
                            break;
                        }
                    }
                    System.out.println();
                }

            }
        }
    }

    public void showCodeSegment(Rm rm, String programName) {
        byte [][] vmDescriptor = rm.getVmDescriptor(programName);
        if ( vmDescriptor != null ) {
            int ptrIndex = Test.bytesToInt(vmDescriptor[Constants.VM_PTR_INDEX]);
            int dsIndex = Test.bytesToInt(vmDescriptor[Constants.VM_DS_INDEX]);
            int csIndex = Test.bytesToInt(vmDescriptor[Constants.VM_CS_INDEX]);
            byte[][] rmTable = memory[Test.bytesToInt(Rm.ptr.data)];
            byte[][] vmTable = memory[Test.bytesToInt(rmTable[ptrIndex])];
            int fullOfDS = csIndex / 16;
            for (int i = fullOfDS; i < 16; i++) { // pereina vm ptr bloku lentele
                int vmBlockNumber = Test.bytesToInt(vmTable[i]);
                if (vmBlockNumber != 0) {
                    if (i == fullOfDS) {
                        for (int j = 0; j < csIndex % 16 -1; j++) {
                            System.out.print("0000 ");
                        }
                        System.out.print("CSEG ");
                        printCSEG(csIndex%16,vmBlockNumber);
                        System.out.println();
                    } else {
                        printCSEG(0, vmBlockNumber);
                        System.out.println();
                    }

                }
                //System.out.println();
            }
        }
    }

    public void printCSEG(int begin, int vmBlockNumber) {
        for (int j = begin; j < 16; j++){
            if (isEqual(vmBlockNumber, j, "HALT")) {
                System.out.print("HALT ");
                j++;
                while (j < 16) {
                    System.out.print("0000 ");
                    j++;
                }
            } else {
                String command = new String(memory[vmBlockNumber][j]);
                System.out.print(command + " ");
            }
        }
    }


    public void showTrackMemory(int blockNumber) {
        int lastCommand = 0;
        for (int i = 0; i < 16; i++) {
            ByteBuffer buffer = ByteBuffer.wrap(memory[blockNumber][i]);
            int t = buffer.getInt();
            if (t != 0) {
                lastCommand = showBlockMemory(t, lastCommand);
            } else {
                for (int j = i; j < 16; j++) {
                    System.out.println("0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000");
                }
                break;
            }

            //System.out.println();
        }

    }

    private int showBlockMemory(int t, int lastCommand) {
        //lastCommand meaning 1 - DW; 2 - DT
        for (int j = 0; j < 16; j++) {
            if (lastCommand == 1) {
                ByteBuffer buffer2 = ByteBuffer.wrap(memory[t][j]);
                int t2 = buffer2.getInt();
                System.out.print(t2 + " ");
                lastCommand = 0;
                j++;
            } else if (lastCommand == 2) {
                String command = new String(memory[t][j]);
                System.out.print(command + " ");
                lastCommand = 0;
                j++;
            }
            if (isEqual(t, j, "DSEG")) {
                System.out.print("DSEG ");
            } else if (isEqual(t, j, "DW00")) {
                System.out.print("DW00 ");
                j++;
                if (j < 16) {
                    ByteBuffer buffer2 = ByteBuffer.wrap(memory[t][j]);
                    int t2 = buffer2.getInt();
                    System.out.print(t2 + " ");
                } else
                    lastCommand = 1;

            } else if (isEqual(t, j, "DT00")) {
                System.out.print("DT00 ");
                j++;
                if (j < 16) {
                    String command = new String(memory[t][j]);
                    System.out.print(command + " ");
                } else
                    lastCommand = 2;
            } else if (isEqual(t, j, "CSEG")) {
                System.out.print("CSEG ");
                /*j++;
                while (j < 16) {
                    String command = new String(memory[t][j]);
                    if (command.equals("HALT")) {
                        System.out.print("HALT ");
                        j++;
                        while (j < 16) {
                            System.out.print("0000" + " ");
                            j++;
                        }
                    } else {
                        System.out.print(command + " ");
                        j++;
                    }
                }*/
            } else if (isEqual(t, j, "HALT")) {
                System.out.print("HALT ");
                j++;
                while (j < 16) {
                    System.out.print("0000 ");
                    j++;
                }
            } else {
                String command = new String(memory[t][j]);
                System.out.print(command + " ");
            }
        }
        System.out.println();
        return lastCommand;
    }

    private boolean isEqual(int t, int j, String command) {
        byte[] array = command.getBytes();
        if (memory[t][j][0] == array[0] & memory[t][j][1] == array[1] & memory[t][j][2] == array[2] &
                memory[t][j][3] == array[3]) {
            return true;
        } else return false;
    }


}
