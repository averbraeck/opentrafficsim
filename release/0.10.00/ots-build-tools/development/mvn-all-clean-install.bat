@ECHO OFF
REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-all-clean-install.bat $
REM $Id$

REM Runs Maven commands 'clean' and 'install' for all Maven artifacts

CALL "%~dp0mvn-all.bat" clean install
PAUSE
