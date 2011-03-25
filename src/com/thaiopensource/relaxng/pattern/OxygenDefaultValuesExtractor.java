/*
 *  The Syncro Soft SRL License
 *
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
package com.thaiopensource.relaxng.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

/**
 * Extracts the default values for attributes and notifies a
 * listener for each value.
 * 
 * @author george
 */
public class OxygenDefaultValuesExtractor {
  /**
   * Receives notification of default values. 
   */
  public static interface DefaultValuesListener {
    /**
     * The callback/notification method.
     * @param elementName The local name of the element.
     * @param elementNamespace The element namespace.
     * @param attributeName The local name of the attribute.
     * @param attributeNamepsace The attribute namespace.
     * @param value The default value.
     */
    public void addDefault(String elementName, String elementNamespace, String attributeName, String attributeNamepsace, String value);
  }
  
  /**
   * The listener that will receive default value notifications.
   */
  private DefaultValuesListener listener;
  // store a list of element patterns.
  private final List<Pattern> patternList = new ArrayList<Pattern>();
  private final HashSet<Pattern> patternSet = new HashSet<Pattern>();

  private ElementContentVisitor ecv = new ElementContentVisitor(); 
  private ElementsVisitor ev = new ElementsVisitor(); 
  
  
  /**
   * Constructor
   * 
   * @param listener The annotation manager
   */
  public OxygenDefaultValuesExtractor(DefaultValuesListener listener) {
    this.listener = listener;
  }

  /**
   * Trigger the parsing.
   * @param p The start pattern.
   */
  public void parsePattern(Pattern p) {
    p.apply(ecv);
    for (int i = 0; i < patternList.size(); i++) {
      Pattern tem = patternList.get(i);
      tem.apply(ev);
    }
  }
  
  private void addPattern(Pattern p) {
    if (!patternSet.contains(p)) {
      patternList.add(p);
      patternSet.add(p);
    }
  }

  /**
   * Base class for pattern visitors.
   * @author george
   */
  class BaseVisitor implements PatternFunction<VoidValue>, NameClassVisitor {
    // ** Pattern visitor methods.** //
    public VoidValue caseElement(ElementPattern p)          {return VoidValue.VOID;}
    public VoidValue caseAttribute(AttributePattern p)      {return VoidValue.VOID;}

    public VoidValue caseError(ErrorPattern p)              {return VoidValue.VOID;}
    public VoidValue caseEmpty(EmptyPattern p)              {return VoidValue.VOID;}
    public VoidValue caseNotAllowed(NotAllowedPattern p)    {return VoidValue.VOID;}    
    public VoidValue caseGroup(GroupPattern g)              {g.getOperand1().apply(this);g.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseInterleave(InterleavePattern i)    {i.getOperand1().apply(this);i.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseChoice(ChoicePattern c)            {c.getOperand1().apply(this);c.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseOneOrMore(OneOrMorePattern p)      {p.getOperand().apply(this);return VoidValue.VOID;}
    public VoidValue caseData(DataPattern d)                {return VoidValue.VOID;}
    public VoidValue caseDataExcept(DataExceptPattern p)    {return VoidValue.VOID;}
    public VoidValue caseValue(ValuePattern p)              {return VoidValue.VOID;}
    public VoidValue caseText(TextPattern t)                {return VoidValue.VOID;}
    public VoidValue caseList(ListPattern l)                {return VoidValue.VOID;}
    public VoidValue caseRef(RefPattern p)                  {p.getPattern().apply(this);return VoidValue.VOID;}
    public VoidValue caseAfter(AfterPattern p)              {return VoidValue.VOID;}

    // ** NameClass visitor methods.** //
    public void visitName(Name name)                        {}
    
    public void visitChoice(NameClass nc1, NameClass nc2)   {nc1.accept(this);nc2.accept(this);}
    public void visitNsName(String ns)                      {}
    public void visitNsNameExcept(String ns, NameClass nc)  {}
    public void visitAnyName()                              {}
    public void visitAnyNameExcept(NameClass nc)            {}
    public void visitNull()                                 {}
    public void visitError()                                {}
  }
  
  /**
   * Adds element patterns to the patterns list.
   * @author george
   */
  class ElementContentVisitor extends BaseVisitor {
    @Override
    public VoidValue caseElement(ElementPattern p) {
      addPattern(p);
      return VoidValue.VOID;
    }
  }
  
  /**
   * Visits an element, extracts default attributes and calls
   * the element content visitor to visit the element content.
   * 
   * @author george
   */
  class ElementsVisitor extends BaseVisitor {
    /**
     * Keeps all the elements found. List of Name.
     */
    private List<Name> elements = new ArrayList<Name>();

    @Override
    public VoidValue caseElement(ElementPattern p) {
      // determine the element name and call the attribute visitor on
      // its content.
      elements.clear();
      p.getNameClass().accept(this);
      if (elements.size() > 0) {
        p.getContent().apply(new AttributesVisitor(elements));
      }
      // call the content visitor to get other elements.
      p.getContent().apply(ecv);
      return VoidValue.VOID;
    }

    @Override
    public void visitName(Name name) {
      elements.add(name);
    }
  }
  
  /**
   * Notify the listener for each visited attribute with default value.
   */
  class AttributesVisitor extends BaseVisitor {
    private String defaultValue;
    private List<Name> elements;
    /**
     * @param elements The parent element names for the visited attributes.
     */
    public AttributesVisitor(List<Name> elements) {
      this.elements = elements;
    }
    @Override
    public VoidValue caseAttribute(AttributePattern p) {
      defaultValue =  p.getDefaultValue();
      if (defaultValue != null) {
        p.getNameClass().accept(this);
      }
      return VoidValue.VOID;
    }
    @Override
    public void visitName(Name name) {
      for (Name eName : elements) {
        listener.addDefault(eName.getLocalName(), eName.getNamespaceUri(), name.getLocalName(), name.getNamespaceUri(), defaultValue);
      }
    }
  }
}
