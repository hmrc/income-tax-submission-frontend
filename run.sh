#!/usr/bin/env bash

sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes -mem 4096 run
