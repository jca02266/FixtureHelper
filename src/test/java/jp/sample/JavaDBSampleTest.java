package jp.sample;

import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.helper.FixtureHelper;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext.xml")
public class JavaDBSampleTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    JavaDBSample obj;

    FixtureHelper fh;

    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        fh = new FixtureHelper(dataSource);
        fh.loadFixtures("test_pattern1");
    }

    @Test
    public void testSampleSelect() throws IOException {
        List<Result> expect = fh.getBeans(Result.class, "test_pattern1", "expect");
        List<Result> actual = obj.sampleSelect();
        assertThat(actual, is(expect));
    }
}
