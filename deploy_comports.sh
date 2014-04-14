#!/bin/bash

beginPath=/dev/ttyS
endPath=00

sudo ln -s /dev/pts/$1 $beginPath$1$endPath
sudo ln -s /dev/pts/$2 $beginPath$2$endPath