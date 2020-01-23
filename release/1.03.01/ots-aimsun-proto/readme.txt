Note for making changes to the proto-file and automatic compilation in Eclipse:
  
If you are using Eclipse, you need to install an additional Eclipse plugin because m2e does 
not evaluate the extension specified in a pom.xml. Download os-maven-plugin-1.6.0.jar and 
put it into the <ECLIPSE_HOME>/plugins directory. The location of the jar file is:
http://repo1.maven.org/maven2/kr/motd/maven/os-maven-plugin/1.6.0/os-maven-plugin-1.6.0.jar
(os-maven-plugin is a Maven extension, a Maven plugin, and an Eclipse plugin.) 
       
Alternatively, after a change, do run-as on the project, and use Maven build... 
specify "compile" as the Goal. Add a parameter, and use os.detected.classifier
as the parameter name, and e.g., windows-x86_64 as the value, or any other os classifier 
as described at https://github.com/trustin/os-maven-plugin 
