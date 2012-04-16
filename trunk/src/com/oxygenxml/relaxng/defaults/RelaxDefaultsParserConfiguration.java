package com.oxygenxml.relaxng.defaults;

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * @author george@oxygenxml.com
 * A parser configuration that adds in a module to add Relax NG specified default values.
 */
public class RelaxDefaultsParserConfiguration extends XIncludeAwareParserConfiguration  {
  
  /** Feature identifier: dynamic validation. */
  protected static final String DYNAMIC_VALIDATION =
      Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;
  /** Feature identifier: validation. */
  protected static final String VALIDATION =
      Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
  /**
   * An XML component that adds default attribute values by looking into a Relax NG schema
   * that includes a:defaultValue annotations.
   * See the Relax NG DTD compatibility specification.
   */
  RelaxNGDefaultsComponent fRelaxDefaults = null;

  /**
   * Default constructor.
   */
  public RelaxDefaultsParserConfiguration() {
    this(null, null);
  }

  /** 
   * Constructs a parser configuration using the specified symbol table. 
   *
   * @param symbolTable The symbol table to use.
   */
  public RelaxDefaultsParserConfiguration(SymbolTable symbolTable) {
    this(symbolTable, null, null);
  }

  /**
   * Constructs a parser configuration using the specified symbol table and
   * grammar pool.
   * <p>
   *
   * @param symbolTable The symbol table to use.
   * @param grammarPool The grammar pool to use.
   */
  public RelaxDefaultsParserConfiguration(
      SymbolTable symbolTable,
      XMLGrammarPool grammarPool) {
    this(symbolTable, grammarPool, null);
  }

  /**
   * Constructs a parser configuration using the specified symbol table,
   * grammar pool, and parent settings.
   * <p>
   *
   * @param symbolTable    The symbol table to use.
   * @param grammarPool    The grammar pool to use.
   * @param parentSettings The parent settings.
   */
  public RelaxDefaultsParserConfiguration(
      SymbolTable symbolTable,
      XMLGrammarPool grammarPool,
      XMLComponentManager parentSettings) {

    super(symbolTable, grammarPool, parentSettings);

  }

  @Override
  public boolean parse(boolean complete) throws XNIException, IOException {
    if (fInputSource != null) {
      setFeature(DYNAMIC_VALIDATION, getFeature(VALIDATION));
    }
    return super.parse(complete);
  }
  
  /** 
   * Configures the pipeline. 
   */
  @Override
  protected void configurePipeline() {
    super.configurePipeline();
    insertRelaxDefaultsComponent();
  }
  
  /**
   * Configures the pipeline.
   */
  @Override
  protected void configureXML11Pipeline() {
    super.configureXML11Pipeline();
    insertRelaxDefaultsComponent();
  }

  protected void insertRelaxDefaultsComponent() {
    if (fRelaxDefaults == null) {
      fRelaxDefaults = new RelaxNGDefaultsComponent();
      addCommonComponent(fRelaxDefaults);
      fRelaxDefaults.reset(this);
    }
    XMLDocumentSource prev = fLastComponent;
    fLastComponent = fRelaxDefaults;
      
    XMLDocumentHandler next = prev.getDocumentHandler();
    prev.setDocumentHandler(fRelaxDefaults);
    fRelaxDefaults.setDocumentSource(prev);
    if (next != null) {
        fRelaxDefaults.setDocumentHandler(next);
        next.setDocumentSource(fRelaxDefaults);
    }
  }
  
}
