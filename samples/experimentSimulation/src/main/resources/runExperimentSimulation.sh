#!/bin/bash
echo "Running Experimedia Experiment Simulator"

#init
declare -a Participants=('alice' 'bob' 'carol' 'david' 'elizabeth' 'frank' 'gemma' 'henry' 'imogen' 'julie')
cd ../../../;

#run all the simulation clients
for n in "${Participants[@]}"; do
	echo "Processing participant $n"
	sleep 1
	mvn "-Dexec.args=-classpath %classpath uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.experimentSimulation.EntryPoint 	"$n".txt" -Dexec.executable=/usr/bin/java -DskipTests=true org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
done


echo "Finished!"
