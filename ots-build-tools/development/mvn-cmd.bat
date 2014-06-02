REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-cmd.bat $
REM $Id: mvn-cmd.bat 832 2013-09-18 14:24:04Z averbraeck $

REM Changes to directory %1 and runs Maven command %2, assuming mvn is in %PATH%

CD /d "%1"
CALL mvn -DdownloadSources=true -DdownloadJavadocs=true %2
if not "%ERRORLEVEL%" == "0" PAUSE  