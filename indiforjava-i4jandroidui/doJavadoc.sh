#! /bin/bash

find ./src/ -name *.java > files.txt

javadoc @./javadocOptions -J-Xbootclasspath/p:/usr/lib/jvm/java-6-sun/jre/lib/javaws.jar @./files.txt
