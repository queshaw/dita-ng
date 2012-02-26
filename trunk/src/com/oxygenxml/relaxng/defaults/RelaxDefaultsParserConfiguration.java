package com.oxygenxml.relaxng.defaults;

import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

/**
 * @author george@oxygenxml.com
 * A parser configuration that adds in a module to add Relax NG specified default values.
 */
public class RelaxDefaultsParserConfiguration extends XIncludeAwareParserConfiguration  {
  /**
   * An XML component that adds default attribute values by looking into a Relax NG schema
   * that includes a:defaultValue annotations.
   * See the Relax NG DTD compatibility specification.
   */
  RelaxNGDefaultsComponent relaxDefaults = null;
  
  /**
   * 
   */
  public RelaxDefaultsParserConfiguration() {
    super();
    relaxDefaults = new RelaxNGDefaultsComponent(fSymbolTable);
    addComponent(relaxDefaults);
  }
  
  /**
   * 
   * @param st
   * @param xgp
   */
  public RelaxDefaultsParserConfiguration(SymbolTable st, XMLGrammarPool xgp) {
    super(st, xgp);
    relaxDefaults = new RelaxNGDefaultsComponent(fSymbolTable);
    addComponent(relaxDefaults);
  }
  
  /** 
   * Configures the pipeline. 
   */
  @Override
  protected void configurePipeline() {
    super.configurePipeline();
    if (fLastComponent != null) {
      relaxDefaults.setDocumentHandler(fLastComponent.getDocumentHandler());
      fLastComponent.setDocumentHandler(relaxDefaults);
      relaxDefaults.setDocumentSource(fLastComponent);
      fLastComponent = relaxDefaults;
    }
  }
  
  /**
   * Configures the pipeline.
   */
  @Override
  protected void configureXML11Pipeline() {
    super.configureXML11Pipeline();
    if (fLastComponent != null) {
      relaxDefaults.setDocumentHandler(fLastComponent.getDocumentHandler());
      fLastComponent.setDocumentHandler(relaxDefaults);
      relaxDefaults.setDocumentSource(fLastComponent);
      fLastComponent = relaxDefaults;
    }
  }
}
