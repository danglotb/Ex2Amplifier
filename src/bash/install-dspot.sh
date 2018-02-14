#!/usr/bin/env bash

wget http://cvc4.cs.stanford.edu/downloads/builds/x86_64-linux-opt/cvc4-1.4-x86_64-linux-opt
{ echo "catg.cvc4Command=" ; echo $(pwd); echo "/" ; echo $(ls | grep cvc4); } | sed ':a;N;s/\n//;ba' >> lib/catg.conf
chmod +x cvc4-1.4-x86_64-linux-opt

cat lib/catg.conf

git clone http://github.com/STAMP-project/dspot.git
cd dspot
mvn install -DskipTests

cd ..

git clone http://github.com/danglotb/jbse -b pre-java8
cd jbse
mvn install -DskipTests

cd ..

#mkdir lib
#cd lib
#git clone https://github.com/Z3Prover/z3.git
#cd z3
#python scripts/mk_make.py --prefix=./
#cd build
#make
#sudo make install