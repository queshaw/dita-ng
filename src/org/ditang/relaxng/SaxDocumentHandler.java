package org.ditang.relaxng;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * An XML document handler that sends events to a SAX content handler.
 */
public class SaxDocumentHandler implements XMLDocumentHandler {

    private ContentHandler contentHandler;

    private XMLDocumentSource documentSource;

    private XMLDocumentHandler previous;

    private XMLLocator locator;

    /**
     * Creates a new instance of the document handler.
     */
    public SaxDocumentHandler() {
        this(null);
    }

    /**
     * Creates a new instance of the document handler.
     *
     * @param source the document source.
     */
    public SaxDocumentHandler(XMLDocumentSource source) {
        setDocumentSource(source);  
    }

    /** @inheritDoc */
    @Override
    public void setDocumentSource(XMLDocumentSource source) {
        documentSource = source;
        if (source != null) {
            previous = source.getDocumentHandler();
            source.setDocumentHandler(this);
        }
    }

    /** @inheritDoc */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return documentSource;
    }

    /**
     * Returns the target content handler.
     *
     * @return the content handler.
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Sets the target content handler.
     *
     * @param contentHandler the content handler.
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * Returns the XNI locator.
     *
     * @return the locator.
     */
    protected XMLLocator getLocator() {
        return locator;
    }

    /**
     * Sets the XNI locator.
     *
     * @param the locator.
     */
    protected void setLocator(XMLLocator locator) {
        this.locator = locator;
    }

    /** @inheritDoc */
    @Override
    public void xmlDecl(String version, String encoding, String standalone,
                        Augmentations augs) throws XNIException {
        if (previous != null)
            previous.xmlDecl(version, encoding, standalone, augs);
    }

    /** @inheritDoc */
    @Override
    public void processingInstruction(String target, XMLString data,
                                      Augmentations augs) throws XNIException {
        if (previous != null)
            previous.processingInstruction(target, data, augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.processingInstruction(target, data.toString());
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void doctypeDecl(String rootElement, String publicId,
                            String systemId, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.doctypeDecl(rootElement, publicId, systemId, augs);
    }

    /** @inheritDoc */
    @Override
    public void comment(XMLString text, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.comment(text, augs);
    }

    /** @inheritDoc */
    @Override
    public void startDocument(XMLLocator locator, String encoding,
                              NamespaceContext namespaceContext,
                              Augmentations augs)
        throws XNIException
    {
        setLocator(locator);
        if (previous != null) {
            previous.startDocument(locator, encoding,
                                   namespaceContext, augs);
        }
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.startDocument();
            } catch (SAXException e) {
                throw new XMLParseException(locator, "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void endDocument(Augmentations augs) throws XNIException {
        if (previous != null)
            previous.endDocument(augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.endDocument();
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void startElement(QName element, XMLAttributes attributes,
                             Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.startElement(element, attributes, augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.startElement(notNull(element.uri),
                                element.localpart,
                                prefixed(element), saxAttributes(attributes));
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void endElement(QName element, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.endElement(element, augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.endElement(notNull(element.uri), element.localpart,
                              prefixed(element));
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }

    }

    /** @inheritDoc */
    @Override
    public void emptyElement(QName element, XMLAttributes attributes,
                             Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.emptyElement(element, attributes, augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.startElement(notNull(element.uri), element.localpart,
                                prefixed(element),
                                saxAttributes(attributes));
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding, Augmentations augs)
            throws XNIException
    {
        if (previous != null)
            previous.startGeneralEntity(name, identifier, encoding, augs);
    }

    /** @inheritDoc */
    @Override
    public void textDecl(String version, String encoding, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.textDecl(version, encoding, augs);
    }

    /** @inheritDoc */
    @Override
    public void endGeneralEntity(String name, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.endGeneralEntity(name, augs);
    }

    /** @inheritDoc */
    @Override
    public void characters(XMLString text, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.characters(text, augs);
        ContentHandler ch = getContentHandler();
        try {
            ch.characters(text.ch, text.offset, text.length);
        } catch (SAXException e) {
            throw new XMLParseException(getLocator(), "", e);
        }
    }

    /** @inheritDoc */
    @Override
    public void ignorableWhitespace(XMLString text, Augmentations augs)
        throws XNIException
    {
        if (previous != null)
            previous.ignorableWhitespace(text, augs);
        ContentHandler ch = getContentHandler();
        if (ch != null) {
            try {
                ch.ignorableWhitespace(text.ch, text.offset, text.length);
            } catch (SAXException e) {
                throw new XMLParseException(getLocator(), "", e);
            }
        }
    }

    /** @inheritDoc */
    @Override
    public void startCDATA(Augmentations augs) throws XNIException {
        if (previous != null)
            previous.startCDATA(augs);
    }

    /** @inheritDoc */
    @Override
    public void endCDATA(Augmentations augs) throws XNIException {
        if (previous != null)
            previous.endCDATA(augs);
    }

    private Attributes saxAttributes(XMLAttributes attributes) {
        AttributesImpl atts = new AttributesImpl();
        for (int i = 0, e = attributes.getLength(); i < e; ++i) {
            atts.addAttribute(notNull(attributes.getURI(i)),
                    attributes.getLocalName(i),
                    notNull(attributes.getQName(i)),
                    attributes.getType(i),
                    attributes.getValue(i));
        }
        return atts;
    }

    private String notNull(String s) {
        return s == null ? "" : s;
    }

    private String prefixed(QName q) {
        String prefix = notNull(q.prefix);
        return "".equals(prefix) ? q.localpart
                                 : String.format("%s:%s", prefix, q.localpart);
    }
}
