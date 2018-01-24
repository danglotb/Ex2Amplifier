#!/usr/bin/env bash

wget http://cvc4.cs.stanford.edu/downloads/builds/x86_64-linux-opt/cvc4-1.4-x86_64-linux-opt
{ echo "\ncatg.cvc4Command=" ; echo $(pwd); echo "/" ; echo $(ls | grep cvc4); } | sed ':a;N;s/\n//;ba' >> lib/catg.conf

git clone http://github.com/STAMP-project/dspot.git
cd dspot
mvn install -DskipTests

cd ..