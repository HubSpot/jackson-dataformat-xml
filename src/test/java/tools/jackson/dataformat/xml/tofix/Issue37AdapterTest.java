package tools.jackson.dataformat.xml.tofix;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.*;

import tools.jackson.databind.*;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 30-Jun-2020, tatu: This is deferred and possibly won't be fixed
//   at all. But leaving failing test here just in case future brings
//   alternate approach to do something.
public class Issue37AdapterTest extends XmlTestUtil
{
    @XmlJavaTypeAdapter(URLEncoderMapDataAdapter.class)
    public static class MapData
    {
        public String key;
        public String value;

        public MapData() { }

        public MapData(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }
    }

    public static class URLEncoderMapDataAdapter extends XmlAdapter<MapData[], Map<String, String>>
    {
        public URLEncoderMapDataAdapter() { }

        @Override
        public MapData[] marshal(Map<String, String> arg0) throws Exception {
            MapData[] mapElements = new MapData[arg0.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : arg0.entrySet()) {
                mapElements[i++] = new MapData(encodeKey(entry.getKey()), entry.getValue());
            }

            return mapElements;
        }

        @Override
        public Map<String, String> unmarshal(MapData[] arg0) throws Exception {
            Map<String, String> r = new HashMap<String, String>();
            for (MapData mapelement : arg0) {
                r.put(decodeKey(mapelement.key), mapelement.value);
            }
            return r;
        }

        private final static String ENCODING = "UTF-8";

        private String encodeKey(String key) throws UnsupportedEncodingException {
            return URLEncoder.encode(key, ENCODING);
        }

        private String decodeKey(String key) throws UnsupportedEncodingException {
            return URLDecoder.decode(key, ENCODING);
        }
    }

    @XmlRootElement(name = "DocWithMapData")
    public static class DocWithMapData
    {
        @XmlJavaTypeAdapter(value = URLEncoderMapDataAdapter.class) // type = MapData[].class)
        public Map<String, String> mapDatas;
    }

    @XmlRootElement(name = "DocWithMapDataSimpleAnnotation")
    public static class DocWithMapDataSimpleAnnotation
    {
        @XmlJavaTypeAdapter(URLEncoderMapDataAdapter.class)
        public Map<String, String> mapDatas;
    }

    private Map<String, String> simpleMapData = singletonMap("key", "value");

    private Map<String, String> needEncodingMapData = singletonMap("my/key", "my/value");

    private Map<String,String> singletonMap(String a, String b) {
        Map<String,String> map = new HashMap<String,String>();
        map.put(a,b);
        return map;
    }

    /*
    /**********************************************************************
    /* Set up
    /***********************************************************************
     */

    protected XmlMapper _jaxbMapper;

    protected XmlMapper _nonJaxbMapper;

    @BeforeEach
    public void setUp() throws Exception
    {
        _jaxbMapper = new XmlMapper();
        _nonJaxbMapper = new XmlMapper();
        // Use JAXB-then-Jackson annotation introspector
        AnnotationIntrospector intr =
            XmlAnnotationIntrospector.Pair.instance(jakartaXMLBindAnnotationIntrospector(),
                new JacksonAnnotationIntrospector());
        _jaxbMapper = XmlMapper.builder()
                .annotationIntrospector(intr)
                .build();
    }

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    @JacksonTestFailureExpected
    @Test
    public void testSimpleKeyMap() throws Exception
    {
        DocWithMapData bean = new DocWithMapData();
        bean.mapDatas = simpleMapData;

        assertEquals("<DocWithMapData><mapDatas><key>value</key></mapDatas></DocWithMapData>",
            _jaxbMapper.writeValueAsString(bean));
    }

    @JacksonTestFailureExpected
    @Test
    public void testNeedEncodingKeyMap() throws Exception
    {
        DocWithMapData bean = new DocWithMapData();
        bean.mapDatas = needEncodingMapData;

        String xml = _jaxbMapper.writeValueAsString(bean);
        
        assertEquals("<DocWithMapData><mapDatas><my%2Fkey>my/value</my%2Fkey></mapDatas></DocWithMapData>",
                xml);
    }

    @JacksonTestFailureExpected
    @Test
    public void testSimpleKeyMapSimpleAnnotation() throws Exception
    {
        DocWithMapDataSimpleAnnotation bean = new DocWithMapDataSimpleAnnotation();
        bean.mapDatas = simpleMapData;

        assertEquals(
            "<DocWithMapDataSimpleAnnotation><mapDatas><key>value</key></mapDatas></DocWithMapDataSimpleAnnotation>",
            _jaxbMapper.writeValueAsString(bean));
    }

    @JacksonTestFailureExpected
    @Test
    public void testNeedEncodingKeyMapSimpleAnnotation() throws Exception
    {
        DocWithMapDataSimpleAnnotation bean = new DocWithMapDataSimpleAnnotation();
        bean.mapDatas = needEncodingMapData;

        assertEquals(
            "<DocWithMapDataSimpleAnnotation><mapDatas><my%2Fkey>my/value</my%2Fkey></mapDatas></DocWithMapDataSimpleAnnotation>",
            _jaxbMapper.writeValueAsString(bean));
    }

    @JacksonTestFailureExpected
    @Test
    public void testNeedEncodingKeyMap_nonJaxb() throws Exception
    {
        DocWithMapData bean = new DocWithMapData();
        bean.mapDatas = needEncodingMapData;

        assertEquals(
            "<DocWithMapData><mapDatas><my%2Fkey>my/value</my%2Fkey></mapDatas></DocWithMapData>",
            _nonJaxbMapper.writeValueAsString(bean));
    }

    @JacksonTestFailureExpected
    @Test
    public void testNeedEncodingKeyMapSimpleAnnotation_nonJaxb() throws Exception
    {
        DocWithMapDataSimpleAnnotation bean = new DocWithMapDataSimpleAnnotation();
        bean.mapDatas = needEncodingMapData;

        assertEquals(
            "<DocWithMapDataSimpleAnnotation><mapDatas><my%2Fkey>my/value</my%2Fkey></mapDatas></DocWithMapDataSimpleAnnotation>",
            _nonJaxbMapper.writeValueAsString(bean));
    }
}
