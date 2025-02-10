Please note this is just a side project I am doing for fun and it might not work on windows, because it is meant for linux.

Wiki:
1. What does FSJDatabase stand for?
FSJDatabase stands for:
File
Structure
Java
Data
Base

3. How do I use this library?
First you need to add the jar to you build path or through maven.
How to install it through maven:

mvn install:install-file -Dfile=<path> -DgroupId=<com.snugdoug.fsjdatabase> -DartifactId=<fsjdatabase-library> -Dversion=<version> -Dpackaging=jar

Note: you will need to run it for every new version of FSJDB

then you will put this in your pom.xml:

<dependency>
    <groupId>com.snugdoug.fsjdatabase</groupId>
    <artifactId>fsjdatabase-library</artifactId>
    <version>VERSION</version>
</dependency>

also in com.snugdoug.example there is a code example of how to use the library also that is the code I use to the program so it should work.


   
