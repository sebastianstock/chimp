package unify;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({JunitTestCompoundNameMatchingConstraintSolver.class,
	JUnitTestNameMatchingConstraintSolver.class, 
	TestCompoundNameVariable.class, 
	TestNameVariable.class})
public class AllTests {

}
