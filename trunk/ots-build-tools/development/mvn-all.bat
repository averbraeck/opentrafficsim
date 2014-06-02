ECHO OFF
REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-all.bat $
REM $Id: mvn-all.bat 832 2013-09-18 14:24:04Z averbraeck $

ECHO Running Maven commands %* for specified Maven artifacts in relative directories

SET _=%CD%
FOR %%A IN (
	dsol-base
	language
	event
	logger 
	naming
	interpreter
	introspection
	jstats
	jstats-charts
	dsol
	dsol-xml
	gisbeans
	dsol-animation
) DO FOR %%B IN (
	%*
) DO CALL "%~dp0mvn-cmd.bat" %%~dp0..\..\%%A %%B

CD %_%
