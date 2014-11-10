Instructions for deploying to public altano maven repository
============================================================

Windows users
-------------

The folder with PuTTY in must be on your path (e.g. "C:\Program Files (x86)\PuTTY").
Maven must be on your path (e.g. "%MAVEN_HOME%\bin" with MAVEN_HOME set to e.g. "C:\Program Files\NetBeans 8.0\java\maven").
You must be running Pageant and have loaded your private key.
You must have the following in your maven settings.xml file (found in e.g. "C:\users\your-username\.m2")

<settings>
  <servers>
    <server>
      <id>it-innovation-public-repository</id>
      <username>your-username-on-altano</username>
      <configuration>
        <sshExecutable>plink</sshExecutable>
        <scpExecutable>pscp</scpExecutable>
      </configuration>
    </server>
  </servers>
</settings>

Execute "mvn clean source:jar javadoc:jar deploy".