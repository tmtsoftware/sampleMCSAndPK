# SampleMCS

This project implements an HCD (Hardware Control Daemon) and an Assembly using 
TMT Common Software ([CSW](https://github.com/tmtsoftware/csw)) APIs. 

## Subprojects

* samplemcsAssembly - an assembly that talks to the SampleMCS HCD
* samplemcsHCD - an HCD that talks to the SampleMCS hardware
* samplemcsDeploy - for starting/deploying HCDs and assemblies

## Build Instructions

The build is based on sbt and depends on libraries generated from the 
[csw](https://github.com/tmtsoftware/csw) project.

See [here](https://www.scala-sbt.org/1.0/docs/Setup.html) for instructions on installing sbt.

## Prerequisites for running Components

The CSW services need to be running before starting the components. 
This is done by starting the `csw-services.sh` script, which is installed as part of the csw build.
If you are not building csw from the sources, you can get the script as follows:

 - Download csw-apps zip from https://github.com/tmtsoftware/csw/releases.
 - Unzip the downloaded zip.
 - Go to the bin directory where you will find `csw-services.sh` script.
 - Run `./csw_services.sh --help` to get more information.
 - Run `./csw_services.sh start` to start the location service and config server.

## Building the HCD and Assembly Applications

 - Run `sbt samplemcsDeploy/universal:packageBin`, this will create self contained zip in `samplemcsDeploy/target/universal` directory
 - Unzip the generated zip and cd into the bin directory

Note: An alternative method is to run `sbt stage`, which installs the applications locally in ./target/universal/stage/bin.

## Performance measurements


#### Step 0 Log Files path setup:
Export below environment variable. This is where events/commands measurement data csv file will be generated after ~ 20mins.
export LogFiles=<Path of the folder in which log files should be generated>
e.g.: export LogFiles=/home/tmt_tcs_2/LogFiles/scenario5
 
#### Step 1 - Start CSW services  
`./csw-services.sh start`  

#### JAVA 9  
As Java 1.8 does not support time capturing in microsecond, before starting any assembly PK or MCS, switch to JRE 9 by modifying PATH variable. This is required only for deployment and build should be done with java 8.  
`export PATH=/java-9-home-path-here/bin:$PATH`  

#### Step 2 - Start Pointing Kernel Assembly  
`export PATH=/java-9-home-path-here/bin:$PATH`  
`cd samplepk/samplepkDeploy/target/universal/stage/bin`  
`./pk-container-cmd-app --local ../../../../src/main/resources/SamplepkContainer.conf`  

#### Step 3 - Start MCS Assembly  
`export PATH=/java-9-home-path-here/bin:$PATH`  
`cd samplemcs/samplemcsDeploy/target/universal/stage/bin`  
`./mcs-container-cmd-app --local ../../../../src/main/resources/McsContainer.conf`  

#### Step 4 - Start Jconsole and connect to MCS Container process from it.
`jconsole`  

#### Step 5 - Start Event subscription in MCS  

`export PATH=/java-9-home-path-here/bin:$PATH`  
`cd samplemcs/samplemcsDeploy/target/universal/stage/bin`  
`./mcs-main-app`  

#### Step 6 - Start Event Generation in PK  
`export PATH=/java-9-home-path-here/bin:$PATH`  
`cd samplepk/samplepkDeploy/target/universal/stage/bin`  
`./pk-client`  

