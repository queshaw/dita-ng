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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xerces.util.SymbolTable;
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
import org.xml.sax.InputSource;

import ro.sync.util.URLUtil;
import ro.sync.xml.XmlUtil;
import ro.sync.xml.catalogresolver.CatalogResolverFactory;

/**
 * XNI component that adds Relax NG default values.
 * @author george
 */
public class RelaxNGDefaultsComponent implements XMLDocumentHandler, XMLComponent, XMLDocumentSource {  
  private XMLDocumentHandler documentHandler;
  private XMLDocumentSource documentSource;
  
  private boolean detecting = false;
  private String schema = null;
  private String type = null;
  private String baseSystemId = null;
  
  private RelaxNGDefaultValues defaults;

  private NamespaceContext context;
  private SymbolTable st;
  
  /**
   * Constructor.
   * @param fSymbolTable 
   */
  public RelaxNGDefaultsComponent(SymbolTable fSymbolTable) {
    st = fSymbolTable;
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(org.apache.xerces.xni.XMLLocator, java.lang.String, org.apache.xerces.xni.NamespaceContext, org.apache.xerces.xni.Augmentations)
   */
  public void startDocument(XMLLocator locator, String enc, NamespaceContext nc, Augmentations aug) throws XNIException {
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
   * @see org.apache.xerces.xni.parser.XMLComponent#reset(org.apache.xerces.xni.parser.XMLComponentManager)
   */
  public void reset(XMLComponentManager arg0) throws XMLConfigurationException {
    baseSystemId = null;
    detecting = false;
    schema = null;
    type = null;
    context = null;
  }
  
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
   */
  public void startElement(QName name, XMLAttributes atts, Augmentations aug) throws XNIException {    
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
    if (schema != null && "xml".equals(type)) {
      defaults = new RNGDefaultValues();
    }
    if (schema != null && "compact".equals(type)) {
      defaults = new RNCDefaultValues();
    }
    if (defaults != null) {
      try {
        String schemaURL = baseSystemId == null? new URL(schema).toString() : new URL(new URL(baseSystemId), schema).toString();
        defaults.update(schemaURL);
      } catch (MalformedURLException e) {
        try {
          URL resolved = resolveWithCatalog(schema);
          if (resolved != null) {
            defaults.update(resolved.toString());
          }
        } catch (MalformedURLException e1) {
        }
      }
    }
  }
  
  /**
   * Try to resolve the specified schema location with the catalog resolver.
   * 
   * @param schemaLocation The specified schema location.
   *
   * @return The new schema location if the specified location can be resolved with the catalog or
   *         <code>null</code> otherwise.
   *
   * @throws MalformedURLException if the location specified by the catalog is not a valid URL.
   */
  private static URL resolveWithCatalog(String schemaLocation) throws MalformedURLException {
    InputSource catalogSource =
      CatalogResolverFactory.getEntityResolver().resolveEntity(null, schemaLocation);
    String catalogSchemaLocation = null;
    if (catalogSource != null) {
      catalogSchemaLocation = catalogSource.getSystemId();
    }
    URL schemaLocationURL = null;
    if (catalogSchemaLocation != null) {
      schemaLocationURL = URLUtil.correct(new URL(catalogSchemaLocation));
    } 
    return schemaLocationURL;
  }
  
  
  /**
   * @param name
   * @param atts
   */
  private void checkAndAddDefaults(QName name, XMLAttributes atts) {
    List<RelaxNGDefaultValues.Attribute> def = defaults.getDefaultAttributes(name.localpart, name.uri);
    if (def != null) {        
      for (RelaxNGDefaultValues.Attribute a: def) {
        if (atts.getIndex(a.namespace, a.localName) < 0) {
          String prefix = null;
          String rawname = a.localName;
          if (a.namespace != null && a.namespace.length()>0) {
            prefix = context.getPrefix(a.namespace);
            if (prefix == null) {
              for (int i =0; i < atts.getLength(); i++) {
                String attname = atts.getQName(i);
                if (attname.startsWith("xmlns:")) {
                  if (a.namespace.equals(atts.getValue(i))) {
                    prefix = attname.substring(6);
                  }
                }
              }
            }
            
            if (prefix != null && prefix.length()>0) {
              rawname = prefix + ":" + a.localName;
              // double check in case of no namespace aware parsers.
              // if we want to fully handle this case we may need further processing.
              if (atts.getIndex(rawname) < 0) {
                QName attName = new QName(st.addSymbol(prefix), st.addSymbol(a.localName), st.addSymbol(rawname), st.addSymbol(a.namespace));
                atts.addAttribute(attName, "CDATA", a.value);
                int attrIndex = atts.getIndex(attName.uri, attName.localpart);
                atts.setSpecified(attrIndex, false);
              }
            } else {
              int k = 1;
              prefix = "ns" + k;
              while (context.getURI(prefix) != null || atts.getValue("xmlns:" + prefix) != null) {
                k++;
                prefix = "ns" + k;
              }
              rawname = prefix + ":" + a.localName;
              
              QName attNs = new QName(st.addSymbol("xmlns"), st.addSymbol(prefix), st.addSymbol("xmlns:"+prefix), st.addSymbol("http://www.w3.org/2000/xmlns/"));
              atts.addAttribute(attNs, "CDATA", a.namespace);
              
              QName attName = new QName(st.addSymbol(prefix), st.addSymbol(a.localName), st.addSymbol(rawname), st.addSymbol(a.namespace));
              atts.addAttribute(attName, "CDATA", a.value);
              int attrIndex = atts.getIndex(attName.uri, attName.localpart);
              atts.setSpecified(attrIndex, false);
            }
          } else {
            String attname = st.addSymbol(a.localName);
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
   * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void xmlDecl(String arg0, String arg1, String arg2, Augmentations arg3) throws XNIException {
    if (documentHandler != null) {
      documentHandler.xmlDecl(arg0, arg1, arg2, arg3);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void doctypeDecl(String arg0, String arg1, String arg2, Augmentations arg3) throws XNIException {
    if (documentHandler != null) {
      documentHandler.doctypeDecl(arg0, arg1, arg2, arg3);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#comment(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
   */
  public void comment(XMLString arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.comment(arg0, arg1);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#processingInstruction(java.lang.String, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
   */
  public void processingInstruction(String name, XMLString content, Augmentations arg2) throws XNIException {
    if (detecting && schema == null && "oxygen".equals(name)) {
      String data = content.toString();
      schema = XmlUtil.getFromPIDataPseudoAttribute(data, "RNGSchema", true);
      type = XmlUtil.getFromPIDataPseudoAttribute(data, "type", true);
    }    
    if (documentHandler != null) {
      documentHandler.processingInstruction(name, content, arg2);
    }
  }
  
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
   */
  public void emptyElement(QName name, XMLAttributes atts, Augmentations arg2) throws XNIException {
    onStartElement(name, atts);
    if (documentHandler != null) {
      documentHandler.emptyElement(name, atts, arg2);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startGeneralEntity(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void startGeneralEntity(String arg0, XMLResourceIdentifier arg1, String arg2, Augmentations arg3) throws XNIException {
    if (documentHandler != null) {
      documentHandler.startGeneralEntity(arg0, arg1, arg2, arg3);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#textDecl(java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void textDecl(String arg0, String arg1, Augmentations arg2) throws XNIException {
    if (documentHandler != null) {
      documentHandler.textDecl(arg0, arg1, arg2);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endGeneralEntity(java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void endGeneralEntity(String arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.endGeneralEntity(arg0, arg1);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#characters(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
   */
  public void characters(XMLString arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.characters(arg0, arg1);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#ignorableWhitespace(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
   */
  public void ignorableWhitespace(XMLString arg0, Augmentations arg1) throws XNIException {
    if (documentHandler != null) {
      documentHandler.ignorableWhitespace(arg0, arg1);
    }
  }
  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.Augmentations)
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
    return null;
  }
  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#setFeature(java.lang.String, boolean)
   */
  public void setFeature(String arg0, boolean arg1) throws XMLConfigurationException {
    //Do nothing  
  }
  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getRecognizedProperties()
   */
  public String[] getRecognizedProperties() {
    return null;
  }
  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#setProperty(java.lang.String, java.lang.Object)
   */
  public void setProperty(String arg0, Object arg1) throws XMLConfigurationException {
    //Do nothing  
  }
  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getFeatureDefault(java.lang.String)
   */
  public Boolean getFeatureDefault(String arg0) {
    return null;
  }
  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#getPropertyDefault(java.lang.String)
   */
  public Object getPropertyDefault(String arg0) {
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
