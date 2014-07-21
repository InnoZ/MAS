MAS
===

analysis and (potentially) development of a multi-agent simultation for carsharing

Text taken from:
http://www.matsim.org/docs/tutorials/8lessons/installation

INSTALATION
Follow the links to, first, install necessary software on your machine and, second, learn some very basic concepts about Java programming.
But first, let us organize our folder structure such that we will find the various downloads easier again:
1. create a folder called "MATSimTutorial" on an easy accessible place (i.e. your desktop or directly under C:, D: or any other partition)
2. inside it, create the folders "install", "software" and "workspace".
- in install, we download all kind of software to install
- in software we download software that works without installation
- the workspace is the place where we work with the Eclipse development environment

INSTALING JAVA
MATSim is written in Java. To run java you need a JRE and to develop Java code you must install a JDK. You can check if Java is already installed on your machine by opening the terminal and typing in 
$java -version
Use the following in the terminal if you don't have JDK installed:
$ sudo apt-get install openjdk-7-jdk

INSTALING ECLIPSE
Most of the core MATSim developers use Ecplise as a software development environment. We recommend the "Eclipse IDE for Java Developers" distribution. To install follow the instructions here: http://www.krizna.com/ubuntu/install-eclipse-in-ubuntu-12-04/

INSTALLING MATSIM
The version number is 0.5.x, with 'x' being the number of latest official bugfix release. Make sure always to use the latest bugfix release. In order to access code and data, the Eclipse IDE is used instead of the command line. Take the following steps to install MATSim in Eclipse.

- Download the current release (matsim-0.5.x.zip, replace x with the number of the bugfix release) from the sourceforge download page (http://sourceforge.net/project/showfiles.php?group_id=167850) and save it on your desktop.
- Unzip the file. A folder (matsim-0.5.x) is created.
- Start Eclipse.
- Start a new Java Project: Click File -> New -> Java Project
- Deselect the "Use default location" checkbox
- For the "Location", choose the unzipped matsim-0.5.x folder.
- Make sure a JRE 6 (or newer version) is set in section JRE.
- Click Next.
- To make the MATSim source code visible in Eclipse do  the following:
    - In the "Java Settings" dialogue, click the Libraries tab.
    - In the JARs list, choose matsim-0.5.x-sources.jar and click Remove.
    - In the JARs list, choose matsim-0.5.x.jar and expand it.
    - Click Source attachment: (None), then click Edit....
    - Click Workspace....
    - Choose matsim-0.5.x/matsim-0.5.x-sources.jar, then click OK.
    - Click again OK to confirm the "Source Attachment Configuration" Dialog
    - Click Finish.
Congratulations! You have successfully installed MATSim for usage with Eclipse. The project can be found in the Package Explorer on the left side of the screen. You can now investigate the MATSim software project, e.g.. Browse the source code of MATSim (packages in Referenced Libraries - matsim-0.5.x.jar). For this, make sure that the package presentation is hierarchical: Click the small triangle at the top right of the Package Explorer, then choose Package presentation - Hierarchical.
