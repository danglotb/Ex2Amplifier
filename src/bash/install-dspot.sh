#!/usr/bin/env bash

git clone http://github.com/STAMP-project/dspot.git
cd dspot
mvn install -DskipTests

cd ..

git clone http://github.com/danglotb/jbse -b pre-java8
cd jbse
mvn install -DskipTests