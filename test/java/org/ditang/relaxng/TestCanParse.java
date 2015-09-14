package org.ditang.relaxng;

import static org.ditang.relaxng.UtilitiesForTests.identityTransformText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.xml.transform.dom.DOMResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestCanParse {

    @Test
    public void aDocumentCanBeParsed() {
        try {
            DOMResult dr = identityTransformText("<doc/>");
            Document doc = (Document) dr.getNode();
            assertNotNull(doc);
            Element root = doc.getDocumentElement();
            assertNotNull(root);
            assertEquals("doc", root.getLocalName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
