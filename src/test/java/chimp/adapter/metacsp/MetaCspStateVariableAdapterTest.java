package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import chimp.model.StateVariableDefinition;
import resourceFluent.FluentScheduler;

public class MetaCspStateVariableAdapterTest {

    @Test
    public void createsOneLegacySchedulerPerExclusiveValueInOrder() {
        List<FluentScheduler> schedulers = new MetaCspStateVariableAdapter().toFluentSchedulers(
                new StateVariableDefinition("arm_posture", 2, List.of("folded", "extended")));

        assertEquals(2, schedulers.size());
        assertEquals("FluentScheduler arm_posture : 2 : folded", schedulers.get(0).toString());
        assertEquals("FluentScheduler arm_posture : 2 : extended", schedulers.get(1).toString());
    }
}
