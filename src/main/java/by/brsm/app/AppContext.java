package by.brsm.app;

import by.brsm.app.dao.AdministratorDao;
import by.brsm.app.dao.AgendaItemDao;
import by.brsm.app.dao.CounterDao;
import by.brsm.app.dao.DataSeeder;
import by.brsm.app.dao.DatabaseManager;
import by.brsm.app.dao.FacultyDao;
import by.brsm.app.dao.FacultySecretaryDao;
import by.brsm.app.dao.ProtocolDao;
import by.brsm.app.dao.StaffTypeDao;
import by.brsm.app.docgen.AppendixTableGenerator;
import by.brsm.app.docgen.ProtocolDocxGenerator;
import by.brsm.app.docgen.ResolutionDocxGenerator;
import by.brsm.app.service.BackupService;
import by.brsm.app.service.NameSortService;
import by.brsm.app.service.NumberingService;
import by.brsm.app.service.ProtocolService;
import by.brsm.app.service.SpeakerResolver;
import by.brsm.app.service.SumInWordsService;
import by.brsm.app.service.TextBuilderService;
import by.brsm.app.service.ValidationService;
import by.brsm.app.util.AppPaths;

/**
 * Контейнер зависимостей приложения (инициализация при старте).
 */
public final class AppContext {

    private static AppContext instance;

    private final DatabaseManager databaseManager;
    private final FacultyDao facultyDao;
    private final FacultySecretaryDao facultySecretaryDao;
    private final AdministratorDao administratorDao;
    private final StaffTypeDao staffTypeDao;
    private final ProtocolDao protocolDao;
    private final AgendaItemDao agendaItemDao;
    private final CounterDao counterDao;
    private final ProtocolService protocolService;
    private final BackupService backupService;

    private AppContext() throws Exception {
        AppPaths.init();
        backupService = new BackupService();
        backupService.backupDatabaseIfExists();

        databaseManager = new DatabaseManager();
        databaseManager.initializeSchema();

        facultyDao = new FacultyDao(databaseManager);
        administratorDao = new AdministratorDao(databaseManager);
        facultySecretaryDao = new FacultySecretaryDao(databaseManager, facultyDao);
        staffTypeDao = new StaffTypeDao(databaseManager);
        counterDao = new CounterDao(databaseManager);
        protocolDao = new ProtocolDao(databaseManager, administratorDao);
        agendaItemDao = new AgendaItemDao(databaseManager);

        new DataSeeder(facultyDao, facultySecretaryDao, administratorDao, staffTypeDao, counterDao).seedIfEmpty();

        SumInWordsService sumInWordsService = new SumInWordsService();
        TextBuilderService textBuilderService = new TextBuilderService(sumInWordsService);
        SpeakerResolver speakerResolver = new SpeakerResolver(administratorDao, facultySecretaryDao);
        NameSortService nameSortService = new NameSortService();
        ValidationService validationService = new ValidationService();
        NumberingService numberingService = new NumberingService(counterDao, protocolDao);
        AppendixTableGenerator appendixTableGenerator = new AppendixTableGenerator();
        ProtocolDocxGenerator protocolDocxGenerator =
                new ProtocolDocxGenerator(textBuilderService, speakerResolver, nameSortService);
        ResolutionDocxGenerator resolutionDocxGenerator =
                new ResolutionDocxGenerator(textBuilderService, speakerResolver, appendixTableGenerator);

        protocolService = new ProtocolService(
                protocolDao, agendaItemDao, numberingService, validationService,
                protocolDocxGenerator, resolutionDocxGenerator, speakerResolver);
    }

    public static synchronized AppContext getInstance() throws Exception {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public FacultyDao getFacultyDao() {
        return facultyDao;
    }

    public FacultySecretaryDao getFacultySecretaryDao() {
        return facultySecretaryDao;
    }

    public AdministratorDao getAdministratorDao() {
        return administratorDao;
    }

    public StaffTypeDao getStaffTypeDao() {
        return staffTypeDao;
    }

    public ProtocolService getProtocolService() {
        return protocolService;
    }

    public SpeakerResolver getSpeakerResolver() {
        return new SpeakerResolver(administratorDao, facultySecretaryDao);
    }

    public TextBuilderService getTextBuilderService() {
        return new TextBuilderService(new SumInWordsService());
    }
}
