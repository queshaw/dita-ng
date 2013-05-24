<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:rng="http://relaxng.org/ns/structure/1.0"
  xmlns:rnga="http://relaxng.org/ns/compatibility/annotations/1.0"
  xmlns:rng2ditadtd="http://dita.org/rng2ditadtd"
  xmlns:relpath="http://dita2indesign/functions/relpath"
  exclude-result-prefixes="xs xd rng rnga relpath"
  version="2.0">

  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>RNG to DITA DTD Converter</xd:p>
      <xd:p><xd:b>Created on:</xd:b> Feb 16, 2013</xd:p>
      <xd:p><xd:b>Authors:</xd:b> ekimber, pleblanc</xd:p>
      <xd:p>This transform takes as input RNG-format DITA document type
      shells and produces from them the DTD shell file
        that reflect the RNG definitions and conform to the DITA 1.3
        DTD coding requirements.
      </xd:p>
    </xd:desc>
  </xd:doc>

  <!-- ==============================
       .dtd file generation mode
       ============================== -->

  <xsl:template match="rng:grammar" mode="dtdFile">
    <xsl:param name="dtdFilename" tunnel="yes" as="xs:string" />
    <xsl:param name="dtdDir" tunnel="yes" as="xs:string" />
    <xsl:param name="modulesToProcess" tunnel="yes" as="document-node()*" />
    <xsl:variable name="thisDomain" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:variable name="thisVersion" select="normalize-space(substring-before(substring-after(../comment()[1],'VERSION:'),'DATE:'))" />
    <xsl:variable name="thisDate" select="normalize-space(substring-before(substring-after(../comment()[1],'DATE:'),'='))" />
    <xsl:text>&lt;?xml version="1.0" encoding="UTF-8"?>
&lt;!-- ============================================================= -->
&lt;!--                    HEADER                                     -->
&lt;!-- ============================================================= -->
&lt;!--  MODULE:    </xsl:text>
    <xsl:choose>
      <xsl:when test="contains($thisDomain,'(')">
        <xsl:value-of select="concat(substring-before($thisDomain,'('), ' DTD (', substring-after($thisDomain,'('))" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat($thisDomain, ' DTD')" />
      </xsl:otherwise>
    </xsl:choose>
<xsl:text>                -->
&lt;!--  VERSION:   </xsl:text><xsl:value-of select="$thisVersion" /><xsl:text>                                               -->
&lt;!--  DATE:      </xsl:text><xsl:value-of select="$thisDate" /><xsl:text>                                      -->
&lt;!--                                                               -->
&lt;!-- ============================================================= -->

&lt;!-- ============================================================= -->
&lt;!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
&lt;!--                    TYPICAL INVOCATION                         -->
&lt;!--                                                               -->
&lt;!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//DTD </xsl:text>
    <xsl:choose>
      <xsl:when test="contains($thisDomain,'(')">
        <xsl:value-of select="substring-before($thisDomain,'(')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$thisDomain" />
      </xsl:otherwise>
    </xsl:choose>
<xsl:text>//EN"
      Delivered as file "</xsl:text><xsl:value-of select="$dtdFilename" /><xsl:text>"               -->

&lt;!-- ============================================================= -->
&lt;!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
&lt;!--                                                               -->
&lt;!-- PURPOSE:    DTD to describe                                   -->
&lt;!--             </xsl:text><xsl:value-of select="$thisDomain" /><xsl:text>    -->
&lt;!--                                                               -->
&lt;!-- ORIGINAL CREATION DATE:                                       -->
&lt;!--             February 2008                                     -->
&lt;!--                                                               -->
&lt;!--             (C) Copyright OASIS Open 2008, 2009.              -->
&lt;!--             All Rights Reserved.                              -->
&lt;!--                                                               -->
&lt;!--  UPDATES:                                                     -->
&lt;!--     </xsl:text><xsl:value-of select="$thisDate" /><xsl:text>: generated from Relax NG implementation      -->
&lt;!-- ============================================================= -->

