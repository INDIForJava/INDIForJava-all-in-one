<img src="assets/logo.png" width="100" style="margin-bottom: 8px" alt="INDIForJava logo"/>

## The INDIForJava project

INDIForJava is a set of libraries (written in the Java programming language) to implement clients (graphical and not graphical ones), drivers and servers that follow the [INDI distributed control protocol](https://www.indilib.org/), a protocol designed to operate astronomical instrumentation.

The project is fully open-source and is available on [GitHub](https://github.com/INDIForJava/INDIForJava).

## Intended Audience

The provided libraries are focused on the development of Drivers, Clients and Servers using the INDI protocol in the Java programming language. If you are a regular software user, you are looking for a simple program to control your astronomical devices and you don't know anything about drivers, programming, Java probably this page is not for you. Try looking at [INDI Clients](https://indilib.org/individuals/indi-clients.html) for a list of programs that may be useful for your purposes.

However, if you are a programmer and you want to start your own astronomical control program or you want to program a driver for a particular astronomical device, this might be your starting point.

Please note that this libraries do not pretend to substitute or even replace the original INDI libraries which are still used and actively developed. Moreover, this project does not pretend to mimic every single option that the original libraries offer and, in fact, the programming ideas behind this library differ in some points to the original ones: particularly, INDI for Java tries to provide a truly object oriented approach in the development of INDI compatible software.

## Where do I start?

If you are interested in the project, you might want to check the [repository](https://github.com/INDIForJava/INDIForJava) available for the project. There you have all the necessary tools to begin developing you're own INDI Clients and Drivers: You'll find the source code for the library, the compiled libraries (in .jar format), all the Java Docs for the libraries and even some example files to start experimenting.

It is also recommended that you check the original [INDI Library web page](http://indilib.org/) in which you will find lots of information about the INDI protocol, the original INDI libraries (written in C and C++) and many more resources (including a forum if you have questions or want to report errors) that might help you start developing your own INDI related projects.

### Example tutorials

Coming soon...

## Library development

### Required build tools

- IntelliJ IDEA 2020.3 or greater
- The Gradle IntelliJ plugin
- Java Development Kit ≥8

### Dependencies

- Simple Logging Facade for Java ([SLF4J](http://www.slf4j.org/)), [MIT license](http://www.slf4j.org/license.html)
- Glassfish [Tyrus](https://tyrus-project.github.io/), [multiple licenses](https://tyrus-project.github.io/license.html)
- [XStream](http://x-stream.github.io/), [BSD license](http://x-stream.github.io/license.html)
- [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/), [Apache 2.0 license](https://github.com/apache/commons-codec/blob/master/LICENSE.txt)
- [nom-tam-fits](https://github.com/nom-tam-fits/nom-tam-fits), [public domain](http://nom-tam-fits.github.io/nom-tam-fits/license.html)
- [Pi4J](https://pi4j.com/1.2/index.html), [LGPL 3.0 license](https://pi4j.com/1.2/license.html)
- [jssc](https://github.com/java-native/jssc) by java-native, [LGPL 3.0 license](https://github.com/java-native/jssc/blob/master/LICENSE.txt)
- [Quickhull3d](https://github.com/Quickhull3d/quickhull3d), [BSD 2-Clause "Simplified" License](https://github.com/Quickhull3d/quickhull3d/blob/master/LICENSE.txt)
- [JBoss Jandex](https://github.com/wildfly/jandex), [Apache 2.0 license](https://github.com/wildfly/jandex/blob/master/LICENSE.txt)

### Library modules

- `core`: the base library.
- `driver`: the module that represents an INDI driver.
- `client`: an INDI client module, which can connect to INDI servers and control drivers.
- `server`: an INDI server module, which can load drivers and connect to clients.
- `driver-telescope`: a telescope abstract driver.
- `driver-focuser`: a telescope abstract driver.
- `driver-ccd`: a telescope abstract driver.
- `driver-filterwheel`: a telescope abstract driver.
- `driver-raspberrypigpio`: an example driver that can control Raspberry Pi GPIOs.
- `driver-active-extension`: a driver extension used to create active drivers (drivers that can manage other drivers on the same server).
- `driver-serial-extension`: a driver extension that provides support for serial ports.
- `fits-utils`: an utility module which can be used together with the CCD module.
- `gnu-scientific-lib`: a Java port of the [GNU Scientific Library](https://www.gnu.org/software/gsl/), used internally in the telescope driver.

### Bug tracking

Feel free to report bugs in the [GitHub issue tracker](https://github.com/INDIForJava/INDIForJava/issues)!

## License

This library is licensed under the GNU Lesser General Public License v3.0. A copy of the license is available in the [GitHub repository](https://github.com/INDIForJava/INDIForJava/blob/main/LICENSE.md)

## Developed by

- [Marco Cipriani](http://marcocipriani01.github.io/) (@marcocipriani01), project admin
- Richard van Nieuwenhoven (@ritchieGitHub), [java for all](http://j4all.org/)
- Sergio Alonso (@zerjillo), [Software Engineering Department, University of Granada](http://lsi.ugr.es/)

### Thanks to

- Antonio Román: testing.
- Alexander Tuschen: testing and bug reporting.
- Romain Fafet and Gerrit Viola for their input, testing and code.
