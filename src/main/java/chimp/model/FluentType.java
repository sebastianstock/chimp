package chimp.model;

public enum FluentType {
    ACTIVITY("Activity"),
    STATE("State"),
    PLANNED_STATE("PlannedState"),
    TASK("Task");

    private final String componentName;

    FluentType(String componentName) {
        this.componentName = componentName;
    }

    public String componentName() {
        return componentName;
    }
}
