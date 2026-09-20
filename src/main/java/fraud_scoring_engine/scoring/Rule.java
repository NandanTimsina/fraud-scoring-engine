package fraud_scoring_engine.scoring;

import fraud_scoring_engine.model.Event;
import lombok.Getter;

import java.util.List;
import java.util.function.Predicate;

@Getter
public class Rule {

    private final String name;
    private final int weight;
    private final Predicate<List<Event>> condition;

    public Rule(String name, int weight, Predicate<List<Event>> condition) {
        this.name = name;
        this.weight = weight;
        this.condition = condition;
    }

    public boolean evaluate(List<Event> events) {
        return condition.test(events);
    }
}