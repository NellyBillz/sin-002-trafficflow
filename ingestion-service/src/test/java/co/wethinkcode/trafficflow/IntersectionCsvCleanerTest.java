package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntersectionCsvCleanerTest {

    @Test
    void normalizesAndDeduplicatesLegacyRows() throws Exception {
        String csv = """
                intersection_id,District ,signal_type,active_flag
                 int-1005 ,downTown  central,ROUNDABOUT,TRUE
                INT-1005,Downtown Central,roundabout,Y
                INT-1007,Eastside,,1
                INT-1013,Westside,unknown,unknown
                """;

        List<IntersectionRecord> records = new IntersectionCsvCleaner()
                .clean(new StringReader(csv));

        assertEquals(3, records.size());
        assertEquals(new IntersectionRecord(
                "INT-1005", "Downtown Central", "roundabout", true), records.get(0));
        assertNull(records.get(1).signalType());
        assertNull(records.get(2).signalType());
        assertNull(records.get(2).active());
    }
}
