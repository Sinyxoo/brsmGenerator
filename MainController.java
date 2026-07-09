package by.brsm.app.docgen;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.service.NameSortService;
import by.brsm.app.service.SpeakerResolver;
import by.brsm.app.service.TextBuilderService;
import by.brsm.app.util.AppPaths;
import by.brsm.app.util.DateUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Генерация файла «Протокол заседания комитета» в формате .docx.
 *
 * <p>Форматирование (поля страницы, интервалы, таблица блока «Голосовали») строго
 * соответствует эталонному шаблону протокола.</p>
 */
public class ProtocolDocxGenerator {

    /** Ширины колонок таблицы «Голосовали» (твипсы) — сняты с эталонного шаблона. */
    private static final int[] VOTING_TABLE_COLUMNS = {1951, 2693, 4927};

    private final TextBuilderService textBuilderService;
    private final SpeakerResolver speakerResolver;
    private final NameSortService nameSortService;

    public ProtocolDocxGenerator(TextBuilderService textBuilderService,
                                 SpeakerResolver speakerResolver,
                                 NameSortService nameSortService) {
        this.textBuilderService = textBuilderService;
        this.speakerResolver = speakerResolver;
        this.nameSortService = nameSortService;
    }

    public Path generate(Protocol protocol) throws IOException {
        Path outputDir = AppPaths.getOutputDir().resolve("protocol_" + protocol.getNumber());
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve("Протокол_" + protocol.getNumber() + ".docx");

        try (XWPFDocument doc = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
            DocxStyleHelper.setPageMargins(doc);
            writeHeader(doc);
            writeProtocolMeta(doc, protocol);
            writeAttendees(doc, protocol);
            writeAgenda(doc, protocol);
            writeVotingSummary(doc, protocol.getDefaultVotesFor());
            writeAgendaDiscussions(doc, protocol);
            writeSignatures(doc, protocol);
            doc.write(out);
        }
        return file;
    }

    private void writeHeader(XWPFDocument doc) {
        DocxStyleHelper.addCentered(doc, "Общественное объединение", false);
        DocxStyleHelper.addCentered(doc, "«Белорусский республиканский союз молодежи»", false);
        DocxStyleHelper.addCentered(doc, "Первичная организация с правами районного комитета", false);
        DocxStyleHelper.addCentered(doc, "Белорусского национального технического университета", false);
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addUnderlinedTitle(doc, "ПРОТОКОЛ");
        DocxStyleHelper.addBlankLine(doc);
    }

    private void writeProtocolMeta(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addCentered(doc,
                DateUtils.formatDocument(protocol.getDate()) + " № " + protocol.getNumber(), false);
        DocxStyleHelper.addCentered(doc, "г. Минск", false);
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addCentered(doc, "заседания Комитета", false);
        DocxStyleHelper.addCentered(doc, "ПО ОО «БРСМ» с правами РК", false);
        DocxStyleHelper.addCentered(doc, "БНТУ", false);
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "Председатель – " + protocol.getChairman().getShortName());
        DocxStyleHelper.addParagraph(doc, "Секретарь – " + protocol.getMeetingSecretary().getShortName());
    }

    private void writeAttendees(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addParagraph(doc,
                "Присутствовали – " + nameSortService.buildAttendeesLine(protocol.getAttendees()) + ".");
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "Повестка дня:", true);
    }

    private void writeAgenda(XWPFDocument doc, Protocol protocol) {
        Person agendaSpeaker = speakerResolver.resolveAgendaDaySpeaker(protocol);
        int number = 1;
        for (AgendaItem item : protocol.getItems()) {
            DocxStyleHelper.addIndentedParagraph(doc, number + ". " + textBuilderService.buildAgendaHeader(item));
            DocxStyleHelper.addIndentedParagraph(doc,
                    "Докладчик – секретарь ПО ОО «БРСМ» с правами РК БНТУ " + agendaSpeaker.getShortName() + ".");
            number++;
        }
    }

    private void writeVotingSummary(XWPFDocument doc, int votes) {
        DocxStyleHelper.addBlankLine(doc);
        writeVotingTable(doc, votes);
    }

    private void writeAgendaDiscussions(XWPFDocument doc, Protocol protocol) {
        int number = 1;
        for (AgendaItem item : protocol.getItems()) {
            Person speaker = speakerResolver.resolveSlushaliSpeaker(item, protocol);
            item.setSpeaker(speaker);
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, number + ". СЛУШАЛИ:", true);
            DocxStyleHelper.addIndentedParagraph(doc, textBuilderService.buildSlushaliText(item, speaker));
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, "ВЫСТУПИЛИ:", true);
            DocxStyleHelper.addIndentedParagraph(doc,
                    textBuilderService.buildVystupiliText(item, speaker, item.getSupporter()));
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, "ПОСТАНОВИЛИ:", true);
            int itemIndex = 1;
            for (String line : textBuilderService.buildProtocolResolutionItems(item, protocol)) {
                DocxStyleHelper.addIndentedParagraph(doc, number + "." + itemIndex + ".\t" + line);
                itemIndex++;
            }
            writeVotingTable(doc, item.getVotesFor());
            number++;
        }
    }

    /** Блок «Голосовали» как безрамочная таблица 3×3 — точно как в эталонном шаблоне протокола. */
    private void writeVotingTable(XWPFDocument doc, int votes) {
        XWPFTable table = DocxStyleHelper.createBorderlessTable(doc, VOTING_TABLE_COLUMNS);
        DocxStyleHelper.setCellText(table.getRow(0).getCell(0), "Голосовали:", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(table.getRow(0).getCell(1), "«за»", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(table.getRow(0).getCell(2), votes + " человек;", DocxStyleHelper.BODY_SIZE, false);

        var row2 = DocxStyleHelper.addRow(table, VOTING_TABLE_COLUMNS);
        DocxStyleHelper.setCellText(row2.getCell(0), "", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(row2.getCell(1), "«против»", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(row2.getCell(2), "нет;", DocxStyleHelper.BODY_SIZE, false);

        var row3 = DocxStyleHelper.addRow(table, VOTING_TABLE_COLUMNS);
        DocxStyleHelper.setCellText(row3.getCell(0), "", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(row3.getCell(1), "«воздержались»", DocxStyleHelper.BODY_SIZE, false);
        DocxStyleHelper.setCellText(row3.getCell(2), "нет.", DocxStyleHelper.BODY_SIZE, false);
    }

    private void writeSignatures(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addSignatureLine(doc, "Председатель", protocol.getChairman().getShortName());
        DocxStyleHelper.addSignatureLine(doc, "Секретарь", protocol.getMeetingSecretary().getShortName());
    }
}
