package by.brsm.app.docgen;

import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

/**
 * Общие стили абзацев, таблиц и изображений для документов БРСМ.
 *
 * <p>Все константы (поля страницы, размеры шрифтов в полупунктах, ширины колонок)
 * сняты напрямую с эталонных .docx-шаблонов протокола и постановления, чтобы
 * сгенерированные файлы совпадали с ними «один в один».</p>
 */
public final class DocxStyleHelper {

    /** Шрифт документа — по умолчанию тела Word (тема шаблона использует Calibri, без явного rFonts). */
    public static final String FONT = "Calibri";

    /** Основной размер текста протокола/постановления — 15pt (в шаблоне везде sz=30). */
    public static final int BODY_SIZE = 30;

    /** Размер текста двуязычной шапки постановления — 14pt (sz=28). */
    public static final int HEADER_BLOCK_SIZE = 28;

    /** Размер текста блока «ПАСТАНОВА/ПОСТАНОВЛЕНИЕ» — 14.5pt (sz=29). */
    public static final int META_BLOCK_SIZE = 29;

    /** Поля страницы (twips), см. w:pgMar в шаблонах: верх/право/низ — 1.5 см, лево — 3 см. */
    private static final int MARGIN_TOP_BOTTOM_RIGHT = 851;
    private static final int MARGIN_LEFT = 1701;
    private static final int MARGIN_HEADER_FOOTER = 709;
    private static final int PAGE_WIDTH = 11906;
    private static final int PAGE_HEIGHT = 16838;

    private DocxStyleHelper() {
    }

    // ------------------------------------------------------------------ Страница

    /** Выставляет поля страницы и формат A4 строго как в эталонных шаблонах. */
    public static void setPageMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr()
                : doc.getDocument().getBody().addNewSectPr();

