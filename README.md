MAS
===

The multi-agent simulation repository (MAS) enables the generation of base-scenarios for German subregions. It will resource data from InnoZ' Mobility DataHub (on the 'playground' server) and will store outdata there as well. Therefor this repo includes the following files:
- scripts for the import of relevant data to the Mobility DataHub
- scripts to run MATSim on the Mobility DataHub
- scripts for the base-scenario generation from scratch
- brief descriptions of the process of scenario generation, in particular our (InnoZ) approach of traffic modelling (see [Wiki](https://github.com/00Bock/MAS/wiki))

## Basic requirements
### MATSim
Due to the constant development process of MATSim, we do not provide a documentation in this repo. For information about how to set up, run and interpret MATSim see
- [MATSim Website](http://www.matsim.org)
or
- [MATSim book](http://ci.matsim.org:8080/view/All/job/MATSim-Book/ws/matsimbook-latest.pdf)

### Installing or updating Java
To run one of the scenarios from the terminal on your local machine you need a Java development kit (jdk). The java version used in the latest MATSim release (e.g. matsim-0.x.x.zip) is jdk7, for the development version or your own MATSim playground you will probably need jdk8.
We strongly recommend to use the latest java version available!

There are several implementations of jdks available. The most common ones are the Oracle implementation and OpenJDK. Choose for yourself which one you prefer, they should work identically.
To install Java on a Windows system, just download the installation file of the latest release and follow the instructions during the installation process.

The installation of Java under a Linux distribution can either be done in the software center or in the terminal. To update e.g. OpenJDK, Version X you need to run

- sudo apt-get install openjdk-X-jdk openjdk-X-source

If Ubuntu should not find the requested files, you need to add the ppa key with the following command

- sudo add-apt-repository ppa:openjdk-r/ppa

After that, run
- sudo apt-get update

and execute the sudo apt-get install command again.

If you have further questions, have a look at [this page](https://wiki.ubuntuusers.de/Java/Installation/).

### Installing Eclipse
If you want to write java scripts or your own java classes to extend or modify MATSim, you also need a software IDE (most of the MATSim developers use [Eclipse IDE for Java Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/mars2)).

Windows: Download the zip-File and unzip it to wherever you want to place it.
To install Eclipse in Linux follow the instructions here: http://www.krizna.com/ubuntu/install-eclipse-in-ubuntu-12-04/ or use the software center.

### Access to Mobility DataHub
To access the Mobility DataHub, you need a user account on the playground. Just contact either Benno Bock, Daniel Hosse or Benjamin Stolte to create an account for you.

You can access the playground via SecureShell (ssh). If your computer has a Linux or an OS operating system, you need nothing else than an existing user account, ssh should already be installed. On Windows, you need to install an ssh client program like [PuTTY](http://www.putty.org) or [WinSCP](https://winscp.net). More hints can be found [here](https://www.innoz.de/de/mobility-database-1).

## First steps
An outdated but maybe still helfull documentray can be found here: http://archive.matsim.org/docs/tutorials/8lessons





