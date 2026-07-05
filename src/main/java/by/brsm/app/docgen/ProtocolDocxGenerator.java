package by.brsm.app.docgen;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.model.RosterEntry;
import by.brsm.app.service.NameSortService;
import by.brsm.app.service.SpeakerResolver;
import by.brsm.app.service.TextBuilderService;
import by.brsm.app.util.AppPaths;
import by.brsm.app.util.DateUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Генерация файла «Протокол заседания комитета» в формате .docx.
 */
public class ProtocolDocxGenerator {

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
        for (AgendaItem item : protocol.getItems()) {
            DocxStyleHelper.addParagraph(doc, textBuilderService.buildAgendaHeader(item));
            DocxStyleHelper.addParagraph(doc,
                    "Докладчик – секретарь ПО ОО «БРСМ» с правами РК БНТУ " + agendaSpeaker.getShortName() + ".");
        }
    }

    private void writeVotingSummary(XWPFDocument doc, int votes) {
        DocxStyleHelper.addBlankLine(doc);
        writeVotingBlock(doc, votes);
    }

    private void writeAgendaDiscussions(XWPFDocument doc, Protocol protocol) {
        int number = 1;
        for (AgendaItem item : protocol.getItems()) {
            Person speaker = speakerResolver.resolveSlushaliSpeaker(item, protocol);
            item.setSpeaker(speaker);
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, number + ". СЛУШАЛИ:", true);
            DocxStyleHelper.addParagraph(doc, textBuilderService.buildSlushaliText(item, speaker));
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, "ВЫСТУПИЛИ:", true);
            DocxStyleHelper.addParagraph(doc,
                    textBuilderService.buildVystupiliText(item, speaker, item.getSupporter()));
            DocxStyleHelper.addBlankLine(doc);
            DocxStyleHelper.addParagraph(doc, "ПОСТАНОВИЛИ:", true);
            for (String line : textBuilderService.buildProtocolResolutionItems(item, protocol)) {
                DocxStyleHelper.addParagraph(doc, line);
            }
            writeVotingBlock(doc, item.getVotesFor());
            number++;
        }
    }

    private void writeVotingBlock(XWPFDocument doc, int votes) {
        DocxStyleHelper.addParagraph(doc, "Голосовали:", true);
        DocxStyleHelper.addParagraph(doc, "«за»");
        DocxStyleHelper.addParagraph(doc, votes + " человек;");
        DocxStyleHelper.addParagraph(doc, "«против»");
        DocxStyleHelper.addParagraph(doc, "нет;");
        DocxStyleHelper.addParagraph(doc, "«воздержались»");
        DocxStyleHelper.addParagraph(doc, "нет.");
    }

    private void writeSignatures(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "Председатель");
        DocxStyleHelper.addParagraph(doc, "\t\t\t" + protocol.getChairman().getShortName());
        DocxStyleHelper.addParagraph(doc, "Секретарь");
        DocxStyleHelper.addParagraph(doc, "\t\t\t" + protocol.getMeetingSecretary().getShortName());
    }
}
