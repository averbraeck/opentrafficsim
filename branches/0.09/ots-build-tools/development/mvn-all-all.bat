REM $HeadURL: https://svn.tbm.tudelft.nl/TBM-SK/DSOL-AOS/dsol-aos/development/mvn-all-deploy.bat $
REM $Id$

REM Runs Maven commands 'eclipse:eclipse' and 'deploy' for all Maven artifacts
REM -Dmaven.wagon.http.ssl.allowall=true 

CALL "%~dp0mvn-all.bat" clean install deploy site-deploy
PAUSE
