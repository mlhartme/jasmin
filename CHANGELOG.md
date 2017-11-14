## Changelog

### 3.0.13 (2017-11-14)
    
* sushi 3.1.6 with code adaptions
* ssass 1.0.10, slf4j 1.7.25, pustefix 0.22.0, servlet-api 3.1.0
* update parent pom and thus compile for Java 8.


### 3.0.12 (2014-03-20)

* Reference exclusions must not exclude declarations.


### 3.0.11 (2014-09-26)

* Update ssass 1.0.7 to 1.0.8 to fix background-position compression.


### 3.0.10 (2014-09-25)

* Removed debug output.


### 3.0.9 (2014-09-24)

* Update ssass 1.0.6 to 1.0.7 to add keyframes support.


### 3.0.8 (2014-08-07)

* Update closure compiler rr2079.1 to v20140625.
  CAUTION: you have to make sure that your application used guava 1.17 or newer.
  This change should have been a minor version change, but I realized it too late :(


### 3.0.7 (2014-08-07)

* Fixed jasmin comment parsing for files with BOM.


### 3.0.6 (2014-06-13)

* Fixed multi-threading problem in css parsing by updating to mork 1.1.4.
* Fixed deprecated warning for modules with both an js and a css file.
* Removed deprecated warning for /xml/jasmin requests.
* Improved hashCache string representation.
* Update sushi 2.8.7 to 2.8.14.
* Switched to slf4j.
* Allow caching on proxies. 
* Removed browser-whitelisting for gzip.
* Updated ssass 1.0.5 to 1.0.6 to get improved attribute selectors.


### 3.0.5 (2014-04-16)

* Fix services requests: use the protocol specified by initial request (instead of always http).


### 3.0.4 (2014-03-07)

* More path prefixes removed from implicit module names: pustefix and htdocs.
* Support implicit modules in PUSTEFIX-INF directories.


### 3.0.3 (2013-02-25)

* Limit max number of parallel computations.
* Use pominfo.properties instead of wsd.properties.
* Avoid concurrent computation of the same path.


### 3.0.2 (2012-11-09)

* Update dependencies to fix too-many-open-files problem.


### 3.0.1 (2012-10-24)

* Add lost retry for http requests.


### 3.0.0 (2012-10-18)

* Get Requests: type preceeds module now (old ordering is still supported but yields a deprecated warnings);
  tailing variant is optional (defaults to 'lead').
* Servlet mapping /xml/jasmin is deprecated now (and yields a warning when used in get requests), use /jasmin instead.
* Removed siteId parameter.
* Switch from YUI Compressor to Google Closure Compiler.
* Serve gzipped content for all browers with the appropriate header - except IE 6.
* Changed "svn" to "scm" in order to support git. "scm" starts with "scm:svn" for svn repositories,
  and with "scm:git" for git repositories.
* Renamed com.oneandone.jasmin to net.oneandone.jasmin.
* Added Sass processing for all css files. File extension remains .css - there is no .sass or .ssass extension.
* Added "call" comments to replace base LOCALHOST.
* Base LOCALHOST issues a deprecated warning now.
* Pustefix Repository Projects are no longer supported. Base "PROJECT" has been removed.
  The parameters "project" and "projectDescriptor" have been removed.
* Report error for libraries without wsd.properties.
* Update sushi 2.7.0 to 2.8.1.
