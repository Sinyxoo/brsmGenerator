package by.brsm.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Преобразование суммы в пропись на русском языке (белорусские рубли).
 */
public class SumInWordsService {

    private static final String[] UNITS = {
            "", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять",
            "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать",
            "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"
    };
    private static final String[] TENS = {
            "", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"
    };
    private static final String[] HUNDREDS = {
            "", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"
    };

    public String toWords(BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        long rubles = normalized.longValue();
        int kopecks = normalized.remainder(BigDecimal.ONE).movePointRight(2).intValue();
        String rublesText = capitalize(triadsToWords(rubles, true));
        String rubWord = rubleForm(rubles);
        return rublesText + " " + rubWord + " " + String.format("%02d", kopecks) + " копеек";
    }

    public String toWords(String amountText) {
        return toWords(new BigDecimal(amountText.replace(',', '.')));
    }

    private String triadsToWords(long number, boolean female) {
        if (number == 0) {
            return "ноль";
        }
        StringBuilder sb = new StringBuilder();
        int triadIndex = 0;
        while (number > 0) {
            int triad = (int) (number % 1000);
            if (triad != 0) {
                String triadText = triadToWords(triad, triadIndex == 1 ? true : female && triadIndex == 0);
                sb.insert(0, triadText + " " + triadName(triad, triadIndex) + " ");
            }
            number /= 1000;
            triadIndex++;
        }
        return sb.toString().trim();
    }

    private String triadToWords(int triad, boolean female) {
        int hundreds = triad / 100;
        int rest = triad % 100;
        StringBuilder sb = new StringBuilder();
        if (hundreds > 0) {
            sb.append(HUNDREDS[hundreds]).append(" ");
        }
        if (rest < 20) {
            if (rest > 0) {
                sb.append(adjustGender(rest, female));
            }
        } else {
            sb.append(TENS[rest / 10]).append(" ");
            int unit = rest % 10;
            if (unit > 0) {
                sb.append(adjustGender(unit, female));
            }
        }
        return sb.toString().trim();
    }

    private String adjustGender(int value, boolean female) {
        if (!female) {
            return UNITS[value];
        }
        return switch (value) {
            case 1 -> "одна";
            case 2 -> "две";
            default -> UNITS[value];
        };
    }

    private String triadName(int triad, int index) {
        int mod100 = triad % 100;
        int mod10 = triad % 10;
        return switch (index) {
            case 1 -> {
                if (mod100 >= 11 && mod100 <= 19) yield "тысяч";
                yield switch (mod10) {
                    case 1 -> "тысяча";
                    case 2, 3, 4 -> "тысячи";
                    default -> "тысяч";
                };
            }
            case 2 -> {
                if (mod100 >= 11 && mod100 <= 19) yield "миллионов";
                yield switch (mod10) {
                    case 1 -> "миллион";
                    case 2, 3, 4 -> "миллиона";
                    default -> "миллионов";
                };
            }
            default -> "";
        };
    }

    private String rubleForm(long rubles) {
        long mod100 = rubles % 100;
        long mod10 = rubles % 10;
        if (mod100 >= 11 && mod100 <= 19) {
            return "рублей";
        }
        return switch ((int) mod10) {
            case 1 -> "рубль";
            case 2, 3, 4 -> "рубля";
            default -> "рублей";
        };
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
