package unitTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        estimateTest.class}) // TODO add: findOutliersTest, inferTest, repairTest, selectTest, simulateTest

public class allQueryTests {
}