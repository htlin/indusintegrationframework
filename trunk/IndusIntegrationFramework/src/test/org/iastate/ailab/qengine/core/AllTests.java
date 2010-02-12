package org.iastate.ailab.qengine.core;

import org.iastate.ailab.qengine.SanityTestQueries;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author neeraj
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { SanityTestQueries.class, BasicQueriesPelletTest.class })
//@Suite.SuiteClasses( { BasicQueriesPelletTest.class })
public class AllTests {
}
