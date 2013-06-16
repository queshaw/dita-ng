<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:rng="http://relaxng.org/ns/structure/1.0"
  xmlns:rnga="http://relaxng.org/ns/compatibility/annotations/1.0"
  xmlns:rng2ditadtd="http://dita.org/rng2ditadtd"
  xmlns:relpath="http://dita2indesign/functions/relpath"
  xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0"
  exclude-result-prefixes="xs xd rng rnga relpath a"
  version="2.0"  >

  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>RNG to DITA DTD Converter</xd:p>
      <xd:p><xd:b>Created on:</xd:b> Feb 16, 2013</xd:p>
      <xd:p><xd:b>Authors:</xd:b> ekimber, pleblanc</xd:p>
      <xd:p>This transform takes as input RNG-format DITA document type
      shells and produces from them vocabulary module file
        that reflect the RNG definitions and conform to the DITA 1.3
        DTD coding requirements.
      </xd:p>
    </xd:desc>
  </xd:doc>

<xsl:key name="nameIndex" match="rng:define" use="@name" />
<xsl:key name="attlistIndex" match="rng:element" use="rng:ref[ends-with(@name, '.attlist')]/@name" />


  <!-- ==============================
       .mod file generation mode
       ============================== -->

  <xsl:template match="/" mode="moduleFile">
    <xsl:param name="thisFile" tunnel="yes" as="xs:string" />
    <xsl:apply-templates mode="#current" />
  </xsl:template>

  <xsl:template match="rng:grammar" mode="moduleFile">
    <xsl:param name="thisFile" tunnel="yes" as="xs:string" />
    <xsl:variable name="thisBasename" select="relpath:getNamePart($thisFile)" />
    <xsl:variable name="thisDomain" select="normalize-space(substring-before(substring-after(../comment()[1],'MODULE:'),'VERSION:'))" />
    <xsl:variable name="thisVersion" select="normalize-space(substring-before(substring-after(../comment()[1],'VERSION:'),'DATE:'))" />
    <xsl:variable name="thisDate" select="normalize-space(substring-before(substring-after(../comment()[1],'DATE:'),'='))" />
    <xsl:variable name="thisUri" select="namespace-uri(.)" />
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
&lt;!--  MODULE:    </xsl:text><xsl:value-of select="$thisDomain" /><xsl:text>                    -->
&lt;!--  VERSION:   </xsl:text><xsl:value-of select="$thisVersion" /><xsl:text>                                               -->
&lt;!--  DATE:      </xsl:text><xsl:value-of select="$thisDate" /><xsl:text>                                     -->
&lt;!--                                                               -->
&lt;!-- ============================================================= -->

<!-- FIXME: Get the comments from the RNG file. Can't assume OASIS module.
  -->
&lt;!-- ============================================================= -->
&lt;!--                   ELEMENT NAME ENTITIES                       -->
&lt;!-- ============================================================= -->

</xsl:text>

  <!-- @TODO: catch the ditaArch declaration if any -->

<!-- This declaration needs to be in the shell. The entity name
     needs to reflect the module name, e.g. xml-d-def
      
      <xsl:text>
&lt;!ENTITY % </xsl:text><xsl:sequence select="concat($domainPrefix, '-def')" /><xsl:text> 
  PUBLIC "-//OASIS//ENTITIES </xsl:text><xsl:value-of select="$thisDomain" /><xsl:text>//EN" 
         "</xsl:text><xsl:value-of select="$thisBasename" /><xsl:text>.ent" 
>
%definitions;

