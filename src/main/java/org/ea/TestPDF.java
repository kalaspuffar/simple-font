package org.ea;

import org.apache.fontbox.cmap.CMap;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.ea.type1.CharString;
import org.ea.type1.FontFile;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class TestPDF {

    public static PDFont createType1(PDDocument newdoc, PDFont font) throws Exception {
        FontFile ff = new FontFile(
            font.getName(),
            font.getFontDescriptor().getFontFamily(),
            font.getFontDescriptor().getFontName()
        );

        ff.setFontMatrix(font.getFontMatrix());
        ff.setFontBBox(font.getFontDescriptor().getFontBoundingBox());

        ff.setBlueValues((int) font.getFontDescriptor().getCapHeight(), (int) font.getFontDescriptor().getXHeight());
        //ff.setAscender((int) font.getFontDescriptor().getAscent());
        //ff.setDescender((int) font.getFontDescriptor().getDescent());

        Encoding encoding = Encoding.getInstance(COSName.STANDARD_ENCODING);

        for (int i = 0; i < 256; i++) {
            PathIterator pIt = null;
            CharString cs = null;
            if (font instanceof PDType1CFont) {
                PDType1CFont font1c = (PDType1CFont) font;
                encoding = font1c.getEncoding();
                pIt = font1c.getPath(font1c.codeToName(i)).getPathIterator(null);
                cs = new CharString(font1c.codeToName(i), i);
            }
            if (font instanceof PDTrueTypeFont) {
                PDTrueTypeFont fonttrue = (PDTrueTypeFont) font;
                encoding = fonttrue.getEncoding();
                pIt = fonttrue.getPath(i).getPathIterator(null);
                cs = new CharString(encoding.getName(i), i);
            }
            if (font instanceof PDType0Font) {
                PDType0Font font0 = (PDType0Font) font;
                pIt = font0.getPath(i).getPathIterator(null);
                String name = font0.toUnicode(i);
                if (name == null) {
                    name = encoding.getName(font0.getDescendantFont().codeToCID(i));
                }
                cs = new CharString(
                    name,
                    font0.getDescendantFont().codeToGID(i)
                );
            }
            if (pIt != null && !pIt.isDone()) {

                int lastMoveX = 0;
                int lastMoveY = 0;

                int d = 0;

                float[] buffer = new float[6];
                for (; !pIt.isDone(); pIt.next()) {
                    switch (pIt.currentSegment(buffer)) {
                        case PathIterator.SEG_CUBICTO:
                            cs.curveTo(
                                    (int)buffer[0], (int)buffer[1],
                                    (int)buffer[2], (int)buffer[3],
                                    (int)buffer[4], (int)buffer[5]
                            );
                            break;
                        case PathIterator.SEG_QUADTO:
                            cs.quadTo(
                                    (int)buffer[0], (int)buffer[1],
                                    (int)buffer[2], (int)buffer[3]
                            );
                            break;
                        case PathIterator.SEG_MOVETO:
                            lastMoveX = (int)buffer[0];
                            lastMoveY = (int)buffer[1];
                            cs.moveTo((int)buffer[0], (int)buffer[1]);
                            break;
                        case PathIterator.SEG_LINETO:
                            cs.lineTo((int)buffer[0], (int)buffer[1]);
                            break;
                        case PathIterator.SEG_CLOSE:
                            cs.lineTo(lastMoveX, lastMoveY);
                            break;
                        default:
                    }
                    d++;
                }

                ff.addCharString(cs);
            }
        }

        //System.out.println(ff.getResult(false));

        /*
        File f = new File("../../fontwork/daniel.pfa");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(ff.getResult(false));
        bw.flush();
        bw.close();
        */

        ByteArrayInputStream bais = new ByteArrayInputStream(
            ff.getResult(true).getBytes(StandardCharsets.ISO_8859_1)
        );
        return new PDType1Font(newdoc, bais, encoding);
    }

    public static void main(String[] args) throws Exception {
        PDDocument doc = PDDocument.load(new File("HelloWorld21.pdf"));
        PDPage page = doc.getPage(7);
        PDResources res = page.getResources();

        //PDType1CFont font = (PDType1CFont) res.getFont(COSName.getPDFName("T1_0"));
        //PDType0Font font = (PDType0Font) res.getFont(COSName.getPDFName("C2_0"));
        //PDTrueTypeFont font = (PDTrueTypeFont) res.getFont(COSName.getPDFName("TT0"));


        PDDocument newdoc = new PDDocument();
        PDPage page1 = new PDPage();
        newdoc.addPage(page1);
        page1.setResources(new PDResources());

        for (COSName name : res.getFontNames()) {
            page1.getResources().put(name, createType1(newdoc, res.getFont(name)));
        }

        /*
        COSName name = COSName.getPDFName("T1_0");
        page1.getResources().put(name, createType1(newdoc, res.getFont(name)));
        name = COSName.getPDFName("C2_0");
        page1.getResources().put(name, createType1(newdoc, res.getFont(name)));
        name = COSName.getPDFName("TT0");
        page1.getResources().put(name, createType1(newdoc, res.getFont(name)));
        */
        newdoc.save(new File("../../fontwork/work.pdf"));
    }
}
