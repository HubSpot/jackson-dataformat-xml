package tools.jackson.dataformat.xml.ser;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.databind.SerializationFeature;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;

import static org.junit.jupiter.api.Assertions.*;

public class XmlPrettyPrinterTest extends XmlTestUtil
{
    static class StringWrapperBean {
        public StringWrapper string;

        public StringWrapperBean() { }
        public StringWrapperBean(String s) { string = new StringWrapper(s); }
    }

    static class IntWrapperBean {
        public IntWrapper wrapped;

        public IntWrapperBean() { }
        public IntWrapperBean(int i) { wrapped = new IntWrapper(i); }
    }

    // [dataformat-xml#45]
    static class AttrBean {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;
    }

    static class AttrBean2 {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;

        public int value = 14;
    }

    public class PojoFor123
    {
        @JacksonXmlProperty(isAttribute = true)
        public String name;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String property;
        
        public PojoFor123(String name) {
            this.name = name;
        }
    }

    // for [dataformat-xml#172]
    static class Company {
        @JacksonXmlElementWrapper(localName="e")
        public List<Employee> employee = new ArrayList<Employee>();
    }

    @JsonPropertyOrder({"id", "type"})
    static class Employee {
        public String id;
        public EmployeeType type;

        public Employee(String id) {
            this.id = id;
            type = EmployeeType.FULL_TIME;
        }
    }

    static enum EmployeeType {
        FULL_TIME;
    }

    /*
    /**********************************************************
    /* Set up
    /**********************************************************
     */

