package org.ea;

import org.ea.type1.CharString;
import org.ea.type1.EExec;
import org.ea.type1.FontFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TestLibrary {
    public static void main(String[] args) throws Exception {
        FontFile ff = new FontFile("SimpleFont", "Simple", "Simple Example Font");

        CharString cs = new CharString("C", 67);
        cs.moveTo(0, 0);
        cs.lineTo(1000, 0);
        cs.lineTo(1000, 1000);
        cs.lineTo(0, 1000);

        ff.addCharString(cs);

        CharString csSpace = new CharString("space", 32);
        ff.addCharString(csSpace);


        File f = new File("../../fontwork/daniel.pfa");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(ff.getResult());
        bw.flush();
        bw.close();
    }
}
