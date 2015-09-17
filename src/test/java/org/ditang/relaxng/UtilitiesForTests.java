package org.ditang.relaxng;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class UtilitiesForTests implements ConstantsForTests {

    private static File testDirectory;

    public static File testDirectory() throws IOException {
        if (testDirectory == null) {
            ClassLoader cl = UtilitiesForTests.class.getClassLoader();
            URL res = cl.getResource("dita-ng.test.directory");
            if (res != null && res.getFile() != null) {
                File placeHolder = new File(res.getFile()).getCanonicalFile();
                testDirectory = placeHolder.getParentFile();
            }
        }
        if (testDirectory == null)
            throw new RuntimeException("The test directory was not found.");
        return testDirectory;
    }

    public static DOMResult identityTransformText(String xml)
        throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
        InputSource is = new InputSource(isr);
        is.setSystemId("urn:dita-ng:unit-test-source");
        SAXSource ss = new SAXSource(new SAXParser(), is);
        return identityTransform(ss);
    }

    public static DOMResult identityTransform(String systemId)
        throws Exception
    {
        return identityTransform(new File(systemId));
    }

    public static DOMResult identityTransform(File f)
        throws Exception
    {
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        InputSource is = new InputSource(isr);
        is.setSystemId(f.getCanonicalFile().toURI().toString());
        return identityTransform(is);
    }

    public static DOMResult identityTransform(InputSource is)
        throws Exception
    {
        SAXParser sp = new SAXParser();
        return identityTransform(new SAXSource(sp, is));
    }

    public static DOMResult identityTransform(SAXSource source)
        throws Exception
    {
        SAXTransformerFactory tf =
            (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        DOMResult result = new DOMResult();
        Transformer trans = tf.newTransformer();
        trans.transform(source, result);
        return result;
    }

    public static Node singleXPathNode(String expression, Node node)
        throws Exception
    {
        XPath xp = XPathFactory.newInstance().newXPath();
        return (Node) xp.evaluate(expression, node, XPathConstants.NODE);
    }
}
