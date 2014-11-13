package edu.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author spupyrev
 * Nov 12, 2014
 */
@RunWith(Suite.class)
@SuiteClasses({
        GenericCloudTest.class,
        ClusterTest.class,
        LayoutTest.class,
        MDSTest.class,
        LexRankTest.class,
        GeometryTest.class})
public class AllTests
{

}
