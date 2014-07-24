# Ticketanalyzer

## Packaging

To build a distributable package, just run 

```bash
export GRADLE_OPTS=-XX:MaxPermSize=512M
./gradlew dist
```

All files that are required to run the projects are now compiled to `build/dist`


## Setting up your IDE

Although the requirements of the projects are rather simple, the IDE setup is sometimes a bit tricky. The main reason is, that the ticketanalyzer uses a plugin architecture and resolves its dependencies through multiple classloaders which use relative paths to load the plugins.

In any case, first run:

```bash
export GRADLE_OPTS=-XX:MaxPermSize=512M
./gradlew dist
```

This copies required libraries such as git or svn libraries into a directory where the ticketanalyzer can find them. Then setup IntelliJ or Eclipse:

### IntelliJ

Next, run
 
```bash
export GRADLE_OPTS=-XX:MaxPermSize=512M
./gradlew idea
```

You can now open the project as a normal IntelliJ-Project (It is not required to setup the project as gradle-project in idea, but it seems it doesn't cause problems either). To start the Project, create an new configuration (replace $projectdir through a real value):
                                                                                               
Setting                 | Value
------------------------|-------
Main class              | de.jowisoftware.mining.Main
VM Options              | -Dsettings=$projectdir/core/src/test/resources/config.properties -DprojectDir=$projectdir/core
WorkingDirectory        | $projectdir/core
Use classpath of module | core

### Eclipse

Next, run

```bash
export GRADLE_OPTS=-XX:MaxPermSize=512M
./gradlew eclipse
```

You can now import all existing projects into eclipse (you will have to import `common`, `core` and all plugins you want to change as separate projects). Please note that in some versions of ScalaIDE the Swing-Library is excluded from the buildpath. _In this case, you have to add the library by yourself_. To start the Project, create an new launcher:

Setting                 | Value
------------------------|-------
Project                 | core
Main class              | de.jowisoftware.mining.Main
VM Arguments            | -Dsettings=${project_loc}/core/src/test/resources/config.properties -DprojectDir=${project_loc}
WorkingDirectory        | ${project_loc}
