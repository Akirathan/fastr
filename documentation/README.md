# FastR

FastR is an implementation of the [R Language](http://www.r-project.org/) in Java atop [Truffle](https://github.com/graalvm/), a framework for building self-optimizing AST interpreters.

FastR is:

* polyglot

R is very powerful and flexible, but certain tasks are best solved by using R in combination with other programming languages.
Interfaces to languages, e.g., Java, Fortran and C/C++, incur a significant overhead, which is caused, to a large degree, by the different execution strategies employed by different languages, e.g., compiled vs. interpreted, and by incompatible internal data representations.

The Truffle framework addresses these issues at a very fundamental level, and builds the necessary polyglot primitives directly into the runtime.
Consequently, FastR leverages this infrastructure to allow multiple languages to interact transparently and seamlessly.
All parts of a polyglot application can be compiled by the same optimizing compiler, and can be executed and debugged simultaneously, with little to no overhead at the language boundary.

* efficient

R is a highly dynamic language that employs a unique combination of data type immutability, lazy evaluation, argument matching, large amount of built-in functionality, and interaction with C and Fortran code.
Consequently, applications that spend a lot of time in R code often have performance problems.
Common solutions are to try to apply primitives to large amounts of data at once and to convert R code to a native language like C.

FastR makes extensive use of the dynamic optimization features provided by the Truffle framework to remove the abstractions that the R language introduces, and can use the Graal compiler to create optimized machine code on the fly.

* compatible

The hardest challenge for implementations of the R language is the tradeoff between compatibility and performance.
If an implementation is very compatible, e.g., by using the traditional internal data layout, it cannot perform optimizations that imply a radically different internal structure.
If an implementation is very efficient, e.g., by adapting internal data structures to the current requirements, it will find it difficult to implement some parts of the GNUR system that are interfacing with applications and packages.

FastR employs many different solution strategies in order to overcome these problems, and also explores possible solutions at a grander scale, like evolution and emulation of R’s native interfaces.

## Getting FastR

FastR is available in two forms:

1. As a pre-built binary. Note that this also includes (Truffle) implementations of JavaScript and optionally Ruby and Python. The pre-built binaries are available for Linux and Mac OS X. The binary release is updated monthly. To install and setup GraalVM and FastR follow the Getting Started instructions in FastR [README](../../README.md#getting_started).
2. As a source release on [GitHub](https://github.com/graalvm/fastr) for developers wishing to contribute to the project and/or study the implementation. The source release is updated regularly and always contains the latest tested version.
    * Note: there is a comunity provided and maintained [Dockerfile](https://github.com/nuest/fastr-docker) for FastR.

## Documentation

Reference manual for FastR, its limitations, compatibility and additional functionality is
available at [GraalVM website](http://www.graalvm.org/docs/reference-manual/languages/r/).

Further documentation is in the [documentation folder](documentation/Index.md) of this repository.

# Building FastR from Source

Building FastR from source is supported on Mac OS X (El Capitan onwards), and various flavors of Linux.
FastR uses a build tool called `mx` (cf `maven`) which can be downloaded from [here](http://github.com/graalvm/mx).
`mx` manages software in _suites_, which are normally one-to-one with a `git` repository. FastR depends fundamentally on the [truffle](http://github.com/graalvm/graal) suite. However, performance also depends on the [Graal compiler](http://github.com/graalvm/graal) as without it, FastR operates in interpreted mode only. The conventional way to arrange the Git repos (suites) is as siblings in a parent directory, which we will call `FASTR_HOME`.

## Pre-Requisites
FastR shares some code with GnuR, for example, the default packages and the Blas library. Therefore, a version of GnuR (currently
R-3.6.1), is downloaded and built as part of the build. Both GNU R and FastR require access certain tools and packages that must be available
prior to the build. These are:

    A jvmci-enabled Java JDK which is available from [pre-built binary](http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html)
    Python version 2.7.x
    A Fortran compiler and libraries. Typically gfortran 4.8 or later
    A C compiler and libraries. Typically gcc or clang
    The pcre package, version 8.38 or later
    The zlib package, version 1.2.8 or later
    The bzip2 package, version 1.0.6 or later
    The xz package, version 5.2.2 or later
    The curl package, version 7.50.1 or later

If any of these are missing the GNU R build will fail which will cause the FastR build to fail also. If the build fails, more details can be found in log files in
the `libdownloads/R-{version}` directory. Note that your system may have existing installations of these packages, possibly in standard system locations,
but older versions. These must either be upgraded or newer versions installed with the package manager on your system. Since different systems use different package
managers some of which install packages in directories that are not scanned by default by the C compiler and linker, it may be necessary to inform the build of these
locations using the following environment variables:

    PKG_INCLUDE_FLAGS_OVERRIDE
    PKG_LDFLAGS_OVERRIDE

For example, on Mac OS, the MacPorts installer places headers in /opt/local/include and libraries in /opt/local/lib, in which case, the above variables must be set to these
values prior to the build, e.g.:

    export PKG_INCLUDE_FLAGS_OVERRIDE=-I/opt/local/include
    export PKG_LDFLAGS_OVERRIDE=-L/opt/local/lib

 Note that if more than once location must be specified, the values must be quoted, e.g., as in `export PKG_LDFLAGS_OVERRIDE="-Lpath1 -Lpath2"`.

 The environment variable `JAVA_HOME` must be set to the location of the jvmci-enabled Java JDK.

## Building FastR
Use the following sequence of commands to download and build an interpreted version of FastR.

    $ mkdir $FASTR_HOME
    $ cd $FASTR_HOME
    $ git clone http://github.com/graalvm/mx
  $ PATH=$PATH:$FASTR_HOME/mx
  $ git clone http://github.com/graalvm/fastr
  $ cd fastr
  $ mx build

The build will clone the Truffle repository and also download various required libraries, including GNU R, which is built first. Any problems with the GNU R configure step likely relate
to dependent packages, so review the previous section. For FastR development, GNU R only needs to be built once, but an `mx clean` will, by default remove it. This can be prevented by setting
the `GNUR_NOCLEAN` environment variable to any value.

It is possible to build FastR in "release mode" which builds and installs the GNU R "recommended" packages and also creates a `fastr-release.jar` file that contains everything that is needed to
run FastR, apart from a Java VM. In particular it captures the package dependencies, e.g., `pcre` and `libgfortran`, so that when the file is unpacked on another system it will work regardless of whether the packages are installed on that system. For some systems that depend on FastR, e.g., GraalVM, it is a requirement to build in release mode as they depend on this file. To build in release mode, set the `FASTR_RELEASE` environment variable to any value. Note that this can be done at any time without doing a complete clean and rebuild. Simply set the variable and execute `mx build`.

## Running FastR

After building, running the FastR console can be done either with `bin/R` or  with `mx r` or `mx R`. Using `mx` makes available some additional options that are of interest to FastR developers.
FastR supports the same command line arguments as R, so running an R script is done with `bin/R -f <file>` or `bin/Rscript <file>`.

## IDE Usage

`mx` supports IDE integration with Eclipse, Netbeans or IntelliJ and creates project metadata with the `ideinit` command (you can limit metadata creation to one IDE by setting the `MX_IDE` environment variable to, say, `eclipse`). After running this command you can import the `fastr` and `truffle` projects using the `File->Import` menu.

## Contributing

We would like to grow the FastR open-source community to provide a free R implementation atop the Truffle/Graal stack.
We encourage contributions, and invite interested developers to join in.
Prospective contributors need to sign the [Oracle Contributor Agreement (OCA)](http://www.oracle.com/technetwork/community/oca-486395.html).
The access point for contributions, issues and questions about FastR is the [GitHub repository](https://github.com/oracle/fastr).

## Troubleshooting

### GNU-R build fails

GNU-R is built as part of the FastR build and therefore all GNU-R dependencies are required.
The output from GNU-R configuration is logged in `libdownloads/R-3.6.1/gnur_configure.log`.

### Build fails when generating R grammar

This problem manifests by the following error message in the build output:

`Parser failed to execute command`

followed by a series of parser errors, such as:

`error(170): R.g:<LINE>:<COL>: the .. range operator isn't allowed in parser rules`

It seems to be an ANTLR issue occurring when `LANG`, `LC_ALL` and `LC_CTYPE` environment
variables are not set to the same value.

The solution is to set those variables to the same value, e.g.

```
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LC_CTYPE=en_US.UTF-8
```

Note: you may need to install `locale` and run the following before setting the above env variables:

```
locale en_US.UTF-8
```


