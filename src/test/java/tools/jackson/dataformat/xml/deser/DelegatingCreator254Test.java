package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DelegatingCreator254Test extends XmlTestUtil
{
    static class Foo {
        public Bar bar;
    }

    static class Bar {
        Integer value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Bar(int i) {
            value = i;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#254]: Coercion needed for int-taking creator (as XML can
    // not natively detect scalars other than Strings)
    @Test
    public void testIntDelegatingCreator() throws Exception
    {
        Foo foo = MAPPER.readValue(
"<foo>\n" +
"   <bar>   28   </bar>\n" +
"</foo>", Foo.class);
        assertNotNull(foo.bar);
        assertEquals(Integer.valueOf(28), foo.bar.value);
    }
}
