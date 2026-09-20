package fraud_scoring_engine.scoring;

import fraud_scoring_engine.model.Event;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesEngine {

    public ScoreResult evaluate(List<Event> events) {
        List<Rule> rules = RuleDefinitions.allRules();

        int totalScore = 0;
        List<Map<String, Object>> triggeredRules = new ArrayList<>();

        for (Rule rule : rules) {
            if (rule.evaluate(events)) {
                totalScore += rule.getWeight();

                Map<String, Object> triggered = new HashMap<>();
                triggered.put("rule", rule.getName());
                triggered.put("weight", rule.getWeight());
                triggeredRules.add(triggered);
            }
        }

        return new ScoreResult(totalScore, triggeredRules);
    }

    @Getter
    public static class ScoreResult {
        private final int score;
        private final List<Map<String, Object>> triggeredRules;

        public ScoreResult(int score, List<Map<String, Object>> triggeredRules) {
            this.score = score;
            this.triggeredRules = triggeredRules;
        }
    }
}