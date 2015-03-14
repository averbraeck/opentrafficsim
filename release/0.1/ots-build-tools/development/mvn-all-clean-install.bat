@ECHO OFF
REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-all-clean-install.bat $
REM $Id: mvn-all-clean-install.bat 797 2013-07-01 08:41:27Z dvankrevelen $

REM Runs Maven commands 'clean' and 'install' for all Maven artifacts

CALL "%~dp0mvn-all.bat" clean install
PAUSE
