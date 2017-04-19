package Rm;

import testTools.Test;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by blitZ on 3/8/2017.
 */
public class HDD implements Closeable {
    public RandomAccessFile file;
    byte[] word;

    public HDD() {
        word = new byte[5];

        try {
            file = new RandomAccessFile("HDD.txt", "rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String readFromMemory() {
        return new String("your mom");
    }

    public void writeToMemory() {

    }

    public void writeToDisk(String name, byte[][] block, int len) throws IOException {
        byte[] bName;
        bName = name.getBytes();
        file.seek(0);// always go to the beginning of file and read from there
        try {
            while (true) {// first we need to find where that file is
                file.read(word, 0, 5);
                if (bName[0] == word[0] && bName[1] == word[1]) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeToBlock(name.getBytes(), block, len, getBlockPosition());
    }

    private void writeToBlock(byte[] name, byte[][] block, int len, long pos) throws IOException {
        file.seek(pos);// got to file beginning
        file.read(word, 0, 4);
        int offset = parseFirstWord();// read first word and parse it
        // TODO: Need to change pdf file, because now first word of file says name of the file and offset that I need to
        // TODO write to
        if (offset == 0) {
            file.seek(pos);
            file.write(name);
        } else
            file.seek(pos + 2);// skip file name
        file.write(Integer.toHexString(offset + len).getBytes());// write new offset for future
        //file.write(" ".getBytes());// and add space
        file.seek(pos + offset * 5 + 5);
        // now just write the content of block
        for (int i = 0; i < len; i++) {
            file.write(block[i]);
            /*if(i == 15)
                file.writeByte(10);
            else*/
            file.writeByte(32);
        }
    }

    public long getBlockPosition() throws IOException {
        long pos = file.getFilePointer();
        return (pos / 5) * 1302;
    }

    private int parseFirstWord() {
        byte[] temp = new byte[2];
        temp[0] = getByteFromChar((char) word[2]);
        temp[1] = getByteFromChar((char) word[3]);
        // System.out.println(temp[0] * temp[1]);
        return temp[0] * 16 + temp[1];
    }

    private byte getByteFromChar(char c) {
        if (Character.isDigit(c))
            return (byte) (c - 48);
        else return (byte) (c - 65 + 10);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
