package by.brsm.app.service;

import by.brsm.app.model.AdminRole;
import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Administrator;
import by.brsm.app.model.FacultySecretary;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;

import java.util.Map;

/**
 * Определяет докладчика в блоке «СЛУШАЛИ» по типу вопроса.
 */
public class SpeakerResolver {

    private final by.brsm.app.dao.AdministratorDao administratorDao;
    private final by.brsm.app.dao.FacultySecretaryDao facultySecretaryDao;

    public SpeakerResolver(by.brsm.app.dao.AdministratorDao administratorDao,
                           by.brsm.app.dao.FacultySecretaryDao facultySecretaryDao) {
        this.administratorDao = administratorDao;
        this.facultySecretaryDao = facultySecretaryDao;
    }

    public Person resolveSlushaliSpeaker(AgendaItem item, Protocol protocol) {
        if (item.getSpeaker() != null) {
            return item.getSpeaker();
        }
        return switch (item.getType()) {
            case SETTLEMENT -> resolveFacultySecretary(item);
            case PAYMENT, DOCUMENT_APPROVAL -> resolveMainSecretary(protocol);
            case COMMITTEE_CHANGE, STAFF_FORMATION, ADMISSION -> resolveDeputySecretary(protocol);
            case FREEFORM -> item.getSpeaker();
        };
    }

    public Administrator resolveAgendaDaySpeaker(Protocol protocol) {
        try {
            return administratorDao.findByRole(AdminRole.SECRETARY)
                    .orElse(protocol.getChairman());
        } catch (Exception e) {
            return protocol.getChairman();
        }
    }

    private Person resolveFacultySecretary(AgendaItem item) {
        Map<String, Object> fields = item.getFields();
        Object facultyId = fields.get("facultyId");
        if (facultyId == null) {
            return null;
        }
        try {
            int id = ((Number) facultyId).intValue();
            return facultySecretaryDao.findByFacultyId(id).map(s -> (Person) s).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Administrator resolveMainSecretary(Protocol protocol) {
        return protocol.getChairman();
    }

    private Administrator resolveDeputySecretary(Protocol protocol) {
        return protocol.getMeetingSecretary();
    }
}
