#!/usr/bin/env bash

sbt run -Dconfig.resource=application.conf -Dapplication.router=testOnlyDoNotUseInAppConf.Routes -Dplay.akka.http.server.request-timeout=60s -J-Xmx256m -J-Xms64m -Dhttp.port=9302 -Drun.mode=Dev
