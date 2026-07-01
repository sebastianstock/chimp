package chimp.model;

import java.util.List;

public interface FluentView {

    FluentId id();

    FluentType type();

    FluentSignature signature();

    FluentMarking marking();

    TemporalInterval temporalInterval();

    List<IntegerValue> integerValues();
}
