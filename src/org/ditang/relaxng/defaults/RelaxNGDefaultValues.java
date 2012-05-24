package org.ditang.relaxng.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ditang.relaxng.defaults.OxygenRelaxNGSchemaReader.SchemaWrapper;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.relaxng.pattern.DefaultValuesExtractor;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;

/**
 * @author george@oxygenxml.com
 */
public abstract class RelaxNGDefaultValues {
  private EntityResolver resolver;
  private ErrorHandler eh;
  

  public RelaxNGDefaultValues(EntityResolver resolver, ErrorHandler eh) {
    this.resolver = resolver;
    this.eh = eh;
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
   * @param uri
   *          The schema uri.
   * @throws SAXException 
   */
  public void update(InputSource in) throws SAXException {
    defaultValuesCollector = null;
    PropertyMapBuilder builder = new PropertyMapBuilder();
    builder.put(ValidateProperty.ENTITY_RESOLVER, resolver);
    builder.put(ValidateProperty.ERROR_HANDLER, eh);
    PropertyMap properties = builder.toPropertyMap();
    try {
      SchemaWrapper sw = (SchemaWrapper) getSchemaReader().createSchema(in,
          properties);
      Pattern start = sw.getStart();
      defaultValuesCollector = new DefaultValuesCollector(start);
    } catch (IncorrectSchemaException e) {
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, e));
    } catch (Exception e) {
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, e));
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
