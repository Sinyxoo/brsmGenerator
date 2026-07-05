package by.brsm.app.service;

import by.brsm.app.model.Person;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сортировка ФИО по фамилии и сборка строки «Присутствовали».
 */
public class NameSortService {

    public String buildAttendeesLine(List<Person> attendees) {
        return attendees.stream()
                .sorted(Comparator.comparing(this::extractSurname, String.CASE_INSENSITIVE_ORDER))
                .map(Person::getShortName)
                .collect(Collectors.joining(", "));
    }

    private String extractSurname(Person person) {
        String shortName = person.getShortName();
        if (shortName == null || shortName.isBlank()) {
            return "";
        }
        return shortName.split("\\s+")[0];
    }
}
