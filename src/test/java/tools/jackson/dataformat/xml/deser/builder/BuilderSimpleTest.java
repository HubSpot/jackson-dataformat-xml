package tools.jackson.dataformat.xml.deser.builder;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.core.Version;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class BuilderSimpleTest extends XmlTestUtil
{
    // // Simple 2-property value class, builder with standard naming

    @JsonDeserialize(builder=SimpleBuilderXY.class)
    public static class ValueClassXY
    {
        final int _x, _y;

        protected ValueClassXY(int x, int y) {
            _x = x+1;
            _y = y+1;
        }
    }

    public static class SimpleBuilderXY
    {
        public int x, y;
    	
        public SimpleBuilderXY withX(int x0) {
    		    this.x = x0;
    		    return this;
        }

        public SimpleBuilderXY withY(int y0) {
    		    this.y = y0;
    		    return this;
        }

        public ValueClassXY build() {
    		    return new ValueClassXY(x, y);
        }
    }

    // // 3-property value, with more varied builder

    @JsonDeserialize(builder=BuildABC.class)
    public static class ValueClassABC
    {
        final int a, b, c;

        protected ValueClassABC(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @JsonIgnoreProperties({ "d" })
    public static class BuildABC
    {
        public int a; // to be used as is
        private int b, c;
    	
        @JsonProperty("b")
        public BuildABC assignB(int b0) {
            this.b = b0;
            return this;
        }

        // Also ok NOT to return 'this'
        @JsonSetter("c")
        public void c(int c0) {
            this.c = c0;
        }

        public ValueClassABC build() {
            return new ValueClassABC(a, b, c);
        }
    }

    // // Then Builder that is itself immutable
    
    @JsonDeserialize(builder=BuildImmutable.class)
    public static class ValueImmutable
    {
        final int value;
        protected ValueImmutable(int v) { value = v; }
    }
    
    public static class BuildImmutable {
        private final int value;

        public BuildImmutable() { this(0); }
        private BuildImmutable(int v) {
            value = v;
        }
        public BuildImmutable withValue(int v) {
            return new BuildImmutable(v);
        }
        public ValueImmutable build() {
            return new ValueImmutable(value);
        }
    }
    // And then with custom naming:

    @JsonDeserialize(builder=BuildFoo.class)
    public static class ValueFoo
    {
        final int value;
        public ValueFoo(int v) { value = v; }
    }

    @JsonPOJOBuilder(withPrefix="foo", buildMethodName="construct")
    public static class BuildFoo {
        private int value;
        
        public BuildFoo fooValue(int v) {
            value = v;
            return this;
        }
        public ValueFoo construct() {
            return new ValueFoo(value);
        }
    }


    // for [databind#761]

    @JsonDeserialize(builder=ValueInterfaceBuilder.class)
    public interface ValueInterface {
        int getX();
    }

    @JsonDeserialize(builder=ValueInterface2Builder.class)
    public interface ValueInterface2 {
        int getX();
    }
    
    public static class ValueInterfaceImpl implements ValueInterface
    {
        final int _x;

        public ValueInterfaceImpl(int x) {
            _x = x+1;
        }

        @Override
        public int getX() {
            return _x;
        }
    }

    public static class ValueInterface2Impl implements ValueInterface2
    {
        final int _x;

        public ValueInterface2Impl(int x) {
            _x = x+1;
        }

        @Override
        public int getX() {
            return _x;
        }
    }
    
    public static class ValueInterfaceBuilder
    {
        public int x;

        public ValueInterfaceBuilder withX(int x0) {
            this.x = x0;
            return this;
        }

        public ValueInterface build() {
            return new ValueInterfaceImpl(x);
        }
    }

    public static class ValueInterface2Builder
    {
        public int x;

        public ValueInterface2Builder withX(int x0) {
            this.x = x0;
            return this;
        }

        // should also be ok: more specific type
        public ValueInterface2Impl build() {
            return new ValueInterface2Impl(x);
        }
    }

    // [databind#777]
    @JsonDeserialize(builder = SelfBuilder777.class)
    @JsonPOJOBuilder(buildMethodName = "", withPrefix = "with")
    public static class SelfBuilder777 {
        public int x;

        public SelfBuilder777 withX(int value) {
            x = value;
            return this;
        }
    }

    // Won't work well with XML, omit
    // [databind#822]
    /*
    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    static class ValueBuilder822

    @JsonDeserialize(builder = ValueBuilder822.class)
    static class ValueClass822
    */

    protected static class NopModule1557 extends JacksonModule
    {
        @Override
        public String getModuleName() {
            return "NopModule";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext setupContext) {
            // This annotation introspector has no opinion about builders, make sure it doesn't interfere
            setupContext.insertAnnotationIntrospector(new NopAnnotationIntrospector() {
                private static final long serialVersionUID = 1L;
                @Override
                public Version version() {
                    return Version.unknownVersion();
                }
            });
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testSimple() throws Exception
    {
        String doc = "<ValueClassXY><x>1</x><y>2</y></ValueClassXY>";
        Object o = MAPPER.readValue(doc, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        // note: ctor adds one to both values
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }

    // related to [databind#1214]
    @Test
    public void testSimpleWithIgnores() throws Exception
    {
        // 'z' is unknown, and would fail by default:
        String doc = "<ValueClassXY><x>1</x><y>2</y><z>4</z></ValueClassXY>";
        Object o = null;

        try {
            o = MAPPER.readerFor(ValueClassXY.class)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(doc);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            assertEquals("z", e.getPropertyName());
            verifyException(e, "Unrecognized property \"z\"");
        }

        // but with config overrides should pass
        ObjectMapper ignorantMapper = mapperBuilder()
                .withConfigOverride(SimpleBuilderXY.class,
                        over -> over.setIgnorals(JsonIgnoreProperties.Value.forIgnoreUnknown(true)
                                ))
                .build();
        o = ignorantMapper.readValue(doc, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        // note: ctor adds one to both values
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }
    
    @Test
    public void testMultiAccess() throws Exception
    {
        String doc = "<ValueClassABC><c>3</c>  <a>2</a>  <b>-9</b></ValueClassABC>";
        ValueClassABC value = MAPPER.readValue(doc, ValueClassABC.class);
        assertNotNull(value);
        assertEquals(2, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);

        // also, since we can ignore some properties:
        value = MAPPER.readValue("<ValueClassABC><c>3</c>\n"
                +"<d>5</d><b>-9</b></ValueClassABC>",
                ValueClassABC.class);
        assertNotNull(value);
        assertEquals(0, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);
    }

    // test for Immutable builder, to ensure return value is used
    @Test
    public void testImmutable() throws Exception
    {
        ValueImmutable value = MAPPER.readValue("<ValueImmutable><value>13</value></ValueImmutable>",
                ValueImmutable.class);        
        assertEquals(13, value.value);
    }

    // test with custom 'with-prefix'
    @Test
    public void testCustomWith() throws Exception
    {
        ValueFoo value = MAPPER.readValue("<ValueFoo><value>1</value></ValueFoo>", ValueFoo.class);        
        assertEquals(1, value.value);
    }

    // for [databind#761]
    
    @Test
    public void testBuilderMethodReturnMoreGeneral() throws Exception
    {
        ValueInterface value = MAPPER.readValue("<ValueInterface><x>1</x></ValueInterface>", ValueInterface.class);
        assertEquals(2, value.getX());
    }

    @Test
    public void testBuilderMethodReturnMoreSpecific() throws Exception
    {
        final String doc = "<ValueInterface2><x>1</x></ValueInterface2>";
        ValueInterface2 value = MAPPER.readValue(doc, ValueInterface2.class);
        assertEquals(2, value.getX());
    }

    @Test
    public void testSelfBuilder777() throws Exception
    {
        SelfBuilder777 result = MAPPER.readValue("<SelfBuilder777><x>3</x></SelfBuilder777>",
                SelfBuilder777.class);
        assertNotNull(result);
        assertEquals(3, result.x);
    }

    // Won't work well with XML, omit:
//    public void testWithAnySetter822() throws Exception

    @Test
    public void testPOJOConfigResolution1557() throws Exception
    {
        ObjectMapper mapper = mapperBuilder()
                .addModule(new NopModule1557())
                .build();
        ValueFoo value = mapper.readValue("<ValueFoo><value>1</value></ValueFoo>", ValueFoo.class);
        assertEquals(1, value.value);
    }
}