</xsl:text>
-->    <xsl:apply-templates mode="#current">
      <xsl:with-param name="domainPfx" select="$domainPrefix" tunnel="yes" as="xs:string" />
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="rng:define" mode="moduleFile" priority="10">
    <xsl:param name="domainPfx" tunnel="yes" as="xs:string" />
    <xsl:choose>
      <xsl:when test="@name='domains-atts-value'">
        <!-- in the entity file -->
      </xsl:when>
      <xsl:when test="$domainPfx and not($domainPfx='') and starts-with(@name, $domainPfx)">
        <!--  in the entity file -->
      </xsl:when>
      <xsl:when test="@combine = 'choice'">
        <!-- Domain integration entity. Not output in the DTD. -->
      </xsl:when>
      <xsl:when test="rng:element">
        <!--  element declaration -->
        <xsl:apply-templates mode="#current" />
      </xsl:when>
      <xsl:when test=".//rng:attribute[@name='class']" >
        <!-- specialization attribute declaration -->
        <xsl:text>&lt;!ATTLIST  </xsl:text>
        <xsl:value-of select="key('attlistIndex',@name)/@name" />
        <xsl:text> %global-atts;  class CDATA </xsl:text>
        <xsl:sequence select="concat('&quot;', string(./rng:optional/rng:attribute/@a:defaultValue), '&quot;')"/>
        <xsl:text> >&#x0a;</xsl:text>
      </xsl:when>
      <xsl:when test="count(rng:*)=1 and rng:ref and key('attlistIndex',@name)" >
        <!-- .attlist pointing to .attributes, ignore -->
        <xsl:if test="$doDebug">
          <xsl:message>Ignore attlist <xsl:value-of select="@name"/></xsl:message>
        </xsl:if>
      </xsl:when>
      <xsl:when test="count(rng:*)=1 and rng:ref and key('nameIndex',rng:ref/@name)/rng:element" >
        <!-- reference to element name in this module, will be in the entity file -->
        <xsl:if test="$doDebug">
          <xsl:message>Ignore name in this module <xsl:value-of select="@name"/></xsl:message>
        </xsl:if>
      </xsl:when>
      <xsl:when test="count(rng:*)=1 and rng:ref and not(key('nameIndex',rng:ref/@name)) and ends-with(rng:ref/@name, '.element')" >
        <!-- reference to element name in another module, will be in entity file -->
        <xsl:if test="$doDebug">
          <xsl:message>Ignore name in other module <xsl:value-of select="@name"/></xsl:message>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- parameter entity -->
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
        <xsl:apply-templates mode="#current" />
        <xsl:if test="count(rng:*) &gt; 1 and (rng:zeroOrMore or rng:oneOrMore)">
          <xsl:text>)</xsl:text>
        </xsl:if>
         <xsl:text>&quot; </xsl:text>
        <xsl:if test="count(rng:*) &gt; 1 or not(rng:ref)">
          <xsl:text>&#x0a;</xsl:text>
        </xsl:if>
        <xsl:text>&gt;&#x0a;&#x0a;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:zeroOrMore" mode="moduleFile" priority="10">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates mode="#current" />
    <xsl:text>)*</xsl:text>
    <xsl:if test="not(position()=last())"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="rng:oneOrMore" mode="moduleFile" priority="10">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates mode="#current" />
    <xsl:text>)+</xsl:text>
    <xsl:if test="not(position()=last())"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="rng:group" mode="moduleFile">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates mode="#current" />
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="rng:choice" mode="moduleFile" priority="10">
    <xsl:if test="local-name(..)='attribute'">
      <xsl:text>(</xsl:text>
    </xsl:if>
    <xsl:for-each select="rng:*">
      <xsl:if test="not(position()=1)"><xsl:text> |&#x0a;</xsl:text></xsl:if>
      <xsl:apply-templates select="." mode="#current" />
    </xsl:for-each>
    <xsl:if test="local-name(..)='attribute'">
      <xsl:text>)</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="rng:ref" mode="moduleFile" priority="10">
    <xsl:choose>
      <xsl:when test="@name='any'">
        <xsl:text>ANY </xsl:text>
      </xsl:when>
      <xsl:when test="not(node()) and key('nameIndex',@name)/rng:element" >
        <!-- reference to element name -->
        <xsl:value-of select="key('nameIndex',@name)/rng:element/@name" />
      </xsl:when>
      <xsl:when test="not(node()) and not(key('nameIndex',@name)) and ends-with(@name, '.element')" >
        <!-- reference to element name in another module -->
        <xsl:value-of select="substring-before(@name,'.element')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="count(../rng:*) &gt; 1 and (../rng:zeroOrMore or ../rng:oneOrMore)">
          <xsl:text>(</xsl:text>
        </xsl:if>
        <xsl:text>%</xsl:text><xsl:value-of select="@name" /><xsl:text>; </xsl:text>
        <xsl:if test="count(../rng:*) &gt; 1 and (../rng:zeroOrMore or ../rng:oneOrMore)">
          <xsl:text>)</xsl:text>
          <xsl:if test="not(position()=last())">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="not(ancestor::rng:choice or ancestor::rng:element) and following-sibling::rng:*"><xsl:text>&#x0a;</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="rng:text" mode="moduleFile" priority="10">
    <xsl:text>#PCDATA</xsl:text>
  </xsl:template>

  <xsl:template match="rng:value" mode="moduleFile" priority="10">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template match="rng:data" mode="moduleFile" priority="10">
    <xsl:choose>
      <xsl:when test="@type='string'">
        <xsl:value-of select="'CDATA'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@type" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:optional" mode="moduleFile" priority="10">
    <xsl:choose>
      <xsl:when test="ancestor::rng:element or ends-with(ancestor::rng:define/@name, '.content')">
        <!-- optional element content -->
        <xsl:text>(</xsl:text>
        <xsl:apply-templates mode="#current" />
        <xsl:text>)?</xsl:text>
        <xsl:if test="not(position()=last())">
          <xsl:text>,</xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- optional attribute value -->
        <xsl:apply-templates mode="#current" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:empty" mode="moduleFile" priority="10">
    <xsl:choose>
      <xsl:when test="ancestor::rng:element">
        <!-- empty element content -->
        <xsl:text>EMPTY</xsl:text>
      </xsl:when>
      <xsl:when test="ends-with(ancestor::rng:define/@name, '.content')">
        <!-- empty element content in parameter entity -->
        <xsl:text>EMPTY</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <!-- empty attribute value -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:attribute" mode="moduleFile" priority="10">
    <xsl:variable name="attributePos">
      <xsl:number level="any" from="rng:define" />
    </xsl:variable>
    <xsl:if test="not($attributePos=1)"><xsl:text>&#x0a;</xsl:text></xsl:if>
    <xsl:value-of select="@name" />
    <xsl:text>&#x0a;                       </xsl:text>
    <xsl:choose>
      <xsl:when test="not(node())">
        <xsl:text>CDATA</xsl:text>
      </xsl:when>
      <xsl:when test="count(node())=1 and rng:value">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates mode="#current" />
        <xsl:text>)</xsl:text>
        <xsl:if test="@rnga:defaultValue"><xsl:text>  #FIXED  </xsl:text></xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="#current" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="@rnga:defaultValue">
        <xsl:text>&#x0a;                             '</xsl:text>
        <xsl:value-of select="@rnga:defaultValue" />
        <xsl:text>'</xsl:text>
      </xsl:when>
      <xsl:when test="local-name(..)='optional'">
        <xsl:text>&#x0a;                             #IMPLIED</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&#x0a;                             #REQUIRED</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template match="rng:element" mode="moduleFile" priority="10">
    <xsl:apply-templates select="rnga:documentation" />
    <!-- Element declaration -->
    <xsl:text>&lt;!ELEMENT  </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text>    </xsl:text>
    <xsl:apply-templates select="rng:ref[not(ends-with(@name, '.attlist'))]" mode="#current" />
    <xsl:text>&gt;&#x0a;</xsl:text>

    <xsl:if test="rng:ref[ends-with(@name, '.attlist')]">
        <xsl:text>&lt;!ATTLIST  </xsl:text>
        <xsl:value-of select="@name" />
        <xsl:text>    </xsl:text>
        <xsl:variable name="refTarget" select="key('nameIndex',rng:ref[ends-with(@name, '.attlist')]/@name)[count(rng:*)=1]" />
        <xsl:choose>
          <xsl:when test="$refTarget/rng:ref">
             <!-- Simplify indirect references -->
             <xsl:apply-templates select="$refTarget/rng:ref" mode="#current" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="rng:ref[ends-with(@name, '.attlist')]" mode="#current" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&gt;&#x0a;&#x0a;</xsl:text>
    </xsl:if>
  </xsl:template>

