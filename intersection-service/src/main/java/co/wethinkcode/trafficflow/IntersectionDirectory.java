package co.wethinkcode.trafficflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class IntersectionDirectory {

    private final List<IntersectionRecord> intersections;
    private final Map<String, IntersectionRecord> byId = new LinkedHashMap<>();

    public IntersectionDirectory(List<IntersectionRecord> intersections) {
        this.intersections = List.copyOf(intersections);
        this.intersections.forEach(record -> byId.put(normalize(record.id()), record));
    }

    public List<IntersectionRecord> intersections() {
        return intersections;
    }

    public Optional<IntersectionRecord> findById(String id) {
        return Optional.ofNullable(byId.get(normalize(id)));
    }

    public List<IntersectionRecord> findByDistrict(String district) {
        String expected = normalize(district);
        return intersections.stream()
                .filter(record -> normalize(record.district()).equals(expected))
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
