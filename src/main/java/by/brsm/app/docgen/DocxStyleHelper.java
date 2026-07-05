package by.brsm.app.docgen;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Общие стили абзацев и runs для документов БРСМ.
 */
public final class DocxStyleHelper {

    public static final String FONT = "Times New Roman";
    public static final int FONT_SIZE = 14;

    private DocxStyleHelper() {
    }

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text, boolean bold) {
        return addParagraph(doc, text, ParagraphAlignment.LEFT, bold);
    }

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text, ParagraphAlignment alignment, boolean bold) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(alignment);
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(FONT);
        run.setFontSize(FONT_SIZE);
        run.setBold(bold);
        if (text != null) {
            run.setText(text);
        }
        return paragraph;
    }

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text) {
        return addParagraph(doc, text, ParagraphAlignment.LEFT, false);
    }

    public static XWPFParagraph addCentered(XWPFDocument doc, String text, boolean bold) {
        return addParagraph(doc, text, ParagraphAlignment.CENTER, bold);
    }

    public static void addBlankLine(XWPFDocument doc) {
        doc.createParagraph();
    }

    public static XWPFParagraph addSignatureLine(XWPFDocument doc, String label, String name) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun labelRun = paragraph.createRun();
        labelRun.setFontFamily(FONT);
        labelRun.setFontSize(FONT_SIZE);
        labelRun.setText(label);
        labelRun.addTab();
        labelRun.addTab();
        labelRun.setText(label + "\t\t\t" + name);
        return paragraph;
    }

    public static void addUnderlinedTitle(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = addCentered(doc, text, true);
        for (XWPFRun run : paragraph.getRuns()) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
    }
}
