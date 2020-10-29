#!/bin/bash
echo install ots

cd /cygdrive/e/java/opentrafficsim/workspace

MODULES="ots ots-base ots-build-tools ots-kpi ots-core ots-road ots-water ots-rail ots-animation ots-draw ots-trafficcontrol ots-swing ots-xsd ots-parser-xml ots-xsd-opendrive ots-parser-opendrive ots-parser-shape ots-xsd-vissim ots-parser-vissim ots-parser-osm ots-parser-nwb ots-web ots-nissan ots-imb ots-demo ots-ntm ots-imb-kpi ots-imb-road ots-multimodal ots-sim0mq ots-sim0mq-kpi ots-sim0mq-road ots-sim0mq-multimodal ots-aimsun-proto ots-aimsun ots-aimsun-demo"

# run mvn on each projecte

for i in $MODULES; do

  cd $i

  mvn clean install
  rc=$?
  if [ $rc -ne 0 ] ; then
    echo mvn clean install for project $i failed with exit code $rc
    cd ..
    exit $rc
  fi 

  cd ..

done
