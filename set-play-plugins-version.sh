#!/bin/sh

VER=$1
if [ x"$VER" == x"" ]; then
  echo "Usage: set-play-plugins-version.sh <version>"
  exit 1
fi

ORG=me.tfeng.play-plugins

sed -i .bak -E "s/^(.*$ORG.*)[0-9]\\.[0-9]\\.[0-9]-SNAPSHOT(.*)/\1$VER\2/" *-example/build.sbt *-example/project/plugins.sbt \
  && rm *-example/build.sbt.bak *-example/project/plugins.sbt.bak
