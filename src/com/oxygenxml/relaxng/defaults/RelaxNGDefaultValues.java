/*
 *  The Syncro Soft SRL License
 *
 *  Copyright (c) 1998-2007 Syncro Soft SRL, Romania.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistribution of source or in binary form is allowed only with
 *  the prior written permission of Syncro Soft SRL.
 *
 *  2. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  3. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  4. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Syncro Soft SRL (http://www.sync.ro/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  5. The names "Oxygen" and "Syncro Soft SRL" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact support@oxygenxml.com.
 *
 *  6. Products derived from this software may not be called "Oxygen",
 *  nor may "Oxygen" appear in their name, without prior written
 *  permission of the Syncro Soft SRL.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE SYNCRO SOFT SRL OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */
package com.oxygenxml.relaxng.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.InputSource;

import ro.sync.contentcompletion.xml.relaxng.RelaxNGSchemaManager.SchemaWrapper;
import ro.sync.xml.catalogresolver.CatalogResolverFactory;
import ro.sync.xml.parser.relaxng.JingXMLReaderCreator;

import com.thaiopensource.relaxng.pattern.OxygenDefaultValuesExtractor;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * @author george
 */
public abstract class RelaxNGDefaultValues {  
  /**
   * @return a schema reader. Can be an XML or compact syntax schema reader.
   */
  protected abstract SchemaReader getSchemaReader();
  
  /**
   * Stores collected values.
   */
  private DefaultValuesCollector defaultValuesCollector = null;
  
  /**
   * Collects default values.
   * Listener for the default values extractor.
   */
  class DefaultValuesCollector implements OxygenDefaultValuesExtractor.DefaultValuesListener {
    /**
     * Stores the default attributes as a hash map with the element info as key. 
     */
    private HashMap<String, List<Attribute>> defaults = new HashMap<String, List<Attribute>>();
    
    /**
     * Constructor.
     * @param start The Relax NG schema pattern.
     */
    public DefaultValuesCollector(Pattern start) {
      new OxygenDefaultValuesExtractor(this).parsePattern(start);
    }
    
    /**
     * Get a key for an element.
     * @param elementLocalName
     * @param elementNamespace
     * @return A string formed from the element local name and its namespace.
     */
    private String getKey(String elementLocalName, String elementNamespace) {
      return elementLocalName + "#" + (elementNamespace == null ? "" : elementNamespace);
    }
    
    /**
     * Get the default attributes for an element.
     * @param elementLocalName The element local name.
     * @param elementNamespace The element namespace. Use null or emptry for no namespace.
     * @return A list of Attribute objects or null if no defaults.
     */
    List<Attribute> getDefaultAttributes(String elementLocalName, String elementNamespace) {
      return defaults.get(getKey(elementLocalName, elementNamespace));
    }
    
    /**
     * Default attribute notification.
     * @see com.thaiopensource.relaxng.pattern.OxygenDefaultValuesExtractor.DefaultValuesListener#addDefault(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addDefault(String elementLocalName, String elementNamespace, String attributeLocalName, String attributeNamepsace, String value) {
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
   * @param uri The schema uri.
   */
  public void update(String uri) {
    defaultValuesCollector = null;
    PropertyMapBuilder builder = new PropertyMapBuilder();
    XMLReaderCreator readerCreator = new JingXMLReaderCreator(null);
    builder.put(ValidateProperty.XML_READER_CREATOR, readerCreator);
    PropertyMap properties = builder.toPropertyMap();
    try {
      InputSource in = new InputSource(uri);
      SchemaWrapper sw = (SchemaWrapper) getSchemaReader().createSchema(in, properties);      
      Pattern start = sw.getStart();
      defaultValuesCollector = new DefaultValuesCollector(start);
    } catch (IncorrectSchemaException e) {
      e.printStackTrace();
    } catch (Exception e) {
     e.printStackTrace(); 
    }
  }

  /**
   * Get the default attributes for an element.
   * @param localName The element local name.
   * @param namespace The element namespace. Use null or empty for no namespace.
   * @return A list of Attribute objects or null if no defaults.
   */
  public List<Attribute> getDefaultAttributes(String localName, String namespace) {
    if (defaultValuesCollector != null) {
      return defaultValuesCollector.getDefaultAttributes(localName, namespace);
    }
    return null;
  }
  
  /**
   * Resolves the system id of the argument input source to another input source
   * using the catalog resolver.
   * 
   * @param in The input source.
   * @return The same input source if it was not resolved using the catalog or a new one.
   */
  static InputSource resolveWithCatalog(InputSource in) {
    InputSource is =
      CatalogResolverFactory.getEntityResolver().resolveEntity(null, in.getSystemId());
    if (is != null && is.getSystemId() != null) {
      in = is;
    }
    return in;
  }
}