    protected XmlMapper _xmlMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @BeforeEach
    public void setUp() throws Exception {
        _xmlMapper = XmlMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // Verify [dataformat-xml#1]
    @Test
    public void testSimpleStringBean() throws Exception
    {
        StringWrapperBean input = new StringWrapperBean("abc");
        String xml = _xmlMapper.writeValueAsString(input); 

        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        StringWrapperBean result = _xmlMapper.readValue(xml, StringWrapperBean.class);
        assertNotNull(result);
        assertEquals("abc", result.string.str);

        // Try via ObjectWriter as well
        xml = _xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        result = _xmlMapper.readValue(xml, StringWrapperBean.class);
        assertNotNull(result);
        assertEquals("abc", result.string.str);
    }

    @Test
    public void testSimpleIntBean() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new IntWrapperBean(42)); 
        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
        	fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        IntWrapperBean result = _xmlMapper.readValue(xml, IntWrapperBean.class);
        assertNotNull(result);
        assertEquals(42, result.wrapped.i);
    }
    
    @Test
    public void testSimpleMap() throws Exception
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        String xml = _xmlMapper.writeValueAsString(map);

        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        
        // Let's verify we get similar stuff back, first:
        Map<?,?> result = _xmlMapper.readValue(xml, Map.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("b", map.get("a"));
    }

    // [dataformat-xml#45]: Use of attributes should not force linefeed for empty elements
    @Test
    public void testWithAttr() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new AttrBean());
        assertEquals("<AttrBean count=\"3\"/>" + DEFAULT_NEW_LINE, xml);
        String xml2 = _xmlMapper.writeValueAsString(new AttrBean2());
        assertEquals(
            "<AttrBean2 count=\"3\">" + DEFAULT_NEW_LINE +
            "  <value>14</value>" + DEFAULT_NEW_LINE +
                "</AttrBean2>" + DEFAULT_NEW_LINE,
            xml2);
    }

    @Test
    public void testEmptyElem() throws Exception
    {
        PojoFor123 simple = new PojoFor123("foobar");
        String xml = _xmlMapper.writeValueAsString(simple);
        assertEquals("<PojoFor123 name=\"foobar\"/>" + DEFAULT_NEW_LINE,
            xml);
    }

    @Test
    public void testMultiLevel172() throws Exception
    {
        Company root = new Company();
        root.employee.add(new Employee("abc"));
        String xml = _xmlMapper.writer()
                .with(XmlWriteFeature.WRITE_XML_DECLARATION)
                .writeValueAsString(root);
        // unify possible apostrophes to quotes
        xml = a2q(xml);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + DEFAULT_NEW_LINE
                +"<Company>" + DEFAULT_NEW_LINE
                +"  <e>" + DEFAULT_NEW_LINE
                +"    <employee>" + DEFAULT_NEW_LINE
                +"      <id>abc</id>" + DEFAULT_NEW_LINE
                +"      <type>FULL_TIME</type>" + DEFAULT_NEW_LINE
                +"    </employee>" + DEFAULT_NEW_LINE
                +"  </e>" + DEFAULT_NEW_LINE
                +"</Company>" + DEFAULT_NEW_LINE,
                xml);
    }

    @Test
    public void testNewLine_withCustomNewLine() throws Exception {
        String customNewLine = "\n\rLF\n\r";
        DefaultXmlPrettyPrinter customXmlPrettyPrinter = new DefaultXmlPrettyPrinter().withCustomNewLine(customNewLine);

        Company root = new Company();
        root.employee.add(new Employee("abc"));

        String xml = _xmlMapper.writer()
            .with(customXmlPrettyPrinter)
            .with(XmlWriteFeature.WRITE_XML_DECLARATION)
            .writeValueAsString(root);
        // unify possible apostrophes to quotes
        xml = a2q(xml);

        // with indentation, should get newLines in prolog/epilog too
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + customNewLine
                + "<Company>" + customNewLine
                + "  <e>" + customNewLine
                + "    <employee>" + customNewLine
                + "      <id>abc</id>" + customNewLine
                + "      <type>FULL_TIME</type>" + customNewLine
                + "    </employee>" + customNewLine
                + "  </e>" + customNewLine
                + "</Company>" + customNewLine,
            xml);
    }

    @Test
    public void testNewLine_systemDefault() throws Exception {
        Company root = new Company();
        root.employee.add(new Employee("abc"));

        String xml = _xmlMapper.writer()
            .with(new DefaultXmlPrettyPrinter())
            .with(XmlWriteFeature.WRITE_XML_DECLARATION)
            .writeValueAsString(root);
        // unify possible apostrophes to quotes
        xml = a2q(xml);

        // with indentation, should get newLines in prolog/epilog too
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + DEFAULT_NEW_LINE
                + "<Company>" + DEFAULT_NEW_LINE
                + "  <e>" + DEFAULT_NEW_LINE
                + "    <employee>" + DEFAULT_NEW_LINE
                + "      <id>abc</id>" + DEFAULT_NEW_LINE
                + "      <type>FULL_TIME</type>" + DEFAULT_NEW_LINE
                + "    </employee>" + DEFAULT_NEW_LINE
                + "  </e>" + DEFAULT_NEW_LINE
                + "</Company>" + DEFAULT_NEW_LINE,
            xml);
    }

    @Test
    public void testNewLine_UseSystemDefaultLineSeperatorOnNullCustomNewLine() throws Exception {
        Company root = new Company();
        root.employee.add(new Employee("abc"));

        String xml = _xmlMapper.writer()
            .with(new DefaultXmlPrettyPrinter().withCustomNewLine(null))
            .with(XmlWriteFeature.WRITE_XML_DECLARATION)
            .writeValueAsString(root);
        // unify possible apostrophes to quotes
        xml = a2q(xml);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + DEFAULT_NEW_LINE
                + "<Company>" + DEFAULT_NEW_LINE
                + "  <e>" + DEFAULT_NEW_LINE
                + "    <employee>" + DEFAULT_NEW_LINE
                + "      <id>abc</id>" + DEFAULT_NEW_LINE
                + "      <type>FULL_TIME</type>" + DEFAULT_NEW_LINE
                + "    </employee>" + DEFAULT_NEW_LINE
                + "  </e>" + DEFAULT_NEW_LINE
                + "</Company>" + DEFAULT_NEW_LINE,
            xml);
    }
}
