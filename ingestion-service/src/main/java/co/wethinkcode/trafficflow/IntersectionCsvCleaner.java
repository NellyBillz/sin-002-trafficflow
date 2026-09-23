package co.wethinkcode.trafficflow;

import com.opencsv.CSVReader;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class IntersectionCsvCleaner {

    private static final Set<String> PLACEHOLDERS = Set.of(
            "n/a", "tbd", "unknown", "-", "nan");

    public List<IntersectionRecord> clean(Reader source) throws Exception {
        Map<String, IntersectionRecord> unique = new LinkedHashMap<>();
        try (CSVReader reader = new CSVReader(source)) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) {
                    continue;
                }
                String id = normalizeId(row[0]);
                if (id == null) {
                    continue;
                }
                IntersectionRecord current = new IntersectionRecord(
                        id,
                        titleCase(row[1]),
                        normalizeSignalType(row[2]),
                        normalizeBoolean(row[3]));
                unique.merge(id, current, this::mergeMissingValues);
            }
        }
        return List.copyOf(unique.values());
    }

    private IntersectionRecord mergeMissingValues(IntersectionRecord first,
                                                   IntersectionRecord duplicate) {
        return new IntersectionRecord(
                first.id(),
                first.district() != null ? first.district() : duplicate.district(),
                first.signalType() != null ? first.signalType() : duplicate.signalType(),
                first.active() != null ? first.active() : duplicate.active());
    }

    private String normalizeId(String value) {
        String cleaned = cleanText(value);
        return cleaned == null ? null : cleaned.toUpperCase(Locale.ROOT);
    }

    private String titleCase(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null) {
            return null;
        }
        String[] words = cleaned.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private String normalizeSignalType(String value) {
        String cleaned = cleanText(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private Boolean normalizeBoolean(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null) {
            return null;
        }
        return switch (cleaned.toLowerCase(Locale.ROOT)) {
            case "y", "yes", "1", "true" -> true;
            case "n", "no", "0", "false" -> false;
            default -> null;
        };
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty() || PLACEHOLDERS.contains(cleaned.toLowerCase(Locale.ROOT))) {
            return null;
        }
        return cleaned;
    }
}
