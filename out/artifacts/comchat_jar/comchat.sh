#!/bin/bash

JAVA_HOME="/usr/bin"
SYSTEM_BIT=$(getconf LONG_BIT)
lib_path=

if [ $SYSTEM_BIT == 32 ]; then
	lib_path="libs/x86"
fi

if [ $SYSTEM_BIT == 64 ]; then
	lib_path="libs/x64"
fi

$JAVA_HOME/java -jar -Djava.library.path=$lib_path comchat.jar