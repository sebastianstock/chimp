package htn;

import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class OrderingConstraintTemplate {

    private EffectTemplate from;
    private EffectTemplate to;
    private FluentConstraint constraint = null;

    public OrderingConstraintTemplate(EffectTemplate from, EffectTemplate to) {
        this.from = from;
        this.to = to;
    }

    public EffectTemplate getFrom() {
        return from;
    }

    public EffectTemplate getTo() {
        return to;
    }

    public FluentConstraint getConstraint(FluentNetworkSolver groundSolver) {
        if (constraint == null) {
            createConstraint(groundSolver);
        }
        return constraint;
    }

    private void createConstraint(FluentNetworkSolver groundSolver) {
        constraint = new FluentConstraint(FluentConstraint.Type.BEFORE);
        constraint.setFrom(from.getPrototype(groundSolver));
        constraint.setTo(to.getPrototype(groundSolver));
    }
}
