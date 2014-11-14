SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

mvn --batch-mode -Pjenkins clean site:site site:stage |tee /tmp/buildSite.log
mvn --batch-mode versions:display-dependency-updates |tee /tmp/dependency-updates.log
mvn --batch-mode versions:display-plugin-updates |tee /tmp/plugin-updates.log

echo
echo "Site now available in target/staging (log file in /tmp/buildSite.log)"
echo "See /tmp/dependency-updates.log for new dependency versions"
echo "See /tmp/plugin-updates.log for new plugin versions"
