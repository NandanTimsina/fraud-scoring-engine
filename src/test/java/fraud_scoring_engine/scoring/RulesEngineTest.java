package fraud_scoring_engine.scoring;

import fraud_scoring_engine.model.Event;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RulesEngineTest {

    private final RulesEngine engine = new RulesEngine();

    private static Instant ago(Duration d) {
        return Instant.now().minus(d);
    }

    private static List<Event> events(int count, String type, Instant time) {
        List<Event> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Event e = new Event();
            e.setType(type);
            e.setEventTimestamp(time);
            list.add(e);
        }
        return list;
    }

    private static List<Object> ruleNames(RulesEngine.ScoreResult result) {
        return result.getTriggeredRules().stream().map(m -> m.get("rule")).toList();
    }

    @Test
    void noEvents_scoresZero() {
        RulesEngine.ScoreResult result = engine.evaluate(List.of());

        assertThat(result.getScore()).isZero();
        assertThat(result.getTriggeredRules()).isEmpty();
    }

    @Test
    void newAccount_isFlagged() {
        List<Event> list = events(1, "listing_posted", ago(Duration.ofHours(1)));

        RulesEngine.ScoreResult result = engine.evaluate(list);

        assertThat(ruleNames(result)).containsExactly("new_account");
        assertThat(result.getScore()).isEqualTo(20);
    }

    @Test
    void establishedQuietUser_scoresZero() {
        List<Event> list = events(2, "listing_posted", ago(Duration.ofDays(30)));

        assertThat(engine.evaluate(list).getScore()).isZero();
    }

    @Test
    void highComplaintRatio_isFlagged() {
        List<Event> list = new ArrayList<>();
        list.addAll(events(2, "complaint_filed", ago(Duration.ofDays(20))));
        list.addAll(events(2, "listing_posted", ago(Duration.ofDays(20))));

        RulesEngine.ScoreResult result = engine.evaluate(list);

        assertThat(ruleNames(result)).containsExactly("high_complaint_ratio");
        assertThat(result.getScore()).isEqualTo(30);
    }

    @Test
    void highEventVelocity_isFlagged() {
        List<Event> list = new ArrayList<>();
        list.addAll(events(1, "listing_posted", ago(Duration.ofDays(30))));
        list.addAll(events(11, "listing_posted", ago(Duration.ofHours(1))));

        RulesEngine.ScoreResult result = engine.evaluate(list);

        assertThat(ruleNames(result)).containsExactly("high_event_velocity");
        assertThat(result.getScore()).isEqualTo(25);
    }

    @Test
    void allRulesFire_scoreIsSumOfWeights() {
        List<Event> list = new ArrayList<>();
        list.addAll(events(6, "complaint_filed", ago(Duration.ofHours(1))));
        list.addAll(events(6, "listing_posted", ago(Duration.ofHours(1))));

        RulesEngine.ScoreResult result = engine.evaluate(list);

        assertThat(ruleNames(result)).containsExactlyInAnyOrder(
                "new_account", "high_complaint_ratio", "high_event_velocity", "multiple_complaints");
        assertThat(result.getScore()).isEqualTo(90);
    }
}