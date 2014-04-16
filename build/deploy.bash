#!/bin/bash

deploy_location="/var/www/be.0110/current/public/releases/TarsosTranscoder"


ant release

#find the current version:
filename=$(basename TarsosTranscoder-*-bin.jar)
version=${filename:17:3}

#create the readme file from textile
textile2html ../README.textile TarsosTranscoder-$version-Readme.html


scp -r TarsosTranscoder-* joren@0110.be:$deploy_location


