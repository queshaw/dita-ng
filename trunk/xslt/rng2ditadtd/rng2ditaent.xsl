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
      shells and produces from them the entity file
        that reflect the RNG definitions and conform to the DITA 1.3
        DTD coding requirements.
      </xd:p>
    </xd:desc>
  </xd:doc>

  <!-- ==============================
       .ent file generation mode
       ============================== -->

  <xsl:template mode="entityFile" match="/">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template mode="entityFile" match="rng:grammar">
    <xsl:param name="thisFile" tunnel="yes" as="xs:string" />
    <xsl:variable name="thisDomain" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:variable name="thisVersion" select="normalize-space(substring-before(substring-after(../comment()[1],'VERSION:'),'DATE:'))" />
    <xsl:variable name="thisDate" select="normalize-space(substring-before(substring-after(../comment()[1],'DATE:'),'='))" />
    <xsl:variable name="domainValue" select="rng:define[@name='domains-atts-value']/rng:value[1]"/>
    <xsl:variable name="domainPrefix"> 
      <xsl:if test="$domainValue and not($domainValue='')">
        <xsl:value-of select="concat(substring-before(tokenize($domainValue, ' ')[last()],')'),'-')" />
      </xsl:if>  
    </xsl:variable>

    <xsl:text>&lt;?xml version="1.0" encoding="UTF-8"?>
&lt;!-- ============================================================= -->
&lt;!--                    HEADER                                     -->
&lt;!-- ============================================================= -->
&lt;!--  MODULE:    </xsl:text><xsl:value-of select="$thisDomain" /><xsl:text> Entities                   -->
&lt;!--  VERSION:   </xsl:text><xsl:value-of select="$thisVersion" /><xsl:text>                                               -->
&lt;!--  DATE:      </xsl:text><xsl:value-of select="$thisDate" /><xsl:text>                                     -->
&lt;!--                                                               -->
&lt;!-- ============================================================= -->

&lt;!-- ============================================================= -->
&lt;!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
&lt;!--                    TYPICAL INVOCATION                         -->
&lt;!--                                                               -->
&lt;!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ENTITIES </xsl:text><xsl:value-of select="$thisDomain" /><xsl:text>//EN"
      Delivered as file "</xsl:text><xsl:value-of select="relpath:getName($thisFile)" /><xsl:text>"               -->

&lt;!-- ============================================================= -->
&lt;!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
&lt;!--                                                               -->
&lt;!-- PURPOSE:    Declaring the substitution context and domain     -->
&lt;!--             entity declarations for the </xsl:text><xsl:value-of select="substring-after($thisDomain,'DITA ')" /><xsl:text>    -->
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
&lt;!--            ELEMENT EXTENSION ENTITY DECLARATIONS              -->
&lt;!-- ============================================================= -->
</xsl:text>
    <xsl:if test="$domainPrefix and not($domainPrefix='') and //rng:define[starts-with(@name, $domainPrefix)]">
      <xsl:apply-templates select="//rng:define[starts-with(@name, $domainPrefix)]" mode="entityExtension" />
    </xsl:if>

<xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    DOMAIN ENTITIES                            -->
&lt;!-- ============================================================= -->
</xsl:text>
    <xsl:apply-templates mode="#current">
      <xsl:with-param name="domainPfx" select="$domainPrefix" tunnel="yes" as="xs:string" />
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="rng:define" mode="entityFile" priority="10">
    <xsl:param name="domainPfx" tunnel="yes" as="xs:string" />
    <xsl:choose>
      <xsl:when test="(@name='domains-atts-value' or @name='domains-atts') and not(rng:value='')">
        <xsl:text>&lt;!ENTITY </xsl:text>
        <xsl:choose>
          <xsl:when test="$domainPfx and not($domainPfx='')">
            <xsl:value-of select="concat($domainPfx,'att')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>    &quot;</xsl:text>
        <xsl:sequence select="rng:value" />
        <xsl:text>&quot;&gt;&#x0a;&#x0a;</xsl:text>
      </xsl:when>
      <xsl:when test="$domainPfx and not($domainPfx='') and starts-with(@name, $domainPfx)">
        <!--  already processed as extension, ignore -->
      </xsl:when>
      <xsl:when test="count(rng:*)=1 and rng:ref and key('nameIndex',rng:ref/@name)/rng:element" >
        <!-- reference to element name in this module -->
        <xsl:text>&lt;!ENTITY % </xsl:text>
        <xsl:sequence select="string(@name)" />
        <xsl:text> &quot;</xsl:text>
        <xsl:apply-templates mode="moduleFile" />
        <xsl:text>&quot; &gt;&#x0a;&#x0a;</xsl:text>
      </xsl:when>
      <xsl:when test="count(rng:*)=1 and rng:ref and not(key('nameIndex',rng:ref/@name)) and ends-with(rng:ref/@name, '.element')" >
        <!-- reference to element name in another module -->
        <xsl:text>&lt;!ENTITY % </xsl:text>
        <xsl:sequence select="string(@name)" />
        <xsl:text> &quot;</xsl:text>
        <xsl:value-of select="substring-before(rng:ref/@name,'.element')" />
        <xsl:text>&quot; &gt;&#x0a;&#x0a;</xsl:text>
      </xsl:when>
      <xsl:when test="rng:element">
        <!--  element declaration -->
      </xsl:when>
      <xsl:when test=".//rng:attribute[@name='class']" >
        <!-- specialization attribute declaration -->
      </xsl:when>
      <xsl:otherwise>
        <!-- not in entity file -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:define" mode="entityExtension">
    <xsl:text>&lt;!ENTITY % </xsl:text>
    <xsl:sequence select="string(@name)" />
    <xsl:text> </xsl:text>
    <xsl:if test="count(rng:*) &gt; 1 or not(rng:ref)">
      <xsl:text>&#x0a;</xsl:text>
    </xsl:if>
    <xsl:text>&quot;</xsl:text>
    <xsl:if test="count(rng:*) &gt; 1 and (rng:zeroOrMore or rng:oneOrMore)">
      <xsl:text>(</xsl:text>
    </xsl:if>
    <xsl:apply-templates mode="moduleFile" />
    <xsl:if test="count(rng:*) &gt; 1 and (rng:zeroOrMore or rng:oneOrMore)">
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:text>&quot; </xsl:text>
    <xsl:if test="count(rng:*) &gt; 1 or not(rng:ref)">
      <xsl:text>&#x0a;</xsl:text>
    </xsl:if>
    <xsl:text>&gt;&#x0a;&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template match="*" mode="entityFile">
    <!-- Most stuff we don't care about -->
  </xsl:template>

  <xsl:template match="rnga:documentation" mode="entityFile" />

  <xsl:template match="comment()" mode="entityFile">
    <!-- Suppress comments in entityFile mode -->
  </xsl:template>


</xsl:stylesheet>