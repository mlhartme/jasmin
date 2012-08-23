<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pfx="http://www.schlund.de/pustefix/core"
  xmlns:cus="http://www.schlund.de/pustefix/customize"
  xmlns:shop="http://www.schlund.de/pustefix/shop"
  xmlns:cxs="http://pustefix.sourceforge.net/properties200401"
  xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
  xmlns:jasmin="java:com.oneandone.jasmin.main.Servlet"
  exclude-result-prefixes="cxs">

  <xsl:template match="shop:load-jasmin-modules">

    <xsl:param name="min" select="@min" />
    <xsl:param name="restrict-to" select="@restrict-to" />
    <xsl:param name="part" select="@part" />
    <xsl:param name="force-modules" select="@force-modules" />
    <xsl:param name="xhr" select="@xhr" />
    <xsl:param name="labjs" select="@labjs" />

    <ixsl:if test="true()">

      <ixsl:variable name="configuration-temp">
        <xsl:call-template name="pfx:include"> 
          <xsl:with-param name="part" select="$part" />
          <xsl:with-param name="noerror" select="'true'" />
          <xsl:with-param name="noedit" select="'true'" />
        </xsl:call-template>
      </ixsl:variable>
      
      <ixsl:variable xmlns:exslt="http://exslt.org/common" name="configuration" select="exslt:node-set($configuration-temp)" />

      <ixsl:variable name="modules">
        <ixsl:variable name="mode" />
        <ixsl:for-each select="$configuration/module">
          <xsl:call-template name="shop:helper-switchmodus" />
        </ixsl:for-each>
      </ixsl:variable>

      <ixsl:variable name="modules-prefix">
        <ixsl:variable name="mode" select="'prefix'" />
        <ixsl:for-each select="$configuration/prefix">
          <xsl:call-template name="shop:helper-switchmodus" />
        </ixsl:for-each>
      </ixsl:variable>

      <ixsl:variable name="modules-all">
        <ixsl:variable name="mode" select="'all'" />
        <ixsl:for-each select="$configuration/module">
          <xsl:call-template name="shop:helper-switchmodus" />
        </ixsl:for-each>
      </ixsl:variable>

      <ixsl:variable name="module-origin">
        <ixsl:choose>
          <ixsl:when test="$__defining_module and $__defining_module != 'WEBAPP'">
            <ixsl:value-of select="$__defining_module"/>
          </ixsl:when>
          <ixsl:otherwise>
            <xsl:value-of select="$artifactId" /> <!-- this has changed, so that navitree fake is not neccessary any more -->
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      
      <ixsl:variable name="modules-dynamic">
        <ixsl:variable name="mode" select="'dynamic'" />
        <ixsl:for-each select="$configuration/module">
          <xsl:call-template name="shop:helper-switchmodus" />
        </ixsl:for-each>
        <ixsl:value-of select="$module-origin" />
        <ixsl:text>-pages-</ixsl:text>
        <ixsl:value-of select="$page"/>
        <ixsl:text>+</ixsl:text>
      </ixsl:variable>

      <ixsl:variable name="modules-suffix">
        <ixsl:variable name="mode" select="'suffix'" />
        <ixsl:for-each select="$configuration/suffix">
          <xsl:call-template name="shop:helper-switchmodus" />
        </ixsl:for-each>
      </ixsl:variable>

      <!-- letztes '+' aus allen Variablen entfernen entfernen -->

      <ixsl:variable name="modulesfinal">
        <ixsl:if test="string-length(substring-before($modules, '+')) > 0">
          <ixsl:value-of select="substring($modules, 0, string-length($modules))" />
        </ixsl:if>
      </ixsl:variable>

      <ixsl:variable name="modulesfinal-prefix">
        <ixsl:value-of select="$modules-prefix" />
      </ixsl:variable>

      <ixsl:variable name="modulesfinal-all">
        <ixsl:if test="string-length(substring-before($modules-all, '+')) > 0">
          <ixsl:value-of select="substring($modules-all, 0, string-length($modules-all))" />
        </ixsl:if>
      </ixsl:variable>

      <ixsl:variable name="modulesfinal-not-all" select="java:replaceAll($modulesfinal-all, '\+', '+!')" xmlns:java="java:java.lang.String" />

      <ixsl:variable name="modulesfinal-dynamic">
        <ixsl:if test="string-length(substring-before($modules-dynamic, '+')) > 0">
          <ixsl:value-of select="substring($modules-dynamic, 0, string-length($modules-dynamic))" />
        </ixsl:if>
      </ixsl:variable>

      <ixsl:variable name="modulesfinal-suffix">
        <ixsl:if test="string-length(substring-before($modules-suffix, '+')) > 0">
          <ixsl:if test="$modulesfinal-dynamic != ''">
            <xsl:text>+</xsl:text>
          </ixsl:if>
          <ixsl:value-of select="substring($modules-suffix, 0, string-length($modules-suffix))" />
        </ixsl:if>
      </ixsl:variable>

      <xsl:variable name="modus">
        <xsl:choose>
          <xsl:when test="$min = 'force'">
            <xsl:text>-min</xsl:text>
          </xsl:when>
          <xsl:when test="not($prohibitEdit = 'no')">
            <xsl:if test="$min = 'true'">
              <xsl:text>-min</xsl:text>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise />
        </xsl:choose>
      </xsl:variable>

      <ixsl:variable name="requestedvariant-raw">
        <ixsl:choose>
          <ixsl:when test="/formresult/@requested-variant and normalize-space(/formresult/@requested-variant) != ''">
            <ixsl:value-of select="/formresult/@requested-variant"/>
          </ixsl:when>
          <ixsl:otherwise>
            <ixsl:text>lead</ixsl:text>
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>

      <!-- special url encoding for AT variant magic in DIY -->
      <!-- ask Tobi Fehrenbach for that -->
      <!-- if it's not encoded, CDS throws lots of exceptions like this one:
           cannot rewrite invalid URI '/xml/jasmin/get/... -->

      <ixsl:variable name="requestedvariant">
        <ixsl:choose>
          <ixsl:when test="contains($requestedvariant-raw, '[') or contains($requestedvariant-raw, ']')">
            <ixsl:value-of select="substring-before($requestedvariant-raw, '[')" />
            <ixsl:text>%5B</ixsl:text>
            <ixsl:value-of select="substring-after(substring-before($requestedvariant-raw, ']'), '[')" />
            <ixsl:text>%5D</ixsl:text>
            <ixsl:value-of select="substring-after($requestedvariant-raw, ']')" />
          </ixsl:when>
          <ixsl:otherwise>
            <ixsl:value-of select="$requestedvariant-raw" />
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>

      <ixsl:variable name="path-prefix">
        <ixsl:value-of select="$__contextpath" />
        <ixsl:text>/xml/jasmin/get/</ixsl:text>
        <ixsl:value-of select="jasmin:getVmStartup()" />
        <ixsl:text>/</ixsl:text>
      </ixsl:variable>

      <xsl:if test="$labjs = 'true'">
        <!-- LABjs script loading has to be placed in <pfx:script> -->
        <xsl:if test="not($restrict-to = 'css')">
          <xsl:choose>
            <xsl:when test="$force-modules != ''">
              <xsl:text>$LAB.script('</xsl:text><ixsl:value-of select="$path-prefix" /><xsl:value-of select="$force-modules" /><xsl:text>/js</xsl:text><xsl:value-of select="$modus" /><xsl:text>/</xsl:text><ixsl:value-of select="$requestedvariant" /><xsl:text>').wait();</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <!-- loading LABjs scripts is only working when there are modules for all pages -->
              <ixsl:if test="$modulesfinal-all != ''">
                <xsl:text>$LAB.script('</xsl:text><ixsl:value-of select="$path-prefix" /><ixsl:value-of select="$modulesfinal-prefix" /><ixsl:value-of select="$modulesfinal-all" /><xsl:text>/js</xsl:text><xsl:value-of select="$modus" /><xsl:text>/</xsl:text><ixsl:value-of select="$requestedvariant" /><xsl:text>').wait()</xsl:text>
              </ixsl:if>
              <ixsl:choose>
                <ixsl:when test="$modulesfinal-dynamic = ''">
                  <xsl:text>.script('</xsl:text><ixsl:value-of select="$path-prefix" /><ixsl:value-of select="$modulesfinal-suffix" /><xsl:text>/js</xsl:text><xsl:value-of select="$modus" /><xsl:text>/</xsl:text><ixsl:value-of select="$requestedvariant" /><xsl:text>').wait()</xsl:text>
                </ixsl:when>
                <ixsl:when test="$modulesfinal-not-all != ''">
                  <xsl:text>.script('</xsl:text><ixsl:value-of select="$path-prefix" /><ixsl:value-of select="$modulesfinal-dynamic" /><xsl:text>+!</xsl:text><ixsl:value-of select="$modulesfinal-not-all" /><ixsl:value-of select="$modulesfinal-suffix" /><xsl:text>/js</xsl:text><xsl:value-of select="$modus" /><xsl:text>/</xsl:text><ixsl:value-of select="$requestedvariant" /><xsl:text>').wait()</xsl:text>
                </ixsl:when>
                <ixsl:otherwise>
                  <xsl:text>.script('</xsl:text><ixsl:value-of select="$path-prefix" /><ixsl:value-of select="$modulesfinal-prefix" /><ixsl:value-of select="$modulesfinal-dynamic" /><ixsl:value-of select="$modulesfinal-suffix" /><xsl:text>/js</xsl:text><xsl:value-of select="$modus" /><xsl:text>/</xsl:text><ixsl:value-of select="$requestedvariant" /><xsl:text>').wait()</xsl:text>
                </ixsl:otherwise>
              </ixsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
        <xsl:if test="not($restrict-to = 'javascript')">
          <xsl:choose>
            <xsl:when test="$force-modules != ''">
              <link type="text/css" href="{{$path-prefix}}{$force-modules}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
            </xsl:when>
            <xsl:otherwise>
              <ixsl:if test="$modulesfinal-all != ''">
                <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-all}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
              </ixsl:if>
              <ixsl:choose>
                <ixsl:when test="$modulesfinal-dynamic = ''">
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </ixsl:when>
                <ixsl:when test="$modulesfinal-not-all != ''">
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-dynamic}}+!{{$modulesfinal-not-all}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </ixsl:when>
                <ixsl:otherwise>
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-dynamic}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </ixsl:otherwise>
              </ixsl:choose>
            </xsl:otherwise>
          </xsl:choose>        
        </xsl:if>
      </xsl:if>

      <xsl:if test="not($xhr = 'true' or $labjs = 'true')">
        <xsl:choose>
          <xsl:when test="$force-modules != ''">
            <xsl:if test="not($restrict-to = 'css')">
              <script type="text/javascript" src="{{$path-prefix}}{$force-modules}/js{$modus}/{{$requestedvariant}}"></script>
            </xsl:if>
            <xsl:if test="not($restrict-to = 'javascript')">
              <link type="text/css" href="{{$path-prefix}}{$force-modules}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <ixsl:if test="$modulesfinal-all != ''">
              <xsl:if test="not($restrict-to = 'css')">
                <script type="text/javascript" src="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-all}}/js{$modus}/{{$requestedvariant}}"></script>
              </xsl:if>
              <xsl:if test="not($restrict-to = 'javascript')">
                <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-all}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
              </xsl:if>
            </ixsl:if>
            <ixsl:choose>
              <ixsl:when test="$modulesfinal-dynamic = ''">
                <xsl:if test="not($restrict-to = 'css')">
                  <script type="text/javascript" src="{{$path-prefix}}{{$modulesfinal-suffix}}/js{$modus}/{{$requestedvariant}}"></script>
                </xsl:if>
                <xsl:if test="not($restrict-to = 'javascript')">
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </xsl:if>
              </ixsl:when>
              <ixsl:when test="$modulesfinal-not-all != ''">
                <xsl:if test="not($restrict-to = 'css')">
                  <script type="text/javascript" src="{{$path-prefix}}{{$modulesfinal-dynamic}}+!{{$modulesfinal-not-all}}{{$modulesfinal-suffix}}/js{$modus}/{{$requestedvariant}}"></script>
                </xsl:if>
                <xsl:if test="not($restrict-to = 'javascript')">
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-dynamic}}+!{{$modulesfinal-not-all}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </xsl:if>
              </ixsl:when>
              <ixsl:otherwise>
                <xsl:if test="not($restrict-to = 'css')">
                  <script type="text/javascript" src="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-dynamic}}{{$modulesfinal-suffix}}/js{$modus}/{{$requestedvariant}}"></script>
                </xsl:if>
                <xsl:if test="not($restrict-to = 'javascript')">
                  <link type="text/css" href="{{$path-prefix}}{{$modulesfinal-prefix}}{{$modulesfinal-dynamic}}{{$modulesfinal-suffix}}/css{$modus}/{{$requestedvariant}}" rel="stylesheet"></link>
                </xsl:if>
              </ixsl:otherwise>
            </ixsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

    </ixsl:if>

  </xsl:template>

  <xsl:template name="shop:helper-switchmodus">
    
    <ixsl:if test="not(./pages/exclude-page[. = $page])">
      <ixsl:if test="($mode = 'prefix' or $mode = 'suffix' or (not($mode) and ./pages/page[. = $page or . = 'all']) or ($mode = 'all' and ./pages/page[. = 'all']) or ($mode = 'dynamic' and ./pages/page[. = $page]))">
        <xsl:if test="$prohibitEdit = 'no'">
          <ixsl:for-each select="./stage/module">
            <ixsl:value-of select="." />
            <ixsl:text>+</ixsl:text>
          </ixsl:for-each>
        </xsl:if>
        <xsl:if test="not($prohibitEdit = 'no')">
          <ixsl:for-each select="./live/module">
            <ixsl:value-of select="." />
            <ixsl:text>+</ixsl:text>
          </ixsl:for-each>
        </xsl:if>
        <ixsl:for-each select="./all/module">
          <ixsl:value-of select="." />
          <ixsl:text>+</ixsl:text>
        </ixsl:for-each>
      </ixsl:if>
    </ixsl:if>
    
  </xsl:template>

  <xsl:template match="shop:conditional-comment">
    <xsl:param name="if" select="@if" />
    <ixsl:text disable-output-escaping="yes">&lt;!--[if <xsl:value-of select="$if" />]&gt;</ixsl:text>
    <xsl:apply-templates />
    <ixsl:text disable-output-escaping="yes">&lt;![endif]--&gt;</ixsl:text>
  </xsl:template>

</xsl:stylesheet>