package org.ditang.relaxng.defaults;

import static org.ditang.relaxng.UtilitiesForTests.identityTransform;
import static org.ditang.relaxng.UtilitiesForTests.singleXPathNode;
import static org.ditang.relaxng.UtilitiesForTests.testDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestParserConfiguration extends ParserConfigurationMixin {

    @Test
    public void aDocumentCanBeParsedRng() throws Exception {
        parseFile("relaxng/no-defaults.rng.xml");
    }

    @Test
    public void aDocumentCanBeParsedRnc() throws Exception {
        parseFile("relaxng/no-defaults.rnc.xml");
    }

    @Test(expected=TransformerException.class)
    public void aDocumentCanBeValidatedRng() throws Exception {
        System.setProperty(VALIDATION_PROPERTY, "true");
        parseFile("relaxng/no-defaults-invalid.rng.xml");
        System.clearProperty(VALIDATION_PROPERTY);
    }

    @Test(expected=TransformerException.class)
    public void aDocumentCanBeValidatedRnc() throws Exception {
        System.setProperty(VALIDATION_PROPERTY, "true");
        parseFile("relaxng/no-defaults-invalid.rnc.xml");
        System.clearProperty(VALIDATION_PROPERTY);
    }

    @Test
    public void defaultsCanBeAddedRng() throws Exception {
        searchFile("/*[@some='value']", "relaxng/defaults.rng.xml");
    }

    @Test
    public void defaultsCanBeAddedRnc() throws Exception {
        searchFile("/*[@some='value']", "relaxng/defaults.rnc.xml");
    }

    private DOMResult parseFile(String name) throws Exception {
        File testDir = testDirectory();
        File noDefaultsRng = new File(testDir, name);
        DOMResult result = identityTransform(noDefaultsRng);
        assertNotNull(result);
        Document dom = (Document) result.getNode();
        assertNotNull(dom);
        Element document = dom.getDocumentElement();
        assertNotNull(document);
        String localName = document.getLocalName();
        assertEquals(localName, "doc");
        return result;
    }

    private Node searchFile(String expression, String name) throws Exception {
        DOMResult result = parseFile(name);
        return singleXPathNode(expression, result.getNode());
    }
}
