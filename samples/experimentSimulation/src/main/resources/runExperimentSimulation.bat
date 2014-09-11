cls

echo Running Experimedia Experiment Simulator

set participants=(alice.txt bob.txt carol.txt david.txt elizabeth.txt frank.txt gemma.txt henry.txt imogen.txt julie.txt)

cd ..\..\..\

for %%i in %participants% do (

echo Processing participant %%i

mvn exec:java -Dexec.mainClass=uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.experimentSimulation.EntryPoint -Dexec.args=%%i

echo Finished processing client )

cd \src\main\resources
