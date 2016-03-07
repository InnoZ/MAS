MAS
===

The multi-agent simulation repository (MAS) stores
- input files for MATSim scenarios
- scripts to run
	* MATSim on the InnoZ playground server
	* scenario generation from scratch
- brief descriptions scenario generation, in perticular our (InnoZ) approach of traffic modelling

## Basic requirements
To access the MATSim data, you need a user account on the playground. Just contact either Benno Bock, Daniel Hosse or Benjamin Stolte to create an account for you.

You can access the playground via SecureShell (ssh). If your computer has a Linux or an OS operating system, you need nothing else than an existing user account, ssh should already be installed. On Windows, you need to install an ssh client program like PuTTY or WinSCP.

To run one of the scenarios from the terminal on your local machine you need a Java development kit (jdk). The java version used in the latest MATSim release (e.g. matsim-0.x.x.zip) is jdk7, for the development version or your own MATSim playground you will probably need jdk8.

If you want to write java scripts or your own java classes to extend or modify MATSim, you also need a software IDE (most of the MATSim developers use Eclipse IDE for Java Developers, http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/mars2).

### Installing Java
There are several implementations of jdks available. The most common ones are the Oracle implementation and OpenJDK. Choose for yourself which one you prefer, they should work identically.
To install Java on a Windows system, just download the installation file of the latest release and follow the instructions during the installation process.
The installation of Java under a Linux distribution can either be done in the software center or in the terminal. In the terminal, just type

> sudo apt-get install openjdk-[version]-jdk

and replace version with the latest release number (currently it is openjk-8).

### Installing Eclipse
Windows: Download the zip-File and unzip it to wherever you want to place it.
To install Eclipse in Linux follow the instructions here: http://www.krizna.com/ubuntu/install-eclipse-in-ubuntu-12-04/ or use the software center.

## MATSim Documentation
Due to the constant development process of MATSim, we do not provide a documentation in this repo. For information about how to set up, run and interpret MATSim see
- [MATSim Website] ( http://www.matsim.org )
or
- [MATSim book] ( http://ci.matsim.org:8080/view/All/job/MATSim-Book/ws/matsimbook-latest.pdf )
