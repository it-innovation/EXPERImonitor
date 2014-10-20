SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

mvn -Pjenkins clean site:site site:stage |tee /tmp/buildSite.log
echo "Site now available in target/staging (log file in /tmp/buidSite.log)"
