package org.ditang.relaxng.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.ditang.relaxng.SaxDocumentHandler;
import org.ditang.relaxng.defaults.OxygenRelaxNGSchemaReader.SchemaWrapper;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.relaxng.pattern.DefaultValuesExtractor;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.relaxng.sax.PatternValidator;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.xml.sax.DraconianErrorHandler;

/**
 * Relax NG default values gatherer.
 * 
 * @author george@oxygenxml.com
 */
public abstract class RelaxNGDefaultValues implements DefaultsConstants {
  /**
   * Error handler
   */
  private ErrorHandler eh;
  /**
   * Resolver
   */
  private final Resolver resolver;
  
  /**
   * XNI document source.
   */
  private XMLDocumentSource documentSource;

  /**
   * Flag indicating whether or not validation should be performed.
   */
  private Boolean validating = null;

  /**
   * Constructor.
   * @param resolver The resolver
   * @param eh The error handler
   */
  public RelaxNGDefaultValues(Resolver resolver, ErrorHandler eh) {
    this.resolver = resolver;
    this.eh = eh;
  }

  /**
   * Returns the XNI document source.
   *
   * @return the document source.
   */
  public XMLDocumentSource getDocumentSource() {
    return documentSource;
  }

  /**
   * Sets the XNI document source.
   *
   * @param source the document source.
   */
  public void setDocumentSource(XMLDocumentSource source) {
    this.documentSource = source;
  }

  /**
   * Determines whether the XML parser validates documents against the schema.
   *
   * @return true if the parser is validating; false otherwise.
   */
  public boolean isValidating() {
      if (validating == null) {
          String validatingProperty =
              System.getProperty(VALIDATION_PROPERTY, "false");
          if ("yes".equalsIgnoreCase(validatingProperty))
              validatingProperty = "true";
          setValidating(Boolean.valueOf(validatingProperty));
      }
    return validating;
  }

  /**
   * Sets whether or not the XML parser validates documents against the schema.
   *
   * @param validating true if the parser should validate documents; false
   * otherwise.
   */
  public void setValidating(boolean validating) {
    this.validating = validating;
  }

  /**
   * @return a schema reader. Can be an XML or compact syntax schema reader.
   */
  protected abstract SchemaReader getSchemaReader();

  /**
   * Stores collected values.
   */
  private DefaultValuesCollector defaultValuesCollector = null;

  /**
   * Collects default values. Listener for the default values extractor.
   */
  class DefaultValuesCollector implements
      DefaultValuesExtractor.DefaultValuesListener {
    /**
     * Stores the default attributes as a hash map with the element info as key.
     */
    private HashMap<String, List<Attribute>> defaults = new HashMap<String, List<Attribute>>();

    /**
     * Constructor.
     * 
     * @param start
     *          The Relax NG schema pattern.
     */
    public DefaultValuesCollector(Pattern start) {
      new DefaultValuesExtractor(this).parsePattern(start);
    }

    /**
     * Get a key for an element.
     * 
     * @param elementLocalName
     * @param elementNamespace
     * @return A string formed from the element local name and its namespace.
     */
    private String getKey(String elementLocalName, String elementNamespace) {
      return elementLocalName + "#"
          + (elementNamespace == null ? "" : elementNamespace);
    }

    /**
     * Get the default attributes for an element.
     * 
     * @param elementLocalName
     *          The element local name.
     * @param elementNamespace
     *          The element namespace. Use null or empty for no namespace.
     * @return A list of Attribute objects or null if no defaults.
     */
    List<Attribute> getDefaultAttributes(String elementLocalName,
        String elementNamespace) {
      return defaults.get(getKey(elementLocalName, elementNamespace));
    }

    /**
     * Default attribute notification.
     * 
     * @see com.thaiopensource.relaxng.pattern.DefaultValuesExtractor.DefaultValuesListener#defaultValue(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void defaultValue(String elementLocalName, String elementNamespace,
        String attributeLocalName, String attributeNamepsace, String value) {
      String key = getKey(elementLocalName, elementNamespace);
      List<Attribute> list = defaults.get(key);
      if (list == null) {
        list = new ArrayList<Attribute>();
        defaults.put(key, list);
      }
      list.add(new Attribute(attributeLocalName, attributeNamepsace, value));
    }
  }

  /**
   * Stores information about a default attribute.
   */
  class Attribute {
    /** The attribute local name */
    String localName;
    /** The attribute namespace */
    String namespace;
    /** The attribute default value */
    String value;

    /**
     * 
     * @param localName
     * @param namespace
     * @param value
     */
    public Attribute(String localName, String namespace, String value) {
      this.localName = localName;
      this.namespace = namespace;
      this.value = value;
    }
  }

  /**
   * Updates the annotation model.
   * 
   * @param in
   *          The schema input source.
   * @throws SAXException 
   */
  public void update(InputSource in) throws SAXException {
    defaultValuesCollector = null;
    PropertyMapBuilder builder = new PropertyMapBuilder();
    //Set the resolver
    builder.put(ValidateProperty.RESOLVER, resolver);  
    builder.put(ValidateProperty.ERROR_HANDLER, eh);
    PropertyMap properties = builder.toPropertyMap();
    try {
      SchemaWrapper sw = (SchemaWrapper) getSchemaReader().createSchema(in,
          properties);
      Pattern start = sw.getStart();
      defaultValuesCollector = new DefaultValuesCollector(start);
      validate(start);
    } catch (IncorrectSchemaException e) {
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, e));
    } catch (Exception e) {
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, e));
    } catch (StackOverflowError e) {
      //EXM-24759 Also catch stackoverflow
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, null));
    }
  }

  /*
   * Validates the parsed document against the schema.
   *
   * @param start the start pattern.
   */
  protected void validate(Pattern start) {
      XMLDocumentSource source = getDocumentSource();
      if (isValidating() && source != null) {
          ValidatorPatternBuilder vpb =
              new ValidatorPatternBuilder(new SchemaPatternBuilder());
          ErrorHandler eh = new DraconianErrorHandler();
          PatternValidator vp = new PatternValidator(start, vpb, eh);
          SaxDocumentHandler dh = new SaxDocumentHandler(source);
          dh.setContentHandler(vp);
      }
  }

  /**
   * Get the default attributes for an element.
   * 
   * @param localName
   *          The element local name.
   * @param namespace
   *          The element namespace. Use null or empty for no namespace.
   * @return A list of Attribute objects or null if no defaults.
   */
  public List<Attribute> getDefaultAttributes(String localName, String namespace) {
    if (defaultValuesCollector != null) {
      return defaultValuesCollector.getDefaultAttributes(localName, namespace);
    }
    return null;
  }
}
