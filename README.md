##THINGS/THINGER 2009
Copyright Erich P Gatejen (c) 2001 through 2011  ALL RIGHTS RESERVED

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the 
License for the specific language governing permissions and limitations under
the License.

For additional license information see the files in the directory doc/license

##DEVELOPERS VERSION 1.3

Things/Thinger is an experimental server and set of tools used to explore a 
various topics in computer science, some of which have long since moved on to 
their own open or proprietary projects.  While the server is fully functional 
and has been used as a base for several other products, this release is 
primarily intended to be a toolkit for those doing their own projects or
as a source code resource for ideas and solutions.

Some of the code is great.  Some of it is trash.  It depends on my specific
goal at the time.  A few people have contributed to this code base, but most
work by others has been in the encapsulating or follow-on projects.  I will
continue to do some maintenance, as it is needed for projects that it 
supported as of April 2009.  I have no intention of supporting the general
public, but it never hurts to ask.

More information can be found in the doc/ directory.

The developers version is for the development of the Things/Thinger system
itself.  The distribution is all that is necessary to use it as a 
toolkit.

While it might seem that this project is geared toward windows, that vast
majority of installations are on Linux.  I personally developed it on
Windows using Eclipse, and thus you see some .BAT helper scripts.  However,
these were for my convenience only.  You'll note that the actual distribution
scripts are both Bash/SH and Windows .BAT.

##PREREQUISITES - Developers Version

-- REQUIRED  ---------------------------------------------------------------

1) JDK 1.6 or higher.  It must be the JDK and not only the JRE.  It uses
   the jdk/lib/tools.jar library.

2) Apache ANT 1.6 or higher.

-- OPTIONAL  ---------------------------------------------------------------

1) Eclipse SDK 3.3 or higher.  You can do everything with a test editor or
   another IDE, but I know Eclipse works well with this project.

##INSTALLATION - Developers Version

1. Edit installconfig.prop.  Change the 'java.jdk' to point to your 
   installation of the Java JDK.  

2. If using Windows, edit set-build-env.bat.  The various .BAT scripts will 
   then work.
THIN- Change ANT_HOME to point to the ANT install location.
    - Change JAVA_HOME to point to the Java JDK install location.  This would
      be the same as in Step 1) above.
      
3. Make the test environment.
    - If using Windows, run maketest.bat
    - If using Linux, run ANT with the target of 'test'.  There are lots of
      ways to do this, but the following should always work:
            + CD to the THINGS install directory.
            + Run /opt/apache-ant-1.7.1/bin/ant test
                  ^^^^^^^^^^^^^^^^^^^^^ where this points to the ANT
                  installation directory.                  

NOTES:
- The ANT targets are geared toward being run from the things install
directory.  They should work all sorts of other ways, but that's how I tested
it.

##MANIFEST - Developers Version

-- IN PACKAGE -------------------------------------------------------------

.classpath              - My classpath file for eclipse.
.project                - My project file for eclipse.
build.bat               - Windows convenience script to run a full build.
build.xml               - ANT build XML.  All targets should work.
cleanit.bat             - Windows convenience script to clean environment.
compile.bat             - Windows convenience script to compile only.
content/                - Content directory. 
content/bin             - Scripts for distribution.
content/build           - Files used in the build process.
content/etc             - Configuration files for the distribution.
content/install.bat     - Installation script for the distribution.
content/install.sh      - Installation script for the distribution.
content/README.TXT      - README for the distribution.
content/VERSION         - VERSION file for the distribution.
content/universe        - Seed UNIVERSE.
doc/                    - Documentation.
installconfig.prop      - Installation configuration file.
lib/                    - 3rd party libraries
LICENSE                 - Liscence notification
makepackage.bat         - Packages the binary distribution.
maketest-noconfig.bat   - Windows convenience script to make the test/ 
                          area without configuring it.
maketest.bat            - Windows convenience script to make and configure
                          the test area.
projects                - Source projects.  Split into the main code and
                          the test code.  If using Eclipse, each should be
                          a separate project, with test dependant on things.
README.TXT              - This file.
set-build-env.bat       - Used by the Windows convenience scripts.
systemtest.bat          - Run the system tests.
unittest.bat            - Run the unit tests.
VERSION                 - The version file.

-- AFTER INSTALL ----------------------------------------------------------

test/                    - Test installation of the distribution.
dist/                    - The compiled distribution.

##USING ECLIPSE

-- SET UP ------------------------------------------------------------
There are a lot of ways to set this up, so feel free to mess with this.  
This is just how I have it set up right now.

- Create two projects 'things' and 'things.test' from the source in 
  projects/things/src and project/test/src, respectively.  Make sure the
  binary compiles go to projects/things/bin and project/test/bin, respectively.
- Add all the libraries under lib/ in the build path for the things project.
  This can be done in the properties setting for the project, under
  'Java Build Path' in Libraries.
- Add tools.jar from the JDK lib/ directory in the build path for the things 
  project.
- Add things as a project for test.  This can be done in the properties 
  setting for the project, under 'Java Build Path' in Projects.  (No,
  you do not have to add the libraries to the test project.  They will be
  inherited from the things project.)

-- RUNNING THE TEST SUITES --------------------------------------------
You can set up the following Run/Debug configurations under the test 
project.

UNIT TEST
project: things
class: test.things.CLIThingsUnitSuiteTester
Arg0 - C:\dev\things\test\etc\test\things\suite.prop

SYSTEM TEST
project: test.system.CLISystemSuiteTester
Arg0 - C:\dev\things\test\etc\test\system\suite.prop 
Arg1 - C:\dev\things\test\etc\test\system\basic_config.prop

-- RUNNING THE SERVER  ----------------------------------------------
You can set up the following Run/Debug configurations under the test 
project to run the server with a console.

SYSTEM SERVER w/ Console
project: test
class: things.thinger.kernel.basic.KernelBasic_System_Bootstrap
Arg0 - C:\dev\things\test\etc\basic_config.prop

-- RUNNING THE CONFIGURATION -----------------------------------------
It's easier to use ANT to do this, but if you need to debug the configuration
process itself, you can set up the following Run/Debug configurations.
 
ConfigureByProps FACTORY
project: things
class: things.common.configuration.ConfigureByProps
Arg0 - checkpoint
Arg1 - /dev/things/content/devtest_install.config
Arg2 - /dev/things/test
Arg3 - /dev/things/test/etc/checkpoint/factory

ConfigureByProps CONFIG
project: things
class: things.common.configuration.ConfigureByProps
configure
Arg0 - /dev/things/content/devtest_install.config
Arg1 - /dev/things/test/etc/checkpoint/factory
Arg2 - /dev/things/test
Arg3 - /dev/things/test/installconfig.prop



