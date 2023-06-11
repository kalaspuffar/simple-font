package org.ea.type1;

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

    private static int uniqueId = 4_000_001;
    private EExec exec;

    private Map<String, Object> definitions = new HashMap<>();
    private Map<String, Object> fontInfo = new HashMap<>();

    public FontFile(String name, String family, String fullName) {
        definitions.put(DEF_FONT_NAME, name);
        definitions.put(DEF_PAINT_TYPE, "0");
        definitions.put(DEF_FONT_TYPE, "1");
        definitions.put(DEF_FONT_MATRIX, "[0.001 0 0 0.001 0 0] readonly");
        definitions.put(DEF_FONT_BBOX, "{0 0 1000 1000} readonly");
        definitions.put(DEF_UNIQUE_ID, Integer.valueOf(uniqueId++));

        fontInfo.put(INFO_VERSION, "001.003");
        fontInfo.put(INFO_FULL_NAME, fullName);
        fontInfo.put(INFO_FAMILY_NAME, family);
        fontInfo.put(INFO_WEIGHT, "Medium");
        exec = new EExec(uniqueId++);
    }

    public void setWeight(String weight) {
        fontInfo.put(INFO_WEIGHT, weight);
    }

    public void addCharString(CharString cs) {
        exec.addCharString(cs);
    }

    public String getResult() throws Exception {
        String fontstr = "";
        fontstr += "%!FontType1-1.0: ";
        fontstr += definitions.get(DEF_FONT_NAME) + " " + fontInfo.get(INFO_VERSION) + "\n";
        fontstr += "%%CreationDate: " + sdf.format(new Date()) + " \n";
        fontstr += "%%Creator: Simple Font Library\n";
        //fontstr += "%%VMusage: 27647 34029\n";
        fontstr += (definitions.size() + 2) + " dict begin\n";

        for (Map.Entry<String, Object> defEntry : definitions.entrySet()) {
            fontstr += "/" + defEntry.getKey() + " ";
            if (defEntry.getKey().equals(DEF_FONT_NAME)) {
                fontstr += "/" + defEntry.getValue().toString() + "\n";
            } else {
                fontstr += defEntry.getValue().toString() + "\n";
            }
        }

        fontstr += "/FontInfo " + fontInfo.size() + " dict begin\n";
        for (Map.Entry<String, Object> defEntry : fontInfo.entrySet()) {
            fontstr += "/" + defEntry.getKey() + " (";
            fontstr += defEntry.getValue().toString() + ") readonly\n";
        }
        fontstr += "end readonly def\n";

        fontstr += "/Encoding 256 array\n";
        fontstr += "0 1 255 {1 index exch /.notdef put } for\n";
        for (CharString cs : exec.getCharStrings()) {
            fontstr += "dup " + cs.getIndex() + " /" + cs.getName() + " put\n";
        }
        fontstr += "readonly def\n";

        fontstr += "currentdict end\n";
        fontstr += "currentfile eexec\n";
        fontstr += exec.getResult();
        fontstr += "\ncleartomark\n";

        return fontstr;
    }
}