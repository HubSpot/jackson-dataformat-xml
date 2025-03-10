package tools.jackson.dataformat.xml.deser.convert;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.type.LogicalType;

import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

// 2020-12-18, tatu: Modified from "jackson-databind" version: XML
//   backend MUST NOT prevent coercion from String since XML has no
//   native boolean representation
public class CoerceToBooleanTest
    extends XmlTestUtil
{
    static class BooleanPOJO {
        public boolean value;

        public void setValue(boolean v) { value = v; }
    }

    private final ObjectMapper DEFAULT_MAPPER = newMapper();

    private final ObjectMapper MAPPER_STRING_TO_BOOLEAN_FAIL = mapperBuilder()
            .withCoercionConfig(LogicalType.Boolean, cfg ->
                cfg.setCoercion(CoercionInputShape.String, CoercionAction.Fail))
            .build();

    private final ObjectMapper MAPPER_EMPTY_TO_BOOLEAN_FAIL = mapperBuilder()
            .withCoercionConfig(LogicalType.Boolean, cfg ->
                cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail))
            .build();

    /*
    /**********************************************************
    /* Test methods: default, legacy configuration, from String
    /**********************************************************
     */

    // for [databind#403]
    @Test
    public void testEmptyStringFailForBooleanPrimitive() throws IOException
    {
        final ObjectReader reader = MAPPER_EMPTY_TO_BOOLEAN_FAIL
                .readerFor(BooleanPOJO.class);
        try {
            reader.readValue("<BooleanPOJO><value></value></BooleanPOJO>");
            fail("Expected failure for boolean + empty String");
        } catch (InvalidFormatException e) {
            verifyException(e, "Cannot coerce empty String");
            verifyException(e, "to `boolean` value");
        }
    }

    @Test
    public void testDefaultStringToBooleanCoercionOk() throws Exception {
        _verifyStringToBooleanOk(DEFAULT_MAPPER);
    }

    /*
    /**********************************************************
    /* Test methods: CoercionConfig, from String
    /**********************************************************
     */

    @Test
    public void testStringToBooleanOkDespiteCoercionConfig() throws Exception {
        _verifyStringToBooleanOk(MAPPER_STRING_TO_BOOLEAN_FAIL);
    }

    /*
    /**********************************************************
    /* Verification
    /**********************************************************
     */

    public void _verifyStringToBooleanOk(ObjectMapper mapper) throws Exception
    {
        // first successful coercions, basic types:
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "true"), Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "false"), Boolean.TYPE, Boolean.FALSE);

        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "true"), Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "false"), Boolean.class, Boolean.FALSE);

        // and then allowed variants:
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "True"), Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "True"), Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "TRUE"), Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "TRUE"), Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "False"), Boolean.TYPE, Boolean.FALSE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "False"), Boolean.class, Boolean.FALSE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("boolean", "FALSE"), Boolean.TYPE, Boolean.FALSE);
        _verifyCoerceSuccess(mapper, _xmlWrapped("Boolean", "FALSE"), Boolean.class, Boolean.FALSE);

        // and then Special boolean derivatives:
        // Alas, AtomicBoolean.equals() does not work so...
        final ObjectReader r = mapper.readerFor(AtomicBoolean.class);

        AtomicBoolean ab = r.readValue(_xmlWrapped("AtomicBoolean", "true"));
        assertTrue(ab.get());

        ab = r.readValue(_xmlWrapped("AtomicBoolean", "false"));
        assertFalse(ab.get());
    }

    /*
    /**********************************************************
    /* Other helper methods
    /**********************************************************
     */

    private String _xmlWrapped(String element, String value) {
        return String.format("<%s>%s</%s>", element, value, element);
    }

    private void _verifyCoerceSuccess(ObjectMapper mapper,
            String input, Class<?> type, Object exp) throws IOException
    {
        Object result = mapper.readerFor(type)
                .readValue(input);
        assertEquals(exp.getClass(), result.getClass());
        assertEquals(exp, result);
    }
}
