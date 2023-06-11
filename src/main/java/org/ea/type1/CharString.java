package org.ea.type1;

import java.awt.geom.GeneralPath;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CharString {

    private static final byte HSTEM = 1;
    private static final byte VSTEM = 3;
    private static final byte VMOVETO = 4;
    private static final byte RLINETO = 5;
    private static final byte HLINETO = 6;
    private static final byte VLINETO = 7;
    private static final byte RRCURVETO = 8;
    private static final byte CLOSEPATH = 9;
    private static final byte CALLSUBR = 10;
    private static final byte RETURN = 11;
    private static final byte EXTENDED = 12;
    private static final byte HSBW = 13;
    private static final byte ENDCHAR = 14;
    private static final byte RMOVETO = 21;
    private static final byte HMOVETO = 22;
    private static final byte VHCURVETO = 30;
    private static final byte HVCURVETO = 31;
    private ByteBuffer writeBuffer = ByteBuffer.allocate(200);

    private int currentX;
    private int currentY;

    private String name;
    private int index;

    public CharString(String name, int index) {
        writeBuffer.put(intToBytes(0));
        writeBuffer.put(intToBytes(0));
        writeBuffer.put(HSBW);
        this.currentX = 0;
        this.currentY = 0;
        this.name = name;
        this.index = index;
    }

    private byte[] intToBytes(int val) {
        if (val <= 107 && val >= -107) {
            return new byte[] {(byte) (val + 139)};
        }
        if (val <= 1131 && val >= 108) {
            val -= 108;
            int rem = val % 256;
            int div = (val - rem) / 256;
            return new byte[] {(byte) (div + 247), (byte) (rem)};
        }
        if (val <= -108 && val >= -1131) {
            val = Math.abs(val);
            val -= 108;
            int rem = val % 256;
            int div = (val + rem) / 256;
            return new byte[] {(byte) (div + 251), (byte) (rem)};
        }

        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put((byte) 255);
        bb.put((byte) 255);
        bb.putInt(val);
        return bb.array();
    }

    public void moveTo(int x, int y) {
        writeBuffer.put(intToBytes(x - currentX));
        writeBuffer.put(intToBytes(y - currentY));
        writeBuffer.put(RMOVETO);
        currentX = x;
        currentY = y;
    }

    public void lineTo(int x, int y) {
        writeBuffer.put(intToBytes(x - currentX));
        writeBuffer.put(intToBytes(y - currentY));
        writeBuffer.put(RLINETO);
        currentX = x;
        currentY = y;
    }

    public void curveTo(int x1, int y1, int x2, int y2, int x3, int y3) {
        writeBuffer.put(intToBytes(x1 - currentX));
        writeBuffer.put(intToBytes(y1 - currentY));
        writeBuffer.put(intToBytes(x2 - currentX));
        writeBuffer.put(intToBytes(y2 - currentY));
        writeBuffer.put(intToBytes(x3 - currentX));
        writeBuffer.put(intToBytes(y3 - currentY));
        writeBuffer.put(RRCURVETO);
        this.currentX = x3;
        this.currentY = y3;
    }

    public void quadTo(int x1, int y1, int x2, int y2) throws Exception {
        throw new Exception("Don't know what to do here.");
    }

    public String getResult() throws Exception {
        writeBuffer.put(CLOSEPATH);
        writeBuffer.put(ENDCHAR);
        HexEnc charEnc = new HexEnc(true, true);
        charEnc.setData(Arrays.copyOfRange(writeBuffer.array(), 0, writeBuffer.position()));
        return charEnc.getResult(true);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
