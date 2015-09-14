package org.ditang.relaxng;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An error handler that inserts location information into the message.
 */
public class LocatingErrorHandler implements ErrorHandler {

    /**
     * Constructs a new error handler.
     */
    public LocatingErrorHandler() { }

    /** @inheritDoc */
    @Override
    public void warning(SAXParseException e) throws SAXException {
    }

    /** @inheritDoc */
    @Override
    public void error(SAXParseException e) throws SAXException {
        throw new SAXException(message(e));
    }

    /** @inheritDoc */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw new SAXException(message(e));
    }

    private String message(SAXParseException e) {
        String msgText = e.getMessage();
        StringBuilder msg = new StringBuilder();
        int line = e.getLineNumber();
        if (line > 0) {
            int col = e.getColumnNumber();
            if (col > 0)
                msg.append(String.format("[%d:%d] ", line, col));
            else
                msg.append(String.format("[%d] ", line));
            msg.append(msgText);
        }
        return msg.toString();
    }
}
