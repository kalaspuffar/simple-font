package org.ea.type1;

import java.util.*;

public class EExec {

    private Map<String, Object> definitions = new HashMap<>();
    private List<CharString> charStrings = new ArrayList<>();

    public EExec(int uniqueId) {
        definitions.put("RD", "{string currentfile exch readstring pop} executeonly");
        definitions.put("ND", "{noaccess def} executeonly");
        definitions.put("NP", "{noaccess put} executeonly");
        definitions.put("BlueValues", "[]");
        definitions.put("MinFeature", "{16 16}");
        definitions.put("password", "5839");
        definitions.put("UniqueID", Integer.valueOf(uniqueId));
        charStrings.add(new CharString(CharString.NOT_DEFINED, -1));
    }

    public void setBlueValues(int capHeight, int xHeight) {
        String blueValues = "[-15 0 ";
        blueValues += capHeight + " " + (capHeight + 10) + " ";
        blueValues += xHeight + " " + (xHeight + 10) + "]";
        definitions.put("BlueValues", blueValues);
    }

    public void addCharString(CharString cs) {
        charStrings.add(cs);
    }

    public List<CharString> getCharStrings() {
        Collections.sort(charStrings, Comparator.comparingInt(CharString::getIndex));
        return charStrings;
    }

    public String getResult(boolean binary) throws Exception {
        String encoded = "";
        encoded += "dup /Private ";
        encoded += definitions.size() + 1;
        encoded += " dict dup begin\n";

        for(Map.Entry<String, Object> entry : definitions.entrySet()) {
            encoded += "/" + entry.getKey();
            encoded += " " + entry.getValue().toString();
            encoded += " def\n";
        }

        encoded += "2 index /CharStrings ";
        encoded += charStrings.size();
        encoded += " dict dup begin\n";

        for(CharString cs : charStrings) {
            encoded += "/" + cs.getName() + " ";
            String charStringOut = cs.getResult();
            encoded += charStringOut.length();
            encoded += " RD " + charStringOut;
            encoded += " ND\n";
        }

        encoded += "end\n";
        encoded += "end\n";
        encoded += "readonly put\n";
        encoded += "noaccess put\n";
        encoded += "dup /FontName get exch definefont pop\n";
        encoded += "mark currentfile closefile\n";

        HexEnc hexEnc = new HexEnc(false, true);
        hexEnc.setData(encoded);
        return hexEnc.getResult(binary);
    }
}