</xsl:text>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    ENTITY DECLARATIONS                        -->
&lt;!-- ============================================================= -->
</xsl:text>
    <xsl:choose>
      <xsl:when test="count(*)=1 and rng:include">
        <!--  simple redirection, as in technicalContent\glossary.dtd -->
        <xsl:apply-templates mode="dtdRedirect" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$modulesToProcess" mode="entityDeclaration" />

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    DOMAIN EXTENSIONS                          -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:for-each-group select="($modulesToProcess | .)//rng:define" group-by="@name">
          <xsl:if test="current-group()/rng:ref[contains(@name, '-d-')]">
            <!--  element with domain extensions -->
            <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text><xsl:value-of select="current-grouping-key()" /><xsl:text>    "</xsl:text>
            <xsl:apply-templates select="current-group()" mode="domainExtension" />
            <xsl:text>">&#x0a;</xsl:text>
          </xsl:if>
        </xsl:for-each-group>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    DOMAIN ATTRIBUTE EXTENSIONS                -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:apply-templates select="$modulesToProcess" mode="attributeExtension" />

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    TOPIC NESTING OVERRIDE                     -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:for-each-group select="($modulesToProcess | .)//rng:define" group-by="@name">
          <xsl:if test="current-group()/rng:ref[@name='info-types']">
            <!--  topic override -->
            <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text><xsl:value-of select="current-grouping-key()" /><xsl:text>    "</xsl:text>
            <xsl:apply-templates select="current-group()" mode="nestingOverride" />
            <xsl:text>">&#x0a;</xsl:text>
          </xsl:if>
        </xsl:for-each-group>

        <xsl:if test="rng:define/rng:element">
<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    LOCALLY DEFINED                            -->
&lt;!-- ============================================================= -->
</xsl:text>
          <xsl:apply-templates select="rng:define/rng:element" mode="localDefinition" />
        </xsl:if>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:text>&#x0a;&lt;!ENTITY included-domains&#x0a;    "</xsl:text>
        <xsl:apply-templates select="$modulesToProcess" mode="attributeOverride" />
        <xsl:text>">&#x0a;</xsl:text>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    TOPIC ELEMENT INTEGRATION                  -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:if test="//rng:start">
          <xsl:apply-templates select="$modulesToProcess" mode="mainDeclaration">
            <xsl:with-param name="mainElement" select="//rng:start/rng:ref/@name" as="xs:string" />
          </xsl:apply-templates>
        </xsl:if>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    DOMAIN ELEMENT INTEGRATION                 -->
