package fraud_scoring_engine.scoring;

import fraud_scoring_engine.model.Event;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RuleDefinitions {

    public static List<Rule> allRules() {
        return List.of(

                new Rule("new_account", 20, events ->
                        earliestEventAge(events)
                                .map(age -> age.compareTo(Duration.ofDays(7)) < 0)
                                .orElse(false)
                ),

                new Rule("high_complaint_ratio", 30, events -> {
                    long complaints = countByType(events, "complaint_filed");
                    long total = events.size();
                    if (total == 0) return false;
                    return (double) complaints / total > 0.3;
                }),

                new Rule("high_event_velocity", 25, events -> {
                    long recentCount = events.stream()
                            .filter(e -> e.getEventTimestamp() != null)
                            .filter(e -> e.getEventTimestamp().isAfter(Instant.now().minus(Duration.ofHours(24))))
                            .count();
                    return recentCount > 10;
                }),

                new Rule("multiple_complaints", 15, events ->
                        countByType(events, "complaint_filed") >= 3
                )
        );
    }

    private static long countByType(List<Event> events, String type) {
        return events.stream()
                .filter(e -> type.equals(e.getType()))
                .count();
    }

    private static Optional<Duration> earliestEventAge(List<Event> events) {
        return events.stream()
                .map(Event::getEventTimestamp)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(earliest -> Duration.between(earliest, Instant.now()));
    }
}