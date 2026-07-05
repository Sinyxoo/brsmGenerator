package by.brsm.app.docgen;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.model.RosterEntry;
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
import java.util.Map;

/**
 * Генерация отдельного файла «Постановление комитета».
 */
public class ResolutionDocxGenerator {

    private final TextBuilderService textBuilderService;
    private final SpeakerResolver speakerResolver;
    private final AppendixTableGenerator appendixTableGenerator;

    public ResolutionDocxGenerator(TextBuilderService textBuilderService,
                                   SpeakerResolver speakerResolver,
                                   AppendixTableGenerator appendixTableGenerator) {
        this.textBuilderService = textBuilderService;
        this.speakerResolver = speakerResolver;
        this.appendixTableGenerator = appendixTableGenerator;
    }

    public Path generate(Protocol protocol, AgendaItem item, int resolutionNumber, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve("Постановление_" + resolutionNumber + ".docx");
        Person speaker = speakerResolver.resolveSlushaliSpeaker(item, protocol);

        try (XWPFDocument doc = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
            writeBilingualHeader(doc);
            writeResolutionMeta(doc, protocol, resolutionNumber);
            writeTitle(doc, textBuilderService.buildResolutionHeader(item));
            writeIntro(doc, item, protocol, speaker);
            writeResolutionItems(doc, item, protocol);
            writeChairmanSignature(doc, protocol);
            if (item.getType() == AgendaTypeCode.STAFF_FORMATION) {
                appendixTableGenerator.append(doc, protocol, item, resolutionNumber);
            }
            doc.write(out);
        }
        return file;
    }

    private void writeBilingualHeader(XWPFDocument doc) {
        DocxStyleHelper.addCentered(doc, "Грамадскае аб`яднанне", false);
        DocxStyleHelper.addCentered(doc, "«Беларускi рэспублiканскi", false);
        DocxStyleHelper.addCentered(doc, "саюз моладзi»", false);
        DocxStyleHelper.addCentered(doc,
                "Першасная арганізацыя з правамі раённага камітэта Беларускага нацыянальнага тэхнічнага універсытэта",
                false);
        DocxStyleHelper.addCentered(doc, "Общественное объединение", false);
        DocxStyleHelper.addCentered(doc, "«Белорусский республиканский", false);
        DocxStyleHelper.addCentered(doc, "союз молодежи»", false);
        DocxStyleHelper.addCentered(doc,
                "Первичная организация с правами районного комитета Белорусского национального технического университета",
                false);
    }

    private void writeResolutionMeta(XWPFDocument doc, Protocol protocol, int resolutionNumber) {
        DocxStyleHelper.addCentered(doc, "ПАСТАНОВА", true);
        DocxStyleHelper.addCentered(doc, "КАМІТЭТА", true);
        DocxStyleHelper.addCentered(doc, DateUtils.formatDocument(protocol.getDate()), false);
        DocxStyleHelper.addCentered(doc, "г.Мінск", false);
        DocxStyleHelper.addCentered(doc, "ПОСТАНОВЛЕНИЕ", true);
        DocxStyleHelper.addCentered(doc, "КОМИТЕТА", true);
        DocxStyleHelper.addCentered(doc, "№ " + resolutionNumber, false);
        DocxStyleHelper.addCentered(doc, "г.Минск", false);
        DocxStyleHelper.addBlankLine(doc);
    }

    private void writeTitle(XWPFDocument doc, String header) {
        for (String line : header.split("\\R")) {
            DocxStyleHelper.addCentered(doc, line, false);
        }
        DocxStyleHelper.addBlankLine(doc);
    }

    private void writeIntro(XWPFDocument doc, AgendaItem item, Protocol protocol, Person speaker) {
        DocxStyleHelper.addParagraph(doc, textBuilderService.buildResolutionIntro(item, protocol, speaker));
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "ПОСТАНОВЛЯЕТ:", ParagraphAlignment.LEFT, true);
    }

    private void writeResolutionItems(XWPFDocument doc, AgendaItem item, Protocol protocol) {
        List<String> lines = textBuilderService.buildStandaloneResolutionItems(item, protocol);
        int index = 1;
        for (String line : lines) {
            if (item.getType() == AgendaTypeCode.PAYMENT && line.startsWith("1.")) {
                DocxStyleHelper.addParagraph(doc, line);
            } else if (item.getType() == AgendaTypeCode.PAYMENT) {
                DocxStyleHelper.addParagraph(doc, (index + 1) + ". " + line);
                index++;
            } else if (item.getType() == AgendaTypeCode.ADMISSION && !line.startsWith("Принять")) {
                DocxStyleHelper.addParagraph(doc, line);
            } else {
                DocxStyleHelper.addParagraph(doc, line);
            }
        }
    }

    private void writeChairmanSignature(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "Председатель\t\t" + protocol.getChairman().getShortName());
    }
}
