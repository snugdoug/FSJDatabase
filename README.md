## Please note this is just a side project I am doing for fun and it might not work on windows, because it is meant for linux.

# Wiki:

## <summary>Sections</summary>
1. What does FSJDatabase stand for? https://github.com/snugdoug/jdb/master/README.md#1-what-does-fsjdatabase-stand-for
2. How do I use this library? https://github.com/snugdoug/jdb/edit/master/README.md#2-how-do-i-use-this-library
3. Notes. https://github.com/snugdoug/jdb/master/README.md#notes

## <summary>1. What does FSJDatabase stand for?</summary>
FSJDatabase stands for:
 
File,
Structure,
Java,
Database

## <summary>2. How do I use this library?</summary>

First you need to add the jar to your build path or through maven.
How to install it through maven:
```
mvn install:install-file -Dfile=<path> -DgroupId=<com.snugdoug.fsjdatabase> -DartifactId=<fsjdatabase-library> -Dversion=<version> -Dpackaging=jar
```
How to install in build path:

In eclipse first right click on the project, then click build path and click configure build path, after that click java build path, finally click librarys and add jars then select the FSJDatabase jar (you may have to navigate to the jar)

Note: you will need to run it for every new version of FSJDatabase

then you will put this in your pom.xml:

``` 
<dependency>
    <groupId>com.snugdoug.fsjdatabase</groupId>
    <artifactId>fsjdatabase-library</artifactId>
    <version>VERSION</version>
</dependency>
```

## <summary>Notes</summary>
also in com.snugdoug.example there is a code example of how to use the library also that is the code I use to the program so it should work.
Also I do use AI to help me work on the project.
AI's that I use for my projects:

https://gemini.google.com

https://chatgpt.com/





   