<!-- others -->
  <xsl:template match="rng:include" mode="moduleFile">
    <xsl:variable name="includedDoc" select="document(@href)" />
    <xsl:if test="$includedDoc/rng:grammar">
      <xsl:variable name="includedDomain" select="normalize-space(substring-before(substring-after($includedDoc/comment()[1],'MODULE:'),'VERSION:'))" />
      <!-- FIXME: Need to parameterize the public ID. See Issue 8. -->
      <xsl:text>
  &lt;!ENTITY % </xsl:text><xsl:value-of select="substring-before(@href,'.')" /><xsl:text>
     PUBLIC "-//OASIS//ELEMENTS </xsl:text><xsl:value-of select="$includedDomain" /><xsl:text>//EN"
     "</xsl:text><xsl:value-of select="substring-before(@href,'.rng')" /><xsl:text>" 
&gt;%</xsl:text><xsl:value-of select="substring-before(@href,'.')" /><xsl:text>;

</xsl:text>
    </xsl:if>
  </xsl:template>
  

  <xsl:template match="comment()" mode="moduleFile" >
    <xsl:choose>
      <xsl:when test="not(following::rng:grammar)">
        <xsl:text>&lt;!-- </xsl:text><xsl:sequence select="translate(.,'&lt;&gt;','')"/><xsl:text> -->&#x0a;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <!-- Suppress comments in moduleFile mode -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:attribute//rnga:documentation" mode="moduleFile" />

  <xsl:template match="rnga:documentation" mode="moduleFile">
    <xsl:text>&lt;!-- </xsl:text><xsl:sequence select="translate(.,'&lt;&gt;','')"/><xsl:text> -->&#x0a;</xsl:text>
  </xsl:template>

</xsl:stylesheet>