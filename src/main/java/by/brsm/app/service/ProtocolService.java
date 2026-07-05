package by.brsm.app.service;

import by.brsm.app.dao.AgendaItemDao;
import by.brsm.app.dao.ProtocolDao;
import by.brsm.app.docgen.ProtocolDocxGenerator;
import by.brsm.app.docgen.ResolutionDocxGenerator;
import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Оркестрация сохранения протокола и генерации документов.
 */
public class ProtocolService {

    public record GeneratedFile(String title, Path path) {
    }

    public record GenerationResult(Path protocolFile, List<GeneratedFile> resolutions) {
    }

    private final ProtocolDao protocolDao;
    private final AgendaItemDao agendaItemDao;
    private final NumberingService numberingService;
    private final ValidationService validationService;
    private final ProtocolDocxGenerator protocolDocxGenerator;
    private final ResolutionDocxGenerator resolutionDocxGenerator;
    private final SpeakerResolver speakerResolver;

    public ProtocolService(ProtocolDao protocolDao,
                           AgendaItemDao agendaItemDao,
                           NumberingService numberingService,
                           ValidationService validationService,
                           ProtocolDocxGenerator protocolDocxGenerator,
                           ResolutionDocxGenerator resolutionDocxGenerator,
                           SpeakerResolver speakerResolver) {
        this.protocolDao = protocolDao;
        this.agendaItemDao = agendaItemDao;
        this.numberingService = numberingService;
        this.validationService = validationService;
        this.protocolDocxGenerator = protocolDocxGenerator;
        this.resolutionDocxGenerator = resolutionDocxGenerator;
        this.speakerResolver = speakerResolver;
    }

    public List<String> validate(Protocol protocol) throws SQLException {
        List<String> errors = new ArrayList<>(validationService.validate(protocol));
        if (protocolDao.existsByNumber(protocol.getNumber(), protocol.getId() > 0 ? protocol.getId() : null)) {
            errors.add("Протокол с номером " + protocol.getNumber() + " уже существует.");
        }
        return errors;
    }

    public int suggestNextProtocolNumber() throws SQLException {
        return numberingService.suggestNextProtocolNumber();
    }

    public GenerationResult generateDocuments(Protocol protocol) throws Exception {
        List<String> errors = validate(protocol);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("\n", errors));
        }

        for (AgendaItem item : protocol.getItems()) {
            if (item.getSpeaker() == null) {
                item.setSpeaker(speakerResolver.resolveSlushaliSpeaker(item, protocol));
            }
        }

        List<Integer> attendeeIds = protocol.getAttendees().stream().map(Person::getId).toList();
        protocolDao.insert(protocol, attendeeIds);
        reindexItems(protocol);
        agendaItemDao.insertAll(protocol.getId(), protocol.getItems());

        Path protocolFile = protocolDocxGenerator.generate(protocol);
        protocolDao.updateFilePath(protocol.getId(), protocolFile.toString());
        numberingService.syncProtocolCounter(protocol.getNumber());

        Path outputDir = protocolFile.getParent();
        List<GeneratedFile> resolutions = new ArrayList<>();
        for (AgendaItem item : protocol.getItems()) {
            if (!item.isRequiresResolution()) {
                continue;
            }
            int resolutionNumber = numberingService.nextResolutionNumber();
            item.setResolutionNumber(resolutionNumber);
            Path resolutionFile = resolutionDocxGenerator.generate(protocol, item, resolutionNumber, outputDir);
            item.setResolutionFilePath(resolutionFile.toString());
            resolutions.add(new GeneratedFile("Постановление №" + resolutionNumber, resolutionFile));
        }
        return new GenerationResult(protocolFile, resolutions);
    }

    public List<Protocol> loadHistory() throws SQLException {
        return protocolDao.findAllSummaries();
    }

    private void reindexItems(Protocol protocol) {
        int index = 1;
        for (AgendaItem item : protocol.getItems()) {
            item.setOrderIndex(index++);
        }
    }
}