        CTPageSz pageSz = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        pageSz.setW(BigInteger.valueOf(PAGE_WIDTH));
        pageSz.setH(BigInteger.valueOf(PAGE_HEIGHT));

        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(MARGIN_TOP_BOTTOM_RIGHT));
        pageMar.setBottom(BigInteger.valueOf(MARGIN_TOP_BOTTOM_RIGHT));
        pageMar.setRight(BigInteger.valueOf(MARGIN_TOP_BOTTOM_RIGHT));
        pageMar.setLeft(BigInteger.valueOf(MARGIN_LEFT));
        pageMar.setHeader(BigInteger.valueOf(MARGIN_HEADER_FOOTER));
        pageMar.setFooter(BigInteger.valueOf(MARGIN_HEADER_FOOTER));
        pageMar.setGutter(BigInteger.ZERO);
    }

    // ------------------------------------------------------------------ Абзацы и текст

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text, boolean bold) {
        return addParagraph(doc, text, ParagraphAlignment.LEFT, bold);
    }

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text, ParagraphAlignment alignment, boolean bold) {
        return addParagraph(doc, text, alignment, bold, BODY_SIZE);
    }

    public static XWPFParagraph addParagraph(XWPFDocument doc, String text, ParagraphAlignment alignment,
                                              boolean bold, int halfPointSize) {
        XWPFParagraph paragraph = doc.createParagraph();
        applyZeroSpacing(paragraph);
        paragraph.setAlignment(alignment);
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(FONT);
        setExactSize(run, halfPointSize);
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

    public static XWPFParagraph addCentered(XWPFDocument doc, String text, boolean bold, int halfPointSize) {
        return addParagraph(doc, text, ParagraphAlignment.CENTER, bold, halfPointSize);
    }

    /** Абзац с отступом первой строки — для нумерованных пунктов (в шаблонах используется 709 твипс ≈ 1,25 см). */
    public static XWPFParagraph addIndentedParagraph(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = addParagraph(doc, text);
        paragraph.setIndentationFirstLine(709);
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        return paragraph;
    }

    public static void addBlankLine(XWPFDocument doc) {
        XWPFParagraph paragraph = doc.createParagraph();
        applyZeroSpacing(paragraph);
        // Пустая строка должна иметь ту же высоту, что и обычный текст — задаём размер знака абзаца.
        CTPPr pPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTRPr rPr = pPr.isSetRPr() ? pPr.getRPr() : pPr.addNewRPr();
        setExactSize(rPr, BODY_SIZE);
    }

    public static XWPFParagraph addSignatureLine(XWPFDocument doc, String label, String name) {
        XWPFParagraph paragraph = doc.createParagraph();
        applyZeroSpacing(paragraph);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(FONT);
        setExactSize(run, BODY_SIZE);
        run.setText(label);
        run.addTab();
        run.addTab();
        run.addTab();
        run.setText(name);
        return paragraph;
    }

    public static void addUnderlinedTitle(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = addCentered(doc, text, true);
        for (XWPFRun run : paragraph.getRuns()) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
    }

    /** Убирает интервалы до/после абзаца и выставляет одинарный межстрочный интервал (как в шаблонах). */
    public static void applyZeroSpacing(XWPFParagraph paragraph) {
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);
        paragraph.setSpacingBetween(1.0, LineSpacingRule.AUTO);
    }

    /** Точный размер шрифта в полупунктах (позволяет задавать значения вроде 29 = 14.5pt, недоступные через setFontSize(int)). */
    public static void setExactSize(XWPFRun run, int halfPoints) {
        CTRPr rPr = run.getCTR().isSetRPr() ? run.getCTR().getRPr() : run.getCTR().addNewRPr();
        setExactSize(rPr, halfPoints);
    }

    private static void setExactSize(CTRPr rPr, int halfPoints) {
        CTHpsMeasure sz = rPr.isSetSz() ? rPr.getSz() : rPr.addNewSz();
        sz.setVal(BigInteger.valueOf(halfPoints));
        CTHpsMeasure szCs = rPr.isSetSzCs() ? rPr.getSzCs() : rPr.addNewSzCs();
        szCs.setVal(BigInteger.valueOf(halfPoints));
    }

    // ------------------------------------------------------------------ Таблицы без рамок

    /** Создаёт таблицу без видимых границ с заданными ширинами колонок (в твипсах). */
    public static XWPFTable createBorderlessTable(XWPFDocument doc, int[] columnWidthsTwips) {
        XWPFTable table = doc.createTable(1, columnWidthsTwips.length);
        removeBorders(table);
        setColumnWidths(table, columnWidthsTwips);
        // POI создаёт таблицу с одной строкой по умолчанию — очищаем текст в ней под последующее заполнение.
        for (XWPFTableCell cell : table.getRow(0).getTableCells()) {
            formatCellDefaults(cell);
        }
        return table;
    }

    /** Добавляет новую строку с тем же числом колонок, что и первая строка таблицы. */
    public static XWPFTableRow addRow(XWPFTable table, int[] columnWidthsTwips) {
        XWPFTableRow row = table.createRow();
        for (int i = 0; i < columnWidthsTwips.length && i < row.getTableCells().size(); i++) {
            setCellWidth(row.getCell(i), columnWidthsTwips[i]);
            formatCellDefaults(row.getCell(i));
        }
        return row;
    }

    public static void setCellText(XWPFTableCell cell, String text, int halfPointSize, boolean bold) {
        XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        // Убираем возможные лишние абзацы, оставленные POI при создании ячейки.
        while (cell.getParagraphs().size() > 1) {
            cell.removeParagraph(cell.getParagraphs().size() - 1);
        }
        applyZeroSpacing(paragraph);
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
        if (text != null && !text.isEmpty()) {
            XWPFRun run = paragraph.createRun();
            run.setFontFamily(FONT);
            setExactSize(run, halfPointSize);
            run.setBold(bold);
            run.setText(text);
        }
    }

    public static void setCellTextCentered(XWPFTableCell cell, String text, int halfPointSize, boolean bold) {
        setCellText(cell, text, halfPointSize, bold);
        cell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
    }

    /** Заполняет ячейку несколькими абзацами (по одному на строку), выровненными по центру. */
    public static void setCellLines(XWPFTableCell cell, java.util.List<String> lines, int halfPointSize, boolean bold) {
        while (cell.getParagraphs().size() > 1) {
            cell.removeParagraph(cell.getParagraphs().size() - 1);
        }
        boolean first = true;
        for (String line : lines) {
            XWPFParagraph paragraph = first ? cell.getParagraphs().get(0) : cell.addParagraph();
            first = false;
            applyZeroSpacing(paragraph);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            while (!paragraph.getRuns().isEmpty()) {
                paragraph.removeRun(0);
            }
            XWPFRun run = paragraph.createRun();
            run.setFontFamily(FONT);
            setExactSize(run, halfPointSize);
            run.setBold(bold);
            run.setText(line);
        }
    }

    private static void formatCellDefaults(XWPFTableCell cell) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            applyZeroSpacing(paragraph);
        }
    }

    private static void removeBorders(XWPFTable table) {
        table.setTopBorder(XWPFBorderType.NONE, 0, 0, "auto");
        table.setBottomBorder(XWPFBorderType.NONE, 0, 0, "auto");
        table.setLeftBorder(XWPFBorderType.NONE, 0, 0, "auto");
        table.setRightBorder(XWPFBorderType.NONE, 0, 0, "auto");
        table.setInsideHBorder(XWPFBorderType.NONE, 0, 0, "auto");
        table.setInsideVBorder(XWPFBorderType.NONE, 0, 0, "auto");
    }

    private static void setColumnWidths(XWPFTable table, int[] columnWidthsTwips) {
        for (int i = 0; i < columnWidthsTwips.length; i++) {
            setCellWidth(table.getRow(0).getCell(i), columnWidthsTwips[i]);
        }
    }

    private static void setCellWidth(XWPFTableCell cell, int widthTwips) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth tcW = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        tcW.setW(BigInteger.valueOf(widthTwips));
        tcW.setType(STTblWidth.DXA);
    }

    /** Выравнивание таблицы по центру страницы (используется для шапки постановления). */
    public static void centerTable(XWPFTable table) {
        var tblPr = table.getCTTbl().isSetTblPr() ? table.getCTTbl().getTblPr() : table.getCTTbl().addNewTblPr();
        var jc = tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc();
        jc.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STJcTable.CENTER);
    }

    // ------------------------------------------------------------------ Разделитель (двойная линия)

    /** Пустой абзац с верхней двойной линией — разделитель между шапкой и блоком «дата/номер» в постановлении. */
    public static void addDoubleLineSeparator(XWPFDocument doc) {
        XWPFParagraph paragraph = doc.createParagraph();
        applyZeroSpacing(paragraph);
        CTPPr pPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTPBdr pBdr = pPr.addNewPBdr();
        CTBorder top = pBdr.addNewTop();
        top.setVal(STBorder.DOUBLE);
        top.setSz(BigInteger.valueOf(6));
        top.setSpace(BigInteger.valueOf(0));
        top.setColor("auto");
    }

    // ------------------------------------------------------------------ Плавающее изображение (эмблема)

    /**
     * Вставляет изображение как плавающее («за текстом»), с горизонтальным выравниванием
     * по центру колонки и вертикальным смещением относительно абзаца — как эмблема БРСМ
     * в шаблоне постановления (ширина 1.88 см, высота 1.7 см, смещение вниз 0.31 см).
     */
    public static void addFloatingImage(XWPFDocument doc, byte[] imageBytes, int widthEmu, int heightEmu,
                                         int verticalOffsetEmu) throws Exception {
        XWPFParagraph paragraph = doc.createParagraph();
        applyZeroSpacing(paragraph);

        var pictureData = doc.addPictureData(new ByteArrayInputStream(imageBytes), XWPFDocument.PICTURE_TYPE_PNG);
        String relId = doc.getRelationId(pictureData);

        String xml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:r><w:rPr><w:noProof/></w:rPr>"
                + "<w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
                + "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" "
                + "relativeHeight=\"251659264\" behindDoc=\"1\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
                + "<wp:simplePos x=\"0\" y=\"0\"/>"
                + "<wp:positionH relativeFrom=\"column\"><wp:align>center</wp:align></wp:positionH>"
                + "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>" + verticalOffsetEmu + "</wp:posOffset></wp:positionV>"
                + "<wp:extent cx=\"" + widthEmu + "\" cy=\"" + heightEmu + "\"/>"
                + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
                + "<wp:wrapNone/>"
                + "<wp:docPr id=\"1\" name=\"brsmLogo\"/>"
                + "<wp:cNvGraphicFramePr><a:graphicFrameLocks/></wp:cNvGraphicFramePr>"
                + "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
                + "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"brsmLogo.png\"/>"
                + "<pic:cNvPicPr><a:picLocks/></pic:cNvPicPr></pic:nvPicPr>"
                + "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/>"
                + "<a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
                + "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + widthEmu + "\" cy=\"" + heightEmu + "\"/></a:xfrm>"
                + "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>"
                + "</pic:pic></a:graphicData></a:graphic>"
                + "</wp:anchor></w:drawing></w:r></w:p>";

        CTP ctp = CTP.Factory.parse(xml);
        paragraph.getCTP().set(ctp);
    }
}
