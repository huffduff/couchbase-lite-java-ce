#!/bin/bash
#
# Build Couchbase Lite Java for MacOS, Community Edition
#
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../common/tools"

function usage() {
    echo "Usage: $0 <build number>"
    exit 1
}

if [ "$#" -ne 1 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

echo "======== BUILD Couchbase Lite Java for MacOS, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Clean up ..." 
"${TOOLS_DIR}/clean_litecore.sh"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_java_litecore.sh" -e CE

echo "======== Build Java"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || exit 1

echo "======== BUILD COMPLETE"
find lib/build/distributions