&lt;!-- ============================================================= -->
</xsl:text>
        <xsl:apply-templates select="$modulesToProcess" mode="elementDeclaration" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:include" mode="dtdRedirect">
    <xsl:variable name="dtdRedirect" select="substring-before(@href,'.rng')" />
    <xsl:variable name="dtdDoc" select="document(@href)" as="document-node()" />
    <xsl:variable name="dtdPublicId" select="normalize-space(substring-before(substring-after($dtdDoc/comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text><xsl:value-of select="concat($dtdRedirect,'Dtd')" /> 
    <xsl:text>&#x0a;  PUBLIC "-//OASIS//DTD </xsl:text><xsl:value-of select="$dtdPublicId" /><xsl:text>//EN" &#x0a;         "</xsl:text>
    <xsl:value-of select="concat($dtdRedirect,'.dtd')" />
    <xsl:text>"&#x0a;>&#x0a;%</xsl:text>
    <xsl:value-of select="concat($dtdRedirect,'Dtd')" /> 
    <xsl:text>;&#x0a;</xsl:text>
  </xsl:template>



  <xsl:template match="*" mode="entityDeclaration">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="entityDeclaration">
    <xsl:param name="dtdDir" tunnel="yes" as="xs:string" />
    <xsl:variable name="rngModuleUrl" as="xs:string" select="string(document-uri(/))" />
    <xsl:variable name="rngModuleDir" as="xs:string" select="relpath:getParent($rngModuleUrl)" />
    <xsl:variable name="rngRelPath" as="xs:string" select="relpath:getRelativePath($dtdDir,$rngModuleDir)" />
    <xsl:variable name="dtdRelPath" select="replace($rngRelPath, '/rng','/dtd')" />
    <xsl:variable name="entFilename" as="xs:string" select="concat(relpath:getNamePart(relpath:getNamePart(document-uri(root(.)))), '.ent')" />
    <xsl:variable name="publicId" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:variable name="domainValue" select="rng:define[@name='domains-atts-value']/rng:value[1]"/>
    <xsl:if test="$domainValue and not($domainValue='')">
      <xsl:variable name="domain" select="substring-before(tokenize($domainValue, ' ')[last()],')')"/>
      <xsl:variable name="domainDec" select="concat($domain,'-dec')" />
      <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text><xsl:value-of select="$domainDec" /> 
      <xsl:text>&#x0a;  PUBLIC "-//OASIS//ENTITIES </xsl:text><xsl:value-of select="$publicId" /><xsl:text>//EN" &#x0a;         "</xsl:text>
      <xsl:if test="not($dtdRelPath='')">
        <xsl:value-of select="concat($dtdRelPath,'/')" />
      </xsl:if> 
      <xsl:value-of select="$entFilename" />
      <xsl:text>"&#x0a;>&#x0a;%</xsl:text>
      <xsl:value-of select="$domainDec" />
      <xsl:text>;&#x0a;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="mainDeclaration">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="mainDeclaration">
    <xsl:param name="mainElement" as="xs:string" />
    <xsl:param name="dtdDir" tunnel="yes" as="xs:string" />
    <xsl:variable name="rngModuleUrl" as="xs:string" select="string(document-uri(/))" />
    <xsl:variable name="rngModuleDir" as="xs:string" select="relpath:getParent($rngModuleUrl)" />
    <xsl:variable name="rngRelPath" as="xs:string" select="relpath:getRelativePath($dtdDir,$rngModuleDir)" />
    <xsl:variable name="dtdRelPath" select="replace($rngRelPath, '/rng','/dtd')" />
    <xsl:if test="$mainElement and not($mainElement='')">
      <xsl:if test="//rng:define[@name=$mainElement]">
        <xsl:variable name="entFilename" as="xs:string" select="concat(relpath:getNamePart(relpath:getNamePart(document-uri(root(.)))), '.mod')" />
        <xsl:variable name="publicId" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
        <xsl:text>&#x0a;&lt;!ENTITY % main-type</xsl:text>
        <xsl:text>&#x0a;  PUBLIC "-//OASIS//ELEMENTS </xsl:text><xsl:value-of select="$publicId" /><xsl:text>//EN" &#x0a;  "</xsl:text>
        <xsl:if test="not($dtdRelPath='')">
          <xsl:value-of select="concat($dtdRelPath,'/')" />
        </xsl:if> 
        <xsl:value-of select="$entFilename" />
        <xsl:text>"&#x0a;>&#x0a;%main-type;&#x0a;</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="elementDeclaration">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="elementDeclaration">
    <xsl:param name="dtdDir" tunnel="yes" as="xs:string" />
    <xsl:variable name="rngModuleUrl" as="xs:string" select="string(document-uri(/))" />
    <xsl:variable name="rngModuleDir" as="xs:string" select="relpath:getParent($rngModuleUrl)" />
    <xsl:variable name="rngRelPath" as="xs:string" select="relpath:getRelativePath($dtdDir,$rngModuleDir)" />
    <xsl:variable name="dtdRelPath" select="replace($rngRelPath, '/rng','/dtd')" />
    <xsl:variable name="entFilename" as="xs:string" select="concat(relpath:getNamePart(relpath:getNamePart(document-uri(root(.)))), '.mod')" />
    <xsl:variable name="publicId" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:variable name="domainValue" select="rng:define[@name='domains-atts-value']/rng:value[1]"/>
    <xsl:if test="$domainValue and not($domainValue='')">
      <xsl:variable name="domain" select="substring-before(tokenize($domainValue, ' ')[last()],')')"/>
      <xsl:if test="ends-with($domain,'-d')">
        <xsl:variable name="domainDef" select="concat($domain,'-def')" />
        <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text>
        <xsl:value-of select="$domainDef" /> 
        <xsl:text>&#x0a;  PUBLIC "-//OASIS//ELEMENTS </xsl:text>
        <xsl:value-of select="$publicId" />
        <xsl:text>//EN" &#x0a;  "</xsl:text>
        <xsl:if test="not($dtdRelPath='')">
          <xsl:value-of select="concat($dtdRelPath,'/')" />
        </xsl:if> 
        <xsl:value-of select="$entFilename" />
        <xsl:text>"&#x0a;>&#x0a;%</xsl:text>
        <xsl:value-of select="$domainDef" /><xsl:text>;&#x0a;</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="domainExtension">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:define" mode="domainExtension">
    <xsl:apply-templates mode="moduleFile" select="node()" />
    <xsl:if test="not(position()=last())"><xsl:text> |</xsl:text></xsl:if>
    <xsl:text>&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template match="*" mode="attributeExtension">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="attributeExtension">
    <xsl:apply-templates select="//rng:define[ends-with(@name,'attribute-extensions')]"  mode="#current" />
  </xsl:template>

  <xsl:template match="rng:define" mode="attributeExtension">
    <xsl:text>&#x0a;&lt;!ENTITY % </xsl:text><xsl:value-of select="@name" /><xsl:text>    "</xsl:text>
    <xsl:apply-templates mode="dtdExtension" />
    <xsl:text>"&#x0a;>&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template match="*" mode="nestingOverride">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:define" mode="nestingOverride">
    <xsl:apply-templates mode="#current" />
  </xsl:template>

  <xsl:template match="rng:ref[@name='info-types']" mode="nestingOverride">
    <xsl:variable name="refTarget" select="key('nameIndex',@name)" />
    <xsl:if test="$refTarget">
       <xsl:apply-templates select="$refTarget/rng:*" mode="#current" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="rng:ref[ends-with(@name,'.element')]" mode="nestingOverride">
    <xsl:value-of select="substring-before(@name,'.element')" />
  </xsl:template>

  <xsl:template match="*" mode="attributeOverride">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="attributeOverride">
    <xsl:variable name="domainValue" select="rng:define[@name='domains-atts-value']/rng:value[1]"/>
    <xsl:if test="$domainValue and not($domainValue='')">
      <xsl:variable name="domain" select="substring-before(tokenize($domainValue, ' ')[last()],')')"/>  
      <xsl:variable name="domainAtt" select="concat($domain,'-att')" />
      <xsl:text>&amp;</xsl:text><xsl:value-of select="$domainAtt" /><xsl:text>;&#x0a;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="rng:ref" mode="dtdExtension domainExtension">
    <xsl:text>%</xsl:text><xsl:value-of select="@name" /><xsl:text>; </xsl:text>
    <xsl:if test="not(position()=last())">
      <xsl:text>|</xsl:text>
    </xsl:if>
    <xsl:text>&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template match="rng:empty" mode="dtdExtension">
    <xsl:comment> empty </xsl:comment>
  </xsl:template>

  <xsl:template match="*" mode="localDefinition">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template match="rng:element" mode="localDefinition">
    <xsl:apply-templates select="../rnga:documentation | rnga:documentation" />
    <!-- Element declaration -->
    <xsl:text>&lt;!ELEMENT  </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text>    </xsl:text>
    <xsl:apply-templates select="rng:*[not(ends-with(@name, '.attlist'))]" mode="moduleFile" />
    <xsl:text> &gt;&#x0a;</xsl:text>

    <xsl:if test="rng:ref[ends-with(@name, '.attlist')]">
        <xsl:text>&lt;!ATTLIST  </xsl:text>
        <xsl:value-of select="@name" />
        <xsl:text>    </xsl:text>
        <xsl:variable name="refPointer" select="rng:ref[ends-with(@name, '.attlist')]" />
        <xsl:variable name="refTarget" select="key('nameIndex',$refPointer/@name)" />
        <xsl:choose>
          <xsl:when test="$refTarget">
             <xsl:apply-templates select="$refTarget/rng:*" mode="moduleFile" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="$refPointer" mode="moduleFile" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&gt;&#x0a;&#x0a;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="dtdFile">
    <!-- Most stuff we don't care about -->
  </xsl:template>

  <xsl:template match="rnga:documentation" mode="dtdFile" />

  <xsl:template match="comment()" mode="dtdFile">
    <!-- Suppress comments in dtdFile mode -->
  </xsl:template>


</xsl:stylesheet>