SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

cd samples/lwtECCClient/src/main/resources/
./run.sh
cd -
cd samples/experimentSimulation/src/main/resources/
./runExperimentSimulation.sh |tee /tmp/prov.log
