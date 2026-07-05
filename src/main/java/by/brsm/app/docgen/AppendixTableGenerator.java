package by.brsm.app.docgen;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.Protocol;
import by.brsm.app.model.RosterEntry;
import by.brsm.app.util.DateUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.List;
import java.util.Map;

/**
 * Генерация таблицы-приложения для типа «Формирование студенческого штаба».
 */
public class AppendixTableGenerator {

    @SuppressWarnings("unchecked")
    public void append(XWPFDocument doc, Protocol protocol, AgendaItem item, int resolutionNumber) {
        Map<String, Object> fields = item.getFields();
        List<RosterEntry> roster = parseRoster(fields.get("roster"));

        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "Приложение №1");
        DocxStyleHelper.addParagraph(doc,
                "к Постановлению комитета ПО ОО «БРСМ» с правами РК БНТУ от "
                        + DateUtils.formatDocument(protocol.getDate()) + " г. №" + resolutionNumber);
        DocxStyleHelper.addParagraph(doc, "Состав");
        DocxStyleHelper.addParagraph(doc,
                fields.get("staffFullName")
                        + " первичной организации Общественного объединения «Белорусский республиканский союз молодежи» Белорусского национального технического университета");

        XWPFTable table = doc.createTable(roster.size() + 1, 3);
        setCellText(table.getRow(0).getCell(0), "№ п/п", true);
        setCellText(table.getRow(0).getCell(1), "Ф.И.О.", true);
        setCellText(table.getRow(0).getCell(2), "Место работы/учебы, должность", true);

        for (int i = 0; i < roster.size(); i++) {
            RosterEntry entry = roster.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            setCellText(row.getCell(0), String.valueOf(i + 1), false);
            setCellText(row.getCell(1), entry.getFullName(), false);
            setCellText(row.getCell(2), entry.getWorkplace(), false);
        }
    }

    @SuppressWarnings("unchecked")
    private List<RosterEntry> parseRoster(Object raw) {
        if (raw instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof RosterEntry) {
            return (List<RosterEntry>) list;
        }
        return List.of();
    }

    private void setCellText(XWPFTableCell cell, String text, boolean bold) {
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(DocxStyleHelper.FONT);
        run.setFontSize(DocxStyleHelper.FONT_SIZE);
        run.setBold(bold);
        run.setText(text != null ? text : "");
    }
}
