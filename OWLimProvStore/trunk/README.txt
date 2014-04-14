                      +--------------------------+
                      |  OWLim Prov Store README |
                      +--------------------------+

This software has been build for OWLim 5.4 - any earlier version is not likely
to work as there were some significant changes to the API from version 5.3.

The current implementation only supports accessing owlim via Sesame deployed
on Tomcat - instructions omitted here.


DEPENDENCIES AND BUILD PROCESS
------------------------------

This software requires maven 3.x to build and you need to install owlim as a
local jar manually (due to licensing, it is not available in a repository).

Provided you have the owlim jar (register to download online [1]) type
the following on the command line:

	mvn install:install-file -Dfile=owlim-lite-5.4.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.4 -Dpackaging=jar

Once this is done, all other dependencies are downloaded automatically when you 
compile the code (open command line in the trunk):

	mvn install

There's a test class, which you can run (from Netbeans, for example), which
creates a test repository, adds some ontologies and triples, and runs a few
queries.


To deal with triples of different relationship types, each triple must define
the type - mainly to be able to deal with data properties (literals) properly.
The following assumptions are made:

1: all triples specify the relationship type, if not an exception is thrown
   and none of the triples in the collection to be added are stored

2: we assume that the literals are within double quotes

3: we assume that the data source type for a literal is given by appending 
   ^^xsd:<datasource> to the string, e.g., "Bobbert banana"^^xsd:string,
   or "2013-01-21T16:15:33"^^xsd:dateTime

4: we assume that if no data source is given, it is a string


MAVEN REPOSITORY
----------------

The project is set up to be deployable to a Maven repo here IT Innovation:

	http://altano.it-innovation.soton.ac.uk/maven2/release/edmprov

To deploy the software, your user account needs to exist on altano, having
uploaded your public key. Andrew Thicker deals with this. Also, you will need
to have access to the repository itself, which is done by Panos Melas.

If access is set up and your private key is loaded in your system, e.g., via
Pageant on Windows, just issue the following command to deploy:

	mvn deploy


To pull the jar files from the repository from another project, without
having the build the software as described above, there's a bit of configuration
required as it’s constrained by username/password authentication.

I’d recommend the following to avoid the username and password to go in the pom
file(s) of your code.

In your Maven settings.xml (living in your .m2 directory, depending on your Maven
config), add a server entry:

<servers>
   <!-- IT Innovation Maven EDM Prov Repository :: configuration for downloading -->
   <server>
      <id>itinnovation-edmprov</id>
      <username>username</username>
     <password>password</password>
   </server>          
</servers>

The username and password are not your personal credentials. Speak with Panos Melas
or Vegard Engen about the credentials you can use.

In your pom.xml file(s), where required, add a repository entry:

<repositories>
   <repository>
      <id>itinnovation-edmprov</id>
      <url>http://altano.it-innovation.soton.ac.uk/maven2/release/edmprov</url>
      <snapshots>
         <enabled>true</enabled>
      </snapshots>
   <repository>
</repositories>

And of course the dependency on the jar itself:

<dependency>
   <groupId>uk.ac.soton.itinnovation.edmprov</groupId>
   <artifactId>OWLimProvStore</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>


[1] http://www.ontotext.com/owlim