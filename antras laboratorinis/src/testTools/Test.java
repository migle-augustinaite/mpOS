package testTools;

import Rm.Rm;

import java.nio.ByteBuffer;

/**
 * Created by blitZ on 3/8/2017.
 */
public class Test {
    Rm rm;

    public Test(Rm rm) {
        this.rm = rm;
    }

    public void testSf() {
        rm.sf.setCf(1);
        rm.sf.print();
        rm.sf.setOf(1);
        rm.sf.setCf(0);
        rm.sf.print();
        rm.sf.setZf(1);
        rm.sf.setCf(1);
        rm.sf.print();
        rm.sf.setZf(0);
        rm.sf.setCf(0);
        rm.sf.setOf(0);

    }

    public void testLoad(String programName) {
        //rm.load(fileName, programName);
        try {
            rm.load(programName);
            rm.showBlock(programName);
            byte[][] descriptor = rm.getVmDescriptor(programName);
            System.out.println("Vm name: " + new String(descriptor[Constants.VM_NAME_INDEX]));
            System.out.println("Vm data segment: " + bytesToInt(descriptor[Constants.VM_DS_INDEX]));
            System.out.println("Vm code segment: " + bytesToInt(descriptor[Constants.VM_CS_INDEX]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Takes block as an argument and prints it's content as if it's code
     */
    public static void printCode(byte[][] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            System.out.print(i + " CODE ");
            for (int j = 0; j < buffer[i].length; j++) {
                System.out.print((char) buffer[i][j]);
            }
            System.out.println();
        }
    }

    public static void printData(byte[][] buffer) {
        for (int i = 0; i < (buffer.length) / 2; i++) {
            System.out.print("DATA ");
            System.out.print((char) buffer[i * 2][0]);
            System.out.print((char) buffer[i * 2][1]);
            System.out.print(" " + bytesToInt(buffer[i * 2 + 1]));
            System.out.println();
        }
    }

    public static int hexToInt(char c){// c should be upper case letter
        if(c >= 48 && c <= 57){
            return c - 48;
        } else
            return c - 'A' + 10;
    }

    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] intToBytes(int integer, int size) {
        return ByteBuffer.allocate(size).putInt(integer).array();
    }
}
