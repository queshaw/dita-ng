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

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;

import ro.sync.contentcompletion.xml.relaxng.OxygenRelaxNGSchemaReader;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.SchemaReader;

/**
 * @author george
 *
 */
public class RNCDefaultValues extends RelaxNGDefaultValues {
  /**
   * class OxygenCompactSchemaReader extends SchemaReaderImpl
   */
  private static class OxygenCompactSchemaReader extends OxygenRelaxNGSchemaReader {
    /**
     * The instance of schema reader.
     */
    private static final SchemaReader theInstance = new OxygenCompactSchemaReader();

    /**
     * Not instantiable from outside.
     */
    private OxygenCompactSchemaReader() {
      super();
    }
    
    /**
     * Get the reader singleton.
     * 
     * @return The current Schema Reader instance.
     */
    private static SchemaReader getInstance() {
      return theInstance;
    }

    /**
     *  Creates a parseable object from a catalog resolved input source 
     *  associated to a RNG schema.
     * 
     * @return the parseable object
     */
    @Override
    protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> 
    createParseable(SAXSource source, SAXResolver saxResolver, ErrorHandler eh, PropertyMap properties) {
      return new CompactParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl>
        (SAX.createInput(resolveWithCatalog(source.getInputSource())), saxResolver.getResolver(), eh);
    }
  }

  /**
   * Returns an instance of OxygenCompactSchemaReader.
   */
  @Override
  protected SchemaReader getSchemaReader() {
    return OxygenCompactSchemaReader.getInstance();
  }
}
