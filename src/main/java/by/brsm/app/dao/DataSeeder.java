package by.brsm.app.dao;

import by.brsm.app.model.AdminRole;
import by.brsm.app.model.Faculty;
import by.brsm.app.model.FacultySecretary;
import by.brsm.app.model.StaffType;

import java.sql.SQLException;

/**
 * Первичное наполнение справочников данными из архивных протоколов.
 */
public class DataSeeder {

    private final FacultyDao facultyDao;
    private final FacultySecretaryDao facultySecretaryDao;
    private final AdministratorDao administratorDao;
    private final StaffTypeDao staffTypeDao;
    private final CounterDao counterDao;

    public DataSeeder(FacultyDao facultyDao,
                      FacultySecretaryDao facultySecretaryDao,
                      AdministratorDao administratorDao,
                      StaffTypeDao staffTypeDao,
                      CounterDao counterDao) {
        this.facultyDao = facultyDao;
        this.facultySecretaryDao = facultySecretaryDao;
        this.administratorDao = administratorDao;
        this.staffTypeDao = staffTypeDao;
        this.counterDao = counterDao;
    }

    public void seedIfEmpty() throws SQLException {
        if (!facultyDao.findAll().isEmpty()) {
            return;
        }

        String[] faculties = {"ФГДИЭ", "СТФ", "ФЭС", "ФИТР", "СФ", "МСФ", "ФТК", "АТФ", "ВТФ", "МТФ", "ПСФ", "ИПФ", "АФ", "ФММП", "ФТУГ", "ЭФ"};
        for (String shortName : faculties) {
            Faculty f = new Faculty();
            f.setShortName(shortName);
            f.setFullName(shortName);
            facultyDao.insert(f);
        }

        var adminDao = administratorDao;
        var sec = new by.brsm.app.model.Administrator();
        sec.setShortName("Позневич К.Ю.");
        sec.setFullNameGenitive("Позневича Кирилла Юрьевича");
        sec.setRole(AdminRole.SECRETARY);
        adminDao.insert(sec);

        var dep = new by.brsm.app.model.Administrator();
        dep.setShortName("Федорович Р.В.");
        dep.setFullNameGenitive("Федоровича Родиона Вячеславовича");
        dep.setRole(AdminRole.DEPUTY_SECRETARY);
        adminDao.insert(dep);

        seedSecretary("Аронова Е.А.", "Ароновой Екатерины Александровны", "ФИТР");
        seedSecretary("Довжик И.А.", "Довжика Ильи Александровича", "ФТК");
        seedSecretary("Дубинина Е.С.", "Дубининой Евгении Сергеевны", "ВТФ");
        seedSecretary("Караневский Н.А.", "Караневского Никиты Александровича", "АТФ");
        seedSecretary("Коледа А.С.", "Коледы Анастасии Сергеевны", "ФММП");
        seedSecretary("Кравчук А.Д.", "Кравчука Артемия Дмитриевича", "ПСФ");
        seedSecretary("Красникова С.Д.", "Красниковой Сабины Денисовны", "ФММП");
        seedSecretary("Мацукевич К.А.", "Мацукевич Карины Александровны", "ФЭС");
        seedSecretary("Пинчук М.П.", "Пинчук Марии Петровны", "МСФ");
        seedSecretary("Радюк Д.А.", "Радюка Даниила Александровича", "ФММП");
        seedSecretary("Разуева Д.А.", "Разуевой Дианы Александровны", "ИПФ");
        seedSecretary("Римашевская Д.В.", "Римашевской Дарьи Владимировны", "АФ");
        seedSecretary("Сацута С.В.", "Сацуты Сергея Васильевича", "СФ");
        seedSecretary("Скобля В.С.", "Скобли Виктории Сергеевны", "ФТУГ");
        seedSecretary("Терехова А.В.", "Тереховой Анастасии Владиславовны", "ФТК");
        seedSecretary("Федосенко Е.Р.", "Федосенко Екатерины Романовны", "СТФ");
        seedSecretary("Шульц А.М.", "Шульц Анны Михайловны", "ФГДИЭ");

        StaffType staff1 = new StaffType();
        staff1.setShortName("трудовых дел");
        staff1.setFullName("Территориального штаба трудовых дел");
        staffTypeDao.insert(staff1);

        StaffType staff2 = new StaffType();
        staff2.setShortName("студенческих отрядов");
        staff2.setFullName("Территориального штаба студенческих отрядов");
        staffTypeDao.insert(staff2);

        counterDao.setValue("protocol_number", 68);
        counterDao.setValue("resolution_number", 10);
    }

    private void seedSecretary(String shortName, String genitive, String facultyShort) throws SQLException {
        FacultySecretary secretary = new FacultySecretary();
        secretary.setShortName(shortName);
        secretary.setFullNameGenitive(genitive);
        Faculty faculty = facultyDao.findAll().stream()
                .filter(f -> f.getShortName().equals(facultyShort))
                .findFirst()
                .orElseThrow();
        secretary.setFaculty(faculty);
        facultySecretaryDao.insert(secretary);
    }
}
