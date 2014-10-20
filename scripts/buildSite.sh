SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

mvn -Pjenkins clean site:site site:stage |tee /tmp/buildSite.log
mvn versions:display-dependency-updates |tee /tmp/dependency-updates.log
mvn versions:display-plugin-updates |tee /tmp/plugin-updates.log

echo
echo "Site now available in target/staging (log file in /tmp/buidSite.log)"
echo "See /tmp/dependency-updates.log for new dependency versions"
echo "See /tmp/plugin-updates.log for new plugin versions"
