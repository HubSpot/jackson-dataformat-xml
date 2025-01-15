package tools.jackson.dataformat.xml.lists;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Failing unit test(s) wrt [Issue#64]
public class Issue101UnwrappedListAttributesTest extends XmlTestUtil
{
    // For [dataformat-xml#101]
    @JsonRootName("root")    
    @JsonPropertyOrder({ "unwrapped", "name" })
    static class Root {
        @JacksonXmlProperty(localName = "unwrapped")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<UnwrappedElement> unwrapped;

        public String name;
    }
     @JsonPropertyOrder({ "id", "type" })
     static class UnwrappedElement {
        public UnwrappedElement () {}

        public UnwrappedElement (String id, String type) {
            this.id = id;
            this.type = type;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String id;

        @JacksonXmlProperty(isAttribute = true)
        public String type;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#101]
    @Test
    public void testWithTwoAttributes() throws Exception
    {
        final String EXP = "<root>"
                +"<unwrapped id=\"1\" type=\"string\"/>"
                +"<unwrapped id=\"2\" type=\"string\"/>"
                +"<name>test</name>"
                +"</root>";
        Root rootOb = new Root();
        rootOb.unwrapped = Arrays.asList(
                new UnwrappedElement("1", "string"),
                new UnwrappedElement("2", "string")
        );
        rootOb.name = "test";

        // First, serialize, which works
        String xml = MAPPER.writeValueAsString(rootOb);
        assertEquals(EXP, xml);

        // then try deserialize
        Root result = MAPPER.readValue(xml, Root.class);
        assertNotNull(result);
        assertEquals(rootOb.name, result.name);
    }
}
