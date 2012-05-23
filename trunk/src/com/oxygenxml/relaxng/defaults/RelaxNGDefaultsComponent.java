package com.oxygenxml.relaxng.defaults;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * XNI component that adds Relax NG default values.
 * 
 * @author george@oxygenxml.com
 */
public class RelaxNGDefaultsComponent implements XMLDocumentHandler,
    XMLComponent, XMLDocumentSource {
  

  /** Property identifier: entity resolver. */
  public static final String ENTITY_RESOLVER = Constants.XERCES_PROPERTY_PREFIX
      + Constants.ENTITY_RESOLVER_PROPERTY;

  /** Property identifier: error reporter. */
  private static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX
      + Constants.ERROR_REPORTER_PROPERTY;

  /** Property identifier: error reporter. */
  private static final String ERROR_HANDLER = Constants.XERCES_PROPERTY_PREFIX
      + Constants.ERROR_HANDLER_PROPERTY;
  
  /** Property identifier: symbol table. */
  private static final String SYMBOL_TABLE = Constants.XERCES_PROPERTY_PREFIX
      + Constants.SYMBOL_TABLE_PROPERTY;

  
  // recognized features and properties

  /** Recognized features. */
  private static final String[] RECOGNIZED_FEATURES = {
  };

  /** Feature defaults. */
  private static final Boolean[] FEATURE_DEFAULTS = {
  };

  /** Recognized properties. */
  private static final String[] RECOGNIZED_PROPERTIES = {
      SYMBOL_TABLE,       
      ERROR_REPORTER,
      ERROR_HANDLER,
      ENTITY_RESOLVER,
  };

  /** Property defaults. */
  private static final Object[] PROPERTY_DEFAULTS = {
      null,
      null,
      null,
      null
  };
    
  private XMLDocumentHandler documentHandler;
  private XMLDocumentSource documentSource;

  private boolean detecting = false;
  private String schema = null;
  private String type = null;
  private String baseSystemId = null;
  
  private RelaxNGDefaultValues defaults;
  private NamespaceContext context;
  
  private SymbolTable fSymbolTable;
  private XMLEntityResolver fResolver;
  private XMLErrorReporter fErrorReporter;
  private XMLErrorHandler fErrorHandler;

  /**
   * Constructor.
   */
  public RelaxNGDefaultsComponent() {
  }


  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#reset(org.apache.xerces.xni.parser.XMLComponentManager)
   */
  public void reset(XMLComponentManager componentManager)
      throws XMLConfigurationException {
    baseSystemId = null;
    detecting = false;
    schema = null;
    type = null;
    context = null;

    fSymbolTable = (SymbolTable) componentManager.getProperty(SYMBOL_TABLE);
    fErrorReporter = (XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER);
    fResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_RESOLVER);
    fErrorHandler = (XMLErrorHandler) componentManager.getProperty(ERROR_HANDLER);
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(org.apache.xerces.xni.XMLLocator,
   *      java.lang.String, org.apache.xerces.xni.NamespaceContext,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void startDocument(XMLLocator locator, String enc,
      NamespaceContext nc, Augmentations aug) throws XNIException {
    context = nc;
    baseSystemId = locator.getBaseSystemId();
    detecting = true;
    schema = null;
    type = null;
    if (documentHandler != null) {
      documentHandler.startDocument(locator, enc, nc, aug);
    }
  }
  
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.XMLAttributes,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void startElement(QName name, XMLAttributes atts, Augmentations aug)
      throws XNIException {
    onStartElement(name, atts);
    if (documentHandler != null) {
      documentHandler.startElement(name, atts, aug);
    }
  }

  /**
   * @param name
   * @param atts
   */
  private void onStartElement(QName name, XMLAttributes atts) {
    if (detecting) {
      detecting = false;
      loadDefaults();
    }
    if (defaults != null) {
      checkAndAddDefaults(name, atts);
    }
  }

  /**
   * 
   */
  private void loadDefaults() {
    defaults = null;
    ErrorHandler eh = new ErrorHandler() {
      @Override
      public void warning(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
      }
    };
    
    if (schema != null && "xml".equals(type)) {
      defaults = new RNGDefaultValues(null, eh);
      // TODO: Pass a resolver.
    }
    if (schema != null && "compact".equals(type)) {
      // TODO: Pass a resolver
      defaults = new RNCDefaultValues(null, eh);
    }
    if (defaults != null) {
        // Tries to obtain an expanded system ID.
        String expanded = schema;
        try {
          expanded  = new URL(new URL(baseSystemId), schema).toString();
        } catch (Exception e) {
        };
        XMLInputSource r = resolveSchema(new XMLResourceIdentifierImpl(null, schema, baseSystemId, expanded));
        if (r == null) {
          r = resolveSchema(new XMLResourceIdentifierImpl(null, schema, baseSystemId, schema));
        }        
        if (r!=null) {
          InputSource in = new InputSource(r.getSystemId());
          in.setByteStream(r.getByteStream());
          in.setCharacterStream(r.getCharacterStream());
          in.setEncoding(r.getEncoding());
          in.setPublicId(r.getPublicId());
          try {
            defaults.update(in);
          } catch (Exception e) {
          }
        } else {          
          InputSource in = new InputSource(expanded);
          try {
            defaults.update(in);
          } catch (Exception e) {
          }
        }
    }
  }

  /**
   * Try to resolve the specified schema location with the entity resolver.
   * 
   * @param schemaLocation
   *          The specified schema location.
   * 
   * @return The new schema location if the specified location can be resolved
   *         with the entity or <code>null</code> otherwise.
   * 
   * @throws MalformedURLException
   *           if the location specified by the entity resolver is not a valid
   *           URL.
   */
  private XMLInputSource resolveSchema(XMLResourceIdentifier resource) {
    XMLInputSource is = null;
    if (fResolver != null) {      
      try {
        is = fResolver.resolveEntity(resource);
      } catch (XNIException e) {
      } catch (IOException e) {
      }
    }
    return is;
  }

  /**
   * @param name
   * @param atts
   */
  private void checkAndAddDefaults(QName name, XMLAttributes atts) {
    List<RelaxNGDefaultValues.Attribute> def = defaults.getDefaultAttributes(
        name.localpart, name.uri);
    if (def != null) {
      for (RelaxNGDefaultValues.Attribute a : def) {
        //EXM-24143 it is possible that the namespace of the default attribute is empty
        //and the namespace of the attribute declared in the XMLAttributes is NULL.
        boolean alreadyDeclared = false;
        alreadyDeclared = atts.getIndex(a.namespace, a.localName) >= 0;
        if(! alreadyDeclared) {
          if("".equals(a.namespace)) {
            //Extra check with NULL Namespace
            alreadyDeclared = atts.getIndex(null, a.localName) >= 0;    
          }
        }
        
        if (! alreadyDeclared) {
          String prefix = null;
          String rawname = a.localName;
          if (a.namespace != null && a.namespace.length() > 0) {
            prefix = context.getPrefix(a.namespace);
            if (prefix == null) {
              for (int i = 0; i < atts.getLength(); i++) {
                String attname = atts.getQName(i);
                if (attname.startsWith("xmlns:")) {
                  if (a.namespace.equals(atts.getValue(i))) {
                    prefix = attname.substring(6);
                  }
                }
              }
            }

            if (prefix != null && prefix.length() > 0) {
              rawname = prefix + ":" + a.localName;
              // double check in case of no namespace aware parsers.
              // if we want to fully handle this case we may need further
              // processing.
              if (atts.getIndex(rawname) < 0) {
                QName attName = new QName(fSymbolTable.addSymbol(prefix),
                    fSymbolTable.addSymbol(a.localName), fSymbolTable.addSymbol(rawname),
                    fSymbolTable.addSymbol(a.namespace));
                atts.addAttribute(attName, "CDATA", a.value);
                int attrIndex = atts.getIndex(attName.uri, attName.localpart);
                atts.setSpecified(attrIndex, false);
              }
            } else {
              int k = 1;
              prefix = "ns" + k;
              while (context.getURI(prefix) != null
                  || atts.getValue("xmlns:" + prefix) != null) {
                k++;
                prefix = "ns" + k;
              }
              rawname = prefix + ":" + a.localName;

              QName attNs = new QName(fSymbolTable.addSymbol("xmlns"),
                  fSymbolTable.addSymbol(prefix), fSymbolTable.addSymbol("xmlns:" + prefix),
                  fSymbolTable.addSymbol("http://www.w3.org/2000/xmlns/"));
              atts.addAttribute(attNs, "CDATA", a.namespace);

              QName attName = new QName(fSymbolTable.addSymbol(prefix),
                  fSymbolTable.addSymbol(a.localName), fSymbolTable.addSymbol(rawname),
                  fSymbolTable.addSymbol(a.namespace));
              atts.addAttribute(attName, "CDATA", a.value);
              int attrIndex = atts.getIndex(attName.uri, attName.localpart);
              atts.setSpecified(attrIndex, false);
            }
          } else {
            String attname = fSymbolTable.addSymbol(a.localName);
            QName attName = new QName(null, attname, attname, null);
            atts.addAttribute(attName, "CDATA", a.value);
            int attrIndex = atts.getIndex(attname);
            atts.setSpecified(attrIndex, false);
          }
        }
      }
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(java.lang.String,
   *      java.lang.String, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void xmlDecl(String arg0, String arg1, String arg2, Augmentations arg3)
      throws XNIException {
    if (documentHandler != null) {
      documentHandler.xmlDecl(arg0, arg1, arg2, arg3);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String,
   *      java.lang.String, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void doctypeDecl(String arg0, String arg1, String arg2,
      Augmentations arg3) throws XNIException {
    if (documentHandler != null) {
      documentHandler.doctypeDecl(arg0, arg1, arg2, arg3);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#comment(org.apache.xerces.xni.XMLString,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void comment(XMLString arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.comment(arg0, arg1);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#processingInstruction(java.lang.String,
   *      org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
   */
  public void processingInstruction(String name, XMLString content,
      Augmentations arg2) throws XNIException {
    if (detecting && schema == null && "oxygen".equals(name)) {
      String data = content.toString();
      schema = getFromPIDataPseudoAttribute(data, "RNGSchema", true);
      type = getFromPIDataPseudoAttribute(data, "type", true);
    }

    if (detecting && schema == null && "xml-model".equals(name)) {
      String data = content.toString();
      schema = getFromPIDataPseudoAttribute(data, "href", true);
      type = getFromPIDataPseudoAttribute(data, "type", true);
      String schemaTypeNs = getFromPIDataPseudoAttribute(data, "schematypens",
          true);
      if (schema != null) {
        if (schema.toLowerCase().endsWith(".rng")) {
          if (nullOrValue(schemaTypeNs, "http://relaxng.org/ns/structure/1.0")
              && nullOrValue(type, "application/xml")) {
            type = "xml";
          } else {
            schema = null;
          }
        } else if (schema.toLowerCase().endsWith(".rnc")) {
          if (nullOrValue(schemaTypeNs, "http://relaxng.org/ns/structure/1.0")
              && nullOrValue(type, "application/relax-ng-compact-syntax")) {
            type = "compact";
          } else {
            schema = null;
          }
        } else {
          if ("http://relaxng.org/ns/structure/1.0".equals(schemaTypeNs)
              && nullOrValue(type, "application/xml")) {
            type = "xml";
          } else if ("application/relax-ng-compact-syntax".equals(type)
              && nullOrValue(schemaTypeNs,
                  "http://relaxng.org/ns/structure/1.0")) {
            type = "compact";
          } else {
            schema = null;
          }
        }
      }
    }

    if (documentHandler != null) {
      documentHandler.processingInstruction(name, content, arg2);
    }
  }

  boolean nullOrValue(String test, String value) {
    if (test == null)
      return true;
    if (test.equals(value))
      return true;
    return false;
  }

  /**
   * This method is copied from com.icl.saxon.ProcInstParser and used in the
   * PIFinder.
   * 
   * Get a pseudo-attribute. This is useful only if the processing instruction
   * data part uses pseudo-attribute syntax, which it does not have to. This
   * syntax is as described in the W3C Recommendation
   * "Associating Style Sheets with XML Documents".
   * 
   * @param data
   *          The PI Data.
   * @param name
   *          The attr name.
   * @param unescapeValue
   *          True to unescape value before return
   * @return the value of the pseudo-attribute if present, or null if not
   */
  private String getFromPIDataPseudoAttribute(String data, String name,
      boolean unescapeValue) {
    int pos = 0;
    while (pos <= data.length() - 4) { // minimum length of x=""
      int nextQuote = -1;
      for (int q = pos; q < data.length(); q++) {
        if (data.charAt(q) == '"' || data.charAt(q) == '\'') {
          nextQuote = q;
          break;
        }
      }
      if (nextQuote < 0) {
        return null;
        // if (nextQuote+1 == name.length()) return null;
      }

      int closingQuote = data.indexOf(data.charAt(nextQuote), nextQuote + 1);
      if (closingQuote < 0) {
        return null;
      }
      int nextName = data.indexOf(name, pos);
      if (nextName < 0) {
        return null;
      }
      if (nextName < nextQuote) {
        // check only spaces and equal signs between the name and the quote
        boolean found = true;
        for (int s = nextName + name.length(); s < nextQuote; s++) {
          char c = data.charAt(s);
          if (!Character.isWhitespace(c) && c != '=') {
            found = false;
            break;
          }
        }
        if (found) {
          String val = data.substring(nextQuote + 1, closingQuote);
          return unescapeValue ? unescape(val) : val;
        }
      }
      pos = closingQuote + 1;
    }
    return null;
  }

  /**
   * This method is copied from com.icl.saxon.ProcInstParser and used in the
   * PIFinder
   * 
   * Interpret character references and built-in entity references
   */
  private String unescape(String value) {
    if (value.indexOf('&') < 0) {
      return value;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '&') {
        if (i + 2 < value.length() && value.charAt(i + 1) == '#') {
          if (value.charAt(i + 2) == 'x') {
            int x = i + 3;
            int charval = 0;
            while (x < value.length() && value.charAt(x) != ';') {
              int digit = "0123456789abcdef".indexOf(value.charAt(x));
              if (digit < 0) {
                digit = "0123456789ABCDEF".indexOf(value.charAt(x));
              }
              if (digit < 0) {
                return null;
              }
              charval = charval * 16 + digit;
              x++;
            }
            char hexchar = (char) charval;
            sb.append(hexchar);
            i = x;
          } else {
            int x = i + 2;
            int charval = 0;
            while (x < value.length() && value.charAt(x) != ';') {
              int digit = "0123456789".indexOf(value.charAt(x));
              if (digit < 0) {
                return null;
              }
              charval = charval * 10 + digit;
              x++;
            }
            char decchar = (char) charval;
            sb.append(decchar);
            i = x;
          }
        } else if (value.substring(i + 1).startsWith("lt;")) {
          sb.append('<');
          i += 3;
        } else if (value.substring(i + 1).startsWith("gt;")) {
          sb.append('>');
          i += 3;
        } else if (value.substring(i + 1).startsWith("amp;")) {
          sb.append('&');
          i += 4;
        } else if (value.substring(i + 1).startsWith("quot;")) {
          sb.append('"');
          i += 5;
        } else if (value.substring(i + 1).startsWith("apos;")) {
          sb.append('\'');
          i += 5;
        } else {
          return null;
        }

      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.XMLAttributes,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void emptyElement(QName name, XMLAttributes atts, Augmentations arg2)
      throws XNIException {
    onStartElement(name, atts);
    if (documentHandler != null) {
      documentHandler.emptyElement(name, atts, arg2);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startGeneralEntity(java.lang.String,
   *      org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void startGeneralEntity(String arg0, XMLResourceIdentifier arg1,
      String arg2, Augmentations arg3) throws XNIException {
    if (documentHandler != null) {
      documentHandler.startGeneralEntity(arg0, arg1, arg2, arg3);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#textDecl(java.lang.String,
   *      java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void textDecl(String arg0, String arg1, Augmentations arg2)
      throws XNIException {
    if (documentHandler != null) {
      documentHandler.textDecl(arg0, arg1, arg2);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endGeneralEntity(java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void endGeneralEntity(String arg0, Augmentations arg1)
      throws XNIException {
    if (documentHandler != null) {
      documentHandler.endGeneralEntity(arg0, arg1);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#characters(org.apache.xerces.xni.XMLString,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void characters(XMLString arg0, Augmentations arg1)
      throws XNIException {
    if (documentHandler != null) {
      documentHandler.characters(arg0, arg1);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#ignorableWhitespace(org.apache.xerces.xni.XMLString,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void ignorableWhitespace(XMLString arg0, Augmentations arg1)
      throws XNIException {
    if (documentHandler != null) {
      documentHandler.ignorableWhitespace(arg0, arg1);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void endElement(QName arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.endElement(arg0, arg1);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startCDATA(org.apache.xerces.xni.Augmentations)
   */
  public void startCDATA(Augmentations arg0) throws XNIException {
    if (documentHandler != null) {
      documentHandler.startCDATA(arg0);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endCDATA(org.apache.xerces.xni.Augmentations)
   */
  public void endCDATA(Augmentations arg0) throws XNIException {
    if (documentHandler != null) {
      documentHandler.endCDATA(arg0);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(org.apache.xerces.xni.Augmentations)
   */
  public void endDocument(Augmentations arg0) throws XNIException {
    if (documentHandler != null) {
      documentHandler.endDocument(arg0);
    }
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#setDocumentSource(org.apache.xerces.xni.parser.XMLDocumentSource)
   */
  public void setDocumentSource(XMLDocumentSource arg0) {
    this.documentSource = arg0;
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#getDocumentSource()
   */
  public XMLDocumentSource getDocumentSource() {
    return documentSource;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getRecognizedFeatures()
   */
  public String[] getRecognizedFeatures() {
    return (String[])(RECOGNIZED_FEATURES.clone());
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#setFeature(java.lang.String,
   *      boolean)
   */
  public void setFeature(String arg0, boolean arg1)
      throws XMLConfigurationException {
    // Do nothing
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getRecognizedProperties()
   */
  public String[] getRecognizedProperties() {
    return (String[])(RECOGNIZED_PROPERTIES.clone());
  }

  /**
   * Sets the value of a property during parsing.
   * 
   * @param propertyId
   * @param value
   */
  public void setProperty(String propertyId, Object value)
      throws XMLConfigurationException {

    // Xerces properties
    if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
      final int suffixLength = propertyId.length()
          - Constants.XERCES_PROPERTY_PREFIX.length();

      if (suffixLength == Constants.SYMBOL_TABLE_PROPERTY.length()
          && propertyId.endsWith(Constants.SYMBOL_TABLE_PROPERTY)) {
        fSymbolTable = (SymbolTable) value;
      } else if (suffixLength == Constants.ERROR_REPORTER_PROPERTY.length()
          && propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
        fErrorReporter = (XMLErrorReporter) value;
      } else if (suffixLength == Constants.ERROR_HANDLER_PROPERTY.length()
          && propertyId.endsWith(Constants.ERROR_HANDLER_PROPERTY)) {
        fErrorHandler = (XMLErrorHandler) value;
      } else if (suffixLength == Constants.ENTITY_RESOLVER_PROPERTY.length()
          && propertyId.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
        fResolver = (XMLEntityResolver) value;
      }
    }
  } // setProperty(String,Object)

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getFeatureDefault(java.lang.String)
   */
  public Boolean getFeatureDefault(String featureId) {
    for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
      if (RECOGNIZED_FEATURES[i].equals(featureId)) {
        return FEATURE_DEFAULTS[i];
      }
    }
    return null;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getPropertyDefault(java.lang.String)
   */
  public Object getPropertyDefault(String propertyId) {
    for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
      if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
        return PROPERTY_DEFAULTS[i];
      }
    }
    return null;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLDocumentSource#setDocumentHandler(org.apache.xerces.xni.XMLDocumentHandler)
   */
  public void setDocumentHandler(XMLDocumentHandler handler) {
    this.documentHandler = handler;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLDocumentSource#getDocumentHandler()
   */
  public XMLDocumentHandler getDocumentHandler() {
    return documentHandler;
  }
}
