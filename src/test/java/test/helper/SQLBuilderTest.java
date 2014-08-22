package test.helper;

import org.junit.*;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class SQLBuilderTest {
    @Test
    public void testToSnakeCase() throws Exception {
        assertThat(SQLBuilder.toSnakeCase("abc"), is("abc"));
        assertThat(SQLBuilder.toSnakeCase("aBc"), is("a_bc"));
        assertThat(SQLBuilder.toSnakeCase("aBC"), is("a_bc"));
        assertThat(SQLBuilder.toSnakeCase("aBcD"), is("a_bc_d"));
        assertThat(SQLBuilder.toSnakeCase("Abc"), is("abc"));
    }

    @Test
    public void testToCamelCase() throws Exception {
        assertThat(SQLBuilder.toCamelCase("abc"), is("abc"));
        assertThat(SQLBuilder.toCamelCase("a_bc"), is("aBc"));
        assertThat(SQLBuilder.toCamelCase("a_bc_d"), is("aBcD"));
        assertThat(SQLBuilder.toCamelCase("abc"), is("abc"));
    }

    @Test
    public void testBuildInsert1() throws Exception {
        assertThat(SQLBuilder.buildInsert("test1", Arrays.asList("abc")),
                is("insert into test1 (abc) values (:abc)"));

        assertThat(SQLBuilder.buildInsert("test2", Arrays.asList("abc", "def")),
                is("insert into test2 (abc, def) values (:abc, :def)"));

        assertThat(SQLBuilder.buildInsert("test2", Arrays.asList("abc", "def")),
                is("insert into test2 (abc, def) values (:abc, :def)"));
    }

    @Test
    public void testBuildInsert2() throws Exception {
        class Abc {
            public int abc;
            public String def;
        }
        assertThat(SQLBuilder.buildInsert("test1", new Abc()),
                is("insert into test1 (abc, def) values (:abc, :def)"));
    }

}
