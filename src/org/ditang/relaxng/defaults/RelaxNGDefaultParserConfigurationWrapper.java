package org.ditang.relaxng.defaults;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class RelaxNGDefaultParserConfigurationWrapper implements XMLParserConfiguration {
  XMLParserConfiguration config;
  
  public RelaxNGDefaultParserConfigurationWrapper() {
    XIncludeAwareParserConfiguration c = new XIncludeAwareParserConfiguration();
    this.config = c;
  }
  
  public RelaxNGDefaultParserConfigurationWrapper(XMLParserConfiguration config) {
    this.config = config;
  }
  
  @Override
  public void parse(XMLInputSource inputSource) throws XNIException,
      IOException {
    config.parse(inputSource);
  }

  @Override
  public void addRecognizedFeatures(String[] featureIds) {
    config.addRecognizedFeatures(featureIds);
  }

  @Override
  public void setFeature(String featureId, boolean state)
      throws XMLConfigurationException {
    config.setFeature(featureId, state);
  }

  @Override
  public boolean getFeature(String featureId) throws XMLConfigurationException {
    return config.getFeature(featureId);
  }

  @Override
  public void addRecognizedProperties(String[] propertyIds) {
    config.addRecognizedProperties(propertyIds);
  }

  @Override
  public void setProperty(String propertyId, Object value)
      throws XMLConfigurationException {
    config.setProperty(propertyId, value);
  }

  @Override
  public Object getProperty(String propertyId) throws XMLConfigurationException {
    return config.getProperty(propertyId);
  }

  @Override
  public void setErrorHandler(XMLErrorHandler errorHandler) {
    config.setErrorHandler(errorHandler);
  }

  @Override
  public XMLErrorHandler getErrorHandler() {
    return config.getErrorHandler();
  }

  @Override
  public void setDocumentHandler(XMLDocumentHandler documentHandler) {
    config.setDocumentHandler(documentHandler);
  }

  @Override
  public XMLDocumentHandler getDocumentHandler() {
    return config.getDocumentHandler();
  }

  @Override
  public void setDTDHandler(XMLDTDHandler dtdHandler) {
    config.setDTDHandler(dtdHandler);
  }

  @Override
  public XMLDTDHandler getDTDHandler() {
    return config.getDTDHandler();
  }

  @Override
  public void setDTDContentModelHandler(
      XMLDTDContentModelHandler dtdContentModelHandler) {
    config.setDTDContentModelHandler(dtdContentModelHandler);
    
  }

  @Override
  public XMLDTDContentModelHandler getDTDContentModelHandler() {
    return config.getDTDContentModelHandler();
  }

  @Override
  public void setEntityResolver(XMLEntityResolver entityResolver) {
    config.setEntityResolver(entityResolver);
  }

  @Override
  public XMLEntityResolver getEntityResolver() {
    return config.getEntityResolver();
  }

  @Override
  public void setLocale(Locale locale) throws XNIException {
    config.setLocale(locale);
  }

  @Override
  public Locale getLocale() {
    return config.getLocale();
  }
}
