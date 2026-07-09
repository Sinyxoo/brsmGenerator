package by.brsm.app.docgen;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.service.SpeakerResolver;
import by.brsm.app.service.TextBuilderService;
import by.brsm.app.util.DateUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Генерация отдельного файла «Постановление комитета» в формате .docx.
 *
 * <p>Шапка (двуязычный блок + эмблема БРСМ + блок «ПАСТАНОВА/ПОСТАНОВЛЕНИЕ») собрана
 * в виде двух таблиц без видимых границ, в точности как в эталонном шаблоне.</p>
 */
public class ResolutionDocxGenerator {

    /** Ширины колонок шапки (твипсы): БЕЛ-текст / зазор под эмблему / РУС-текст. */
    private static final int[] HEADER_COLUMNS = {4358, 1005, 4500};
    /** Ширины колонок блока «ПАСТАНОВА/ПОСТАНОВЛЕНИЕ» (твипсы). */
    private static final int[] META_COLUMNS = {4672, 4672};

    /** Размер эмблемы БРСМ (см. шаблон): ширина 1.88 см, высота 1.7 см, смещение вниз 0.31 см. */
    private static final int LOGO_WIDTH_EMU = 676910;
    private static final int LOGO_HEIGHT_EMU = 610870;
    private static final int LOGO_VERTICAL_OFFSET_EMU = 110490;
    private static final String LOGO_RESOURCE = "/images/brsm_logo.png";

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
            DocxStyleHelper.setPageMargins(doc);
            writeLogo(doc);
            writeBilingualHeaderTable(doc);
            DocxStyleHelper.addDoubleLineSeparator(doc);
            writeResolutionMetaTable(doc, protocol, resolutionNumber);
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

    private void writeLogo(XWPFDocument doc) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(LOGO_RESOURCE)) {
            if (is == null) {
                // Ресурс с эмблемой не найден — пропускаем изображение, но не роняем генерацию документа.
                return;
            }
            byte[] bytes = is.readAllBytes();
            DocxStyleHelper.addFloatingImage(doc, bytes, LOGO_WIDTH_EMU, LOGO_HEIGHT_EMU, LOGO_VERTICAL_OFFSET_EMU);
        } catch (Exception e) {
            throw new IOException("Не удалось вставить эмблему БРСМ в постановление", e);
        }
    }

    private void writeBilingualHeaderTable(XWPFDocument doc) {
        XWPFTable table = DocxStyleHelper.createBorderlessTable(doc, HEADER_COLUMNS);
        DocxStyleHelper.centerTable(table);

        DocxStyleHelper.setCellLines(table.getRow(0).getCell(0), List.of(
                "Грамадскае аб`яднанне",
                "«Беларускi рэспублiканскi",
                "саюз моладзi»",
                "Першасная арганізацыя з правамі раённага камітэта Беларускага нацыянальнага тэхнічнага універсытэта"
        ), DocxStyleHelper.HEADER_BLOCK_SIZE, false);

        // Средняя колонка остаётся пустой — под ней (поверх текста) плавает эмблема БРСМ.
        DocxStyleHelper.setCellText(table.getRow(0).getCell(1), "", DocxStyleHelper.HEADER_BLOCK_SIZE, false);

        DocxStyleHelper.setCellLines(table.getRow(0).getCell(2), List.of(
                "Общественное объединение",
                "«Белорусский республиканский",
                "союз молодежи»",
                "Первичная организация с правами районного комитета Белорусского национального технического университета"
        ), DocxStyleHelper.HEADER_BLOCK_SIZE, false);
    }

    private void writeResolutionMetaTable(XWPFDocument doc, Protocol protocol, int resolutionNumber) {
        XWPFTable table = DocxStyleHelper.createBorderlessTable(doc, META_COLUMNS);
        DocxStyleHelper.centerTable(table);

        DocxStyleHelper.setCellLines(table.getRow(0).getCell(0), List.of(
                "ПАСТАНОВА",
                "КАМІТЭТА",
                DateUtils.formatDocument(protocol.getDate()),
                "г.Мінск"
        ), DocxStyleHelper.META_BLOCK_SIZE, true);

        DocxStyleHelper.setCellLines(table.getRow(0).getCell(1), List.of(
                "ПОСТАНОВЛЕНИЕ",
                "КОМИТЕТА",
                "№ " + resolutionNumber,
                "г.Минск"
        ), DocxStyleHelper.META_BLOCK_SIZE, true);

        DocxStyleHelper.addBlankLine(doc);
    }

    private void writeTitle(XWPFDocument doc, String header) {
        for (String line : header.split("\\R")) {
            DocxStyleHelper.addParagraph(doc, line);
        }
        DocxStyleHelper.addBlankLine(doc);
    }

    private void writeIntro(XWPFDocument doc, AgendaItem item, Protocol protocol, Person speaker) {
        DocxStyleHelper.addIndentedParagraph(doc, textBuilderService.buildResolutionIntro(item, protocol, speaker));
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addParagraph(doc, "ПОСТАНОВЛЯЕТ:", ParagraphAlignment.LEFT, true);
    }

    /** Пункты «ПОСТАНОВЛЯЕТ:» нумеруются сквозным образом для всех типов вопросов. */
    private void writeResolutionItems(XWPFDocument doc, AgendaItem item, Protocol protocol) {
        List<String> lines = textBuilderService.buildStandaloneResolutionItems(item, protocol);
        int index = 1;
        for (String line : lines) {
            DocxStyleHelper.addIndentedParagraph(doc, index + ".\t" + line);
            index++;
        }
    }

    private void writeChairmanSignature(XWPFDocument doc, Protocol protocol) {
        DocxStyleHelper.addBlankLine(doc);
        DocxStyleHelper.addSignatureLine(doc, "Председатель", protocol.getChairman().getShortName());
    }
}
