#!/bin/bash
echo deploy ots

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
  echo usage: $0 OLD_SNAPSHOT_VERSION DEPLOY_VERSION NEW_SNAPSHOT_VERSION \"svn deploy comment\"
  echo e.g.,: $0 0.12.00-SNAPSHOT 0.12.00 0.13.00-SNAPSHOT \"deployed version 0.12.00\"
  exit
fi

OLDSNAPSHOTVERSION=$1
DEPLOYVERSION=$2
NEWSNAPSHOTVERSION=$3
SVNCOMMENT=$4

cd /cygdrive/e/java/opentrafficsim/workspace

MODULES="ots ots-base ots-build-tools ots-kpi ots-core ots-road ots-water ots-rail ots-animation ots-draw ots-trafficcontrol ots-swing ots-xsd ots-parser-xml ots-xsd-opendrive ots-parser-opendrive ots-parser-shape ots-xsd-vissim ots-parser-vissim ots-parser-osm ots-parser-nwb ots-web ots-nissan ots-imb ots-demo ots-ntm ots-imb-kpi ots-imb-road ots-multimodal ots-sim0mq ots-sim0mq-kpi ots-sim0mq-road ots-sim0mq-multimodal ots-aimsun-proto ots-aimsun ots-aimsun-demo"


# check preconditions

for i in $MODULES; do

  if [ ! -d $i ]; then
    echo folder $i not found...
    exit
  fi

  cd $i
  if [ ! -f pom.xml ]; then
    echo pom.xml not found in folder $i
    exit
  fi


  if ! grep -q "<version>$OLDSNAPSHOTVERSION</version>" pom.xml; then
    echo version $OLDSNAPSHOTVERSION not found in pom.xml in $i
    exit
  fi

  cd ..

done


# modify the POM files

for i in $MODULES; do

  cd $i

  sed -i "s/<version>$OLDSNAPSHOTVERSION<\/version>/<version>$DEPLOYVERSION<\/version>/" pom.xml
  chmod 777 pom.xml 

  cd ..

done


# run mvn on ech project and see whether it works fine

for i in $MODULES; do

  cd $i

  mvn clean install
  rc=$?
  if [ $rc -ne 0 ] ; then
    echo mvn clean install for project $i failed with exit code $rc
    #change back the POM files
    cd ..
    for j in $MODULES; do
      cd $j
      sed -i "s/<version>$DEPLOYVERSION<\/version>/<version>$OLDSNAPSHOTVERSION<\/version>/" pom.xml
      chmod 777 pom.xml
      cd ..
    done 
    exit $rc
  fi 

  cd ..

done


# deploy the sites and see whether it runs fine

for i in $MODULES; do

  cd $i

  if [ $i == "ots-build-tools" ] ; then
    mvn deploy
  else
    mvn deploy site-deploy 
  fi
  rc=$?
  if [ $rc -ne 0 ] ; then
    echo mvn deploy site-deploy for project $i failed with exit code $rc
    #change back the POM files
    cd ..
    for j in $MODULES; do
      cd $j
      sed -i "s/<version>$DEPLOYVERSION<\/version>/<version>$OLDSNAPSHOTVERSION<\/version>/" pom.xml
      chmod 777 pom.xml
      cd ..
    done 
    exit $rc
  fi 

  cd ..

done


# delete the svn version tree if it exists in the release folder

svn delete https://svn.tbm.tudelft.nl/OTS/release/$DEPLOYVERSION -m "delete incomplete release $DEPLOYVERSION"
svn mkdir https://svn.tbm.tudelft.nl/OTS/release/$DEPLOYVERSION -m "$SVNCOMMENT"


# copy multislider (not recompiled or committed)
svn copy -rHEAD https://svn.tbm.tudelft.nl/OTS/trunk/multislider https://svn.tbm.tudelft.nl/OTS/release/$DEPLOYVERSION -m "$SVNCOMMENT"


# write everything to svn

for i in $MODULES; do

  cd $i

  svn commit -m "$SVNCOMMENT" pom.xml
  # svn mkdir https://svn.tbm.tudelft.nl/OTS/release/$DEPLOYVERSION/$i -m "$SVNCOMMENT"
  svn copy -rHEAD https://svn.tbm.tudelft.nl/OTS/trunk/$i https://svn.tbm.tudelft.nl/OTS/release/$DEPLOYVERSION -m "$SVNCOMMENT"

  cd ..

done


# change to new snapshot version and commit to SVN

for i in $MODULES; do

  cd $i

  sed -i "s/<version>$DEPLOYVERSION<\/version>/<version>$NEWSNAPSHOTVERSION<\/version>/" pom.xml
  chmod 777 pom.xml 

  svn commit -m "$SVNCOMMENT" pom.xml

  cd ..

done


echo Finished committing version $DEPLOYVERSION of OTS

