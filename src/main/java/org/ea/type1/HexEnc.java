package org.ea.type1;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class HexEnc {
    private static final int EEXEC_INITAL_KEY = 55665;
    private static final int CHARSTRING_INITAL_KEY = 4330;
    private static final int DEFAULT_AMOUNT_OF_RANDOM_BYTES = 4;
    private byte[] encryptionBuffer;
    private boolean used = false;
    private final boolean encrypt;
    private int r; // Key used during encryption.
    private final int n; // Number of random bytes in the encryption.
    private final int c1 = 52845; // Constant specified in the documentation
    private final int c2 = 22719; // Constant specified in the documentation
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public HexEnc(boolean charStringEncryption, boolean encrypt) {
        this(charStringEncryption, encrypt, DEFAULT_AMOUNT_OF_RANDOM_BYTES);
    }

    public HexEnc(boolean charStringEncryption, boolean encrypt, int randomBytes) {
        r = charStringEncryption ? CHARSTRING_INITAL_KEY : EEXEC_INITAL_KEY;
        n = randomBytes;
        this.encrypt = encrypt;
    }

    public void setData(String data) {
        byte[] dataBytes;
        if (encrypt) {
            dataBytes = data.getBytes(StandardCharsets.ISO_8859_1);
        } else {
            dataBytes = hexToBytes(data);
        }
        this.setData(dataBytes);
    }

    public void setData(byte[] dataBytes) {
        if (encrypt) {
            Random rand = new Random();
            encryptionBuffer = new byte[n + dataBytes.length];
            rand.nextBytes(encryptionBuffer);
            System.arraycopy(dataBytes, 0, encryptionBuffer, n, dataBytes.length);
        } else {
            encryptionBuffer = dataBytes;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String getResult(boolean binary) throws Exception {
        if (used) throw new Exception("This object is already used for encrypting so the state is incorrect");

        for (int i = 0; i < encryptionBuffer.length; i++) {
            int c = encryptionBuffer[i] & 0xFF;
            encryptionBuffer[i] = (byte) (c ^ (r >> 8));
            if (encrypt) c = encryptionBuffer[i] & 0xFF;
            r = ((c + r) * c1 + c2) % 65536;
        }

        used = true;

        if (encrypt) {
            if (binary) {
                return new String(encryptionBuffer, StandardCharsets.ISO_8859_1);
            } else {
                return bytesToHex(encryptionBuffer);
            }
        } else {
            byte[] returnArray = Arrays.copyOfRange(encryptionBuffer, n, encryptionBuffer.length);
            if (binary) {
                return new String(returnArray, StandardCharsets.ISO_8859_1);
            } else {
                return bytesToHex(returnArray);
            }
        }
    }
}
