ECHO OFF
REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-all-install.bat $
REM $Id$

REM Runs Maven commands 'eclipse:clean', 'eclipse:eclipse' and 'install' for all Maven artifacts

CALL "%~dp0mvn-all.bat" install
PAUSE