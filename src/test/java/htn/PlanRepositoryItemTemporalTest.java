package htn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import chimp.model.TemporalBounds;

public class PlanRepositoryItemTemporalTest {

    @Test
    public void storesDurationAsNeutralBounds() {
        HTNOperator operator = new HTNOperator(
                "move",
                new String[0],
                new HTNPrecondition[0],
                new EffectTemplate[0],
                0);
        TemporalBounds duration = new TemporalBounds(5, 20);

        operator.setDurationBounds(duration);

        assertEquals(duration, operator.getDurationBounds());
    }
}
