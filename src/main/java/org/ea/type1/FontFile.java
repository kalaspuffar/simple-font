package org.ea.type1;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FontFile {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final String DEF_FONT_NAME = "FontName";
    private static final String DEF_PAINT_TYPE = "PaintType";
    private static final String DEF_FONT_TYPE = "FontType";
    private static final String DEF_FONT_MATRIX = "FontMatrix";
    private static final String DEF_FONT_BBOX = "FontBBox";
    private static final String DEF_UNIQUE_ID = "UniqueID";

    private static final String INFO_VERSION = "version";
    private static final String INFO_FULL_NAME = "FullName";
    private static final String INFO_FAMILY_NAME = "FamilyName";
    private static final String INFO_WEIGHT = "Weight";

    private static final String METRICS_CAPHEIGHT = "CapHeight";
    private static final String METRICS_XHEIGHT = "XHeight";
    private static final String METRICS_ASCENDER = "Ascender";
    private static final String METRICS_DESCENDER = "Descender";


    private static int uniqueId = 4_000_001;
    private EExec exec;

    private Map<String, Object> definitions = new HashMap<>();
    private Map<String, Object> fontInfo = new HashMap<>();
    private Map<String, Object> metrics = new HashMap<>();

    public FontFile(String name, String family, String fullName) {
        definitions.put(DEF_FONT_NAME, name);
        definitions.put(DEF_PAINT_TYPE, "0");
        definitions.put(DEF_FONT_TYPE, "1");
        definitions.put(DEF_FONT_MATRIX, "[0.001 0 0 0.001 0 0] readonly");
        definitions.put(DEF_FONT_BBOX, "{0 0 1000 1000} readonly");
        definitions.put(DEF_UNIQUE_ID, Integer.valueOf(uniqueId++));

        fontInfo.put(INFO_VERSION, "001.003");
        if (fullName != null) {
            fontInfo.put(INFO_FULL_NAME, fullName);
        }
        if (family != null) {
            fontInfo.put(INFO_FAMILY_NAME, family);
        }
        fontInfo.put(INFO_WEIGHT, "Medium");

        exec = new EExec(uniqueId++);
    }

    public void setFontMatrix(Matrix fontMatrix) {
        String fontMatrixStr = fontMatrix.toString().replace(",", " ");
        definitions.put(DEF_FONT_MATRIX, fontMatrixStr + " readonly");
    }

    public void setFontBBox(PDRectangle fontBBox) {
        String fontBBoxStr = "{";
        fontBBoxStr += (int) fontBBox.getLowerLeftX() + " ";
        fontBBoxStr += (int) fontBBox.getLowerLeftY() + " ";
        fontBBoxStr += (int) fontBBox.getUpperRightX() + " ";
        fontBBoxStr += (int) fontBBox.getUpperRightY();
        fontBBoxStr += "} readonly";

        definitions.put(DEF_FONT_BBOX, fontBBoxStr);
    }

    public void setWeight(String weight) {
        fontInfo.put(INFO_WEIGHT, weight);
    }

    public void setBlueValues(int capHeight, int xHeight) {
        exec.setBlueValues(capHeight, xHeight);
    }

    public void setAscender(int value) {
        metrics.put(METRICS_ASCENDER, value);
    }

    public void setDescender(int value) {
        metrics.put(METRICS_DESCENDER, value);
    }

    public void addCharString(CharString cs) {
        exec.addCharString(cs);
    }

    public String getResult(boolean binary) throws Exception {
        String fontstr = "";
        fontstr += "%!FontType1-1.0: ";
        fontstr += definitions.get(DEF_FONT_NAME) + " " + fontInfo.get(INFO_VERSION) + "\n";
        fontstr += "%%CreationDate: " + sdf.format(new Date()) + " \n";
        fontstr += "%%Creator: Simple Font Library\n";
        //fontstr += "%%VMusage: 27647 34029\n";
        fontstr += (definitions.size() + 3) + " dict begin\n";

        for (Map.Entry<String, Object> defEntry : definitions.entrySet()) {
            fontstr += "/" + defEntry.getKey() + " ";
            if (defEntry.getKey().equals(DEF_FONT_NAME)) {
                fontstr += "/" + defEntry.getValue().toString() + " def\n";
            } else {
                fontstr += defEntry.getValue().toString() + " def\n";
            }
        }

        fontstr += "/FontInfo " + fontInfo.size() + " dict begin\n";
        for (Map.Entry<String, Object> defEntry : fontInfo.entrySet()) {
            fontstr += "/" + defEntry.getKey() + " (";
            fontstr += defEntry.getValue().toString() + ") readonly def\n";
        }
        fontstr += "end readonly def\n";

        fontstr += "/Metrics " + metrics.size() + " dict begin\n";
        for (Map.Entry<String, Object> defEntry : metrics.entrySet()) {
            fontstr += "/" + defEntry.getKey() + " (";
            fontstr += defEntry.getValue().toString() + ") readonly def\n";
        }
        fontstr += "end readonly def\n";

        fontstr += "/Encoding 256 array\n";
        fontstr += "0 1 255 {1 index exch /.notdef put } for\n";
        for (CharString cs : exec.getCharStrings()) {
            if (cs.notDefined()) continue;
            fontstr += "dup " + cs.getIndex() + " /" + cs.getName() + " put\n";
        }
        fontstr += "readonly def\n";

        fontstr += "currentdict end\n";
        fontstr += "currentfile eexec\n";

        String execResult = exec.getResult(binary);;

        String clearMark = "\ncleartomark\n";

        if (binary) {
            int len = fontstr.getBytes(StandardCharsets.ISO_8859_1).length;
            int execlen = execResult.getBytes(StandardCharsets.ISO_8859_1).length;
            int clearlen = clearMark.getBytes(StandardCharsets.ISO_8859_1).length;

            ByteBuffer bb = ByteBuffer.allocate(len + execlen + clearlen + 22);

            bb.put((byte) 128);
            bb.put((byte) 1);
            bb.put((byte) (len & 0xFF));
            bb.put((byte) ((len >> 8) & 0xFF));
            bb.put((byte) ((len >> 16) & 0xFF));
            bb.put((byte) ((len >> 24) & 0xFF));
            bb.put(fontstr.getBytes(StandardCharsets.ISO_8859_1));

            bb.put((byte) 128);
            bb.put((byte) 2);
            bb.put((byte) (execlen & 0xFF));
            bb.put((byte) ((execlen >> 8) & 0xFF));
            bb.put((byte) ((execlen >> 16) & 0xFF));
            bb.put((byte) ((execlen >> 24) & 0xFF));
            bb.put(execResult.getBytes(StandardCharsets.ISO_8859_1));

            bb.put((byte) 128);
            bb.put((byte) 1);
            bb.put((byte) (clearlen & 0xFF));
            bb.put((byte) ((clearlen >> 8) & 0xFF));
            bb.put((byte) ((clearlen >> 16) & 0xFF));
            bb.put((byte) ((clearlen >> 24) & 0xFF));
            bb.put(clearMark.getBytes(StandardCharsets.ISO_8859_1));

            bb.put(new byte[] {0,0,0,0});

            return new String(bb.array(), StandardCharsets.ISO_8859_1);
        } else {
            return fontstr + execResult + clearMark;
        }
    }
}