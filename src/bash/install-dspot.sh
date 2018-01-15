#!/usr/bin/env bash

git clone http://github.com/STAMP-project/dspot.git
cd dspot
mvn install -DskipTests

cd ..

git clone http://github.com/danglotb/jbse -b pre-java8
cd jbse
mvn install -DskipTests

cd ..

mkdir lib
cd lib
cd z3
git clone https://github.com/Z3Prover/z3.git
python scripts/mk_make.py --prefix=./
cd build
make
sudo make install