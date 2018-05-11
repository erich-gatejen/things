===============================================================================
THINGS/THINGER 2009
Copyright Erich P Gatejen (c) 2001 through 2010  ALL RIGHTS RESERVED

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

===============================================================================
RELEASE VERSION 1.2.1

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

===============================================================================
PREREQUISITES - Release Version

-- REQUIRED  ---------------------------------------------------------------

1) JDK 1.6 or higher.  It must be the JDK and not only the JRE.  It uses
   the jdk/lib/tools.jar library.

===============================================================================
INSTALLATION - Release Version

1. Run install.sh (for Linux) or install.bat with two arguments: the
   install directory for things (where you find this README.TXT) and
   the location of the jdk.  If you run the script without arguments, it
   will give you help.  And example run like this:

	./install.sh /opt/things /usr/java/jdk1.6.0_03"


That's it!

You cannot run it again, so the install script will go away.

===============================================================================
MANIFEST - Developers Version

-- IN PACKAGE -------------------------------------------------------------

bin\                        - Scripts
bin\runsystemtest.bat       - Script to run the system test
bin\runsystemtest.sh        - Script to run the system test     
bin\rununittest.bat         - Script to run the system test
bin\rununittest.sh          - Script to run the system test
bin\server-console.bat      - Start the server with a console.
bin\server-console.sh       - Start the server with a console.
doc\                        - Documentation
etc\                        - Configurations
etc\basic_config.prop       - Simple, working config for the server.
etc\config.layout           - Install layout.
install.bat                 - Install script.
install.sh                  - Install script.
lib\                        - Runtime libraries.
log\                        - Runtime libraries.
README.TXT                  - This file.
universe\                   - The storage UNIVERSE.
VERSION                     - The version file.

Note that the BASH or Windows .BAT files will disappear for the
platform not installed.

===============================================================================
USING IT

-- THE CONSOLE ------------------------------------------------------------
When you start the server with the "server-console" script, the command line
will accept commands after boot is complete.  The command 'help' will display 
the help.  Arguments are space delimited.

The following is an example run:

C:\dev\things\test\bin>server-console.bat
Begin bootstrap.
Starting multiprocessing kernel.
The kernel process has started.
Logging level set as DEBUG
Loaded properties.
System logger started.
Data prepared.
      service HttpToolService started.
      service HttpCLIService started.
      service ConsoleService started.
      service CLIService started.
      service LocalCommandService started.
Boostrap complete.
help
CLI Command system for THINGS.  The following commands are available:
  ping (whisper)          - Ping the server to see if it is alive.
  ps ('log')              - Get the process list.  Token 'log' will force the
                            output to the log only (not the console).
  run [thing]             - Run the thing named [thing].
  setprop [name] [value]  - Set a local property with the [name] and [value].
  showprops               - Show local properties.
  kill [pid]              - Kill the process given by [pid].
  help                    - Show this help.
  quit                    - Stop the server.
Local properties will be applied to the THINGS run after they are set, and will
disappear after the server is stopped.

-- COMMAND INTERFACE --------------------------------------------
After the server is up, it listens on port 8801 for commands.  It supports
the same commands as console.  You may use your web browser interact
with the interface.

If you go to http://localhost:8801/ you will get the help text.

The following are examples:

Go to:  
    http://localhost:8801/ping wibble
See:
    things.pingpong:OK:ETERNAL_HAPPINESS::pong=wibble:

Go to:  
    http://localhost:8801/ps
See:
    things.processlist:OK:ETERNAL_HAPPINESS:>completed:
     >header=tag|id|clearance|runtime|name|state|state.numeric
     >entry=S3|kernel.service.3.consoleservice|PRIVILEGED|164486|ConsoleService|Running|400
     >entry=S1|kernel.service.1.httptoolservice|PRIVILEGED|164486|HTTPToolService|Running|400
     >entry=S4|kernel.service.4.cli|PRIVILEGED|164470|CLIService|Running|400
     >entry=S5|kernel.service.5.localcommandservice|PRIVILEGED|164470|LocalCommandService|Running|400
     >entry=K0|kernel|EXTREME|164548|kernel|Running|400
     >entry=S2|kernel.service.2.httpcliservice|PRIVILEGED|164486|HttpService|Running|400

If you are using a regular web browser, you can use plain spaces between the 
command arguments (as shown in the 'wing wibble' example above).  you can also
use '%20'.  So 'http://localhost:8801/ping wibble' would turn into
'http://localhost:8801/ping%20wibble'

-- HTTP SERVER --------------------------------------------
The server implements a simple http server and an interface to write
applications that use it.  There are just a couple very simple pages included
with this distribtion.  The server listens on port 8802.

Going to http://localhost:8802/ will give a simple page that looks like
this:
        WELCOME TO THINGS

        Simple implementation

        nothing really here.

Going to http://localhost:8802/PS will show the process list that looks like
this:
        THINGS - PROCESS LIST

        header=tag|id|clearance|runtime|name|state|state.numeric

        >entry=S3|kernel.service.3.consoleservice|PRIVILEGED|211672|ConsoleService|Running|400
        >entry=S1|kernel.service.1.httptoolservice|PRIVILEGED|211672|HTTPToolService|Running|400
        >entry=S4|kernel.service.4.cli|PRIVILEGED|211656|CLIService|Running|400
        >entry=S5|kernel.service.5.localcommandservice|PRIVILEGED|211656|LocalCommandService|Running|400
        >entry=K0|kernel|EXTREME|211750|kernel|Running|400
        >entry=S2|kernel.service.2.httpcliservice|PRIVILEGED|211672|HttpService|Running|400

===============================================================================
ADVANCED CONFIGURATION

- The Command Interface (8001) and Http Server (8002) ports can be changed in 
the file etc/basic_config.prop.  The server listens on no other ports.

