@echo off 

:qualify
REM qualify the install
IF NOT EXIST %2\bin\java.exe  (
	echo JVM not found at %2\bin\java.exe.
	goto help
)
IF NOT EXIST %1\install.sh  (
	echo Things system not found at install_root.
	goto help
)

:install
REM Install files
echo # AUTO-GENERATED INSTALL CONFIGURATION. > %1\etc\install.config.auto
echo java.jdk=%2 >> %1\etc\install.config.auto
echo install.root=%1 >> %1\etc\install.config.auto

REM Run install
%2\bin\java -classpath %1\lib\thingssystem.jar;%1\lib\commons-collections-3.1.jar things.common.configuration.ConfigureByProps checkpoint %1\etc\config.layout %1 %1\etc\checkpoint\factory
%2\bin\java -classpath %1\lib\thingssystem.jar;%1\lib\commons-collections-3.1.jar things.common.configuration.ConfigureByProps configure %1\etc\config.layout %1\etc\checkpoint\factory %1 %1\etc\install.config.auto 
%2\bin\java -classpath %1\lib\thingssystem.jar;%1\lib\commons-collections-3.1.jar things.common.configuration.ConfigureByProps checkpoint %1\etc\config.layout %1 %1\etc\checkpoint\install

REM paths
echo set THINGS_ROOT=%1 >> %1\bin\setenv.bat

REM Fix files
mkdir %1\log
del /q %1\bin\*.sh
del /q %1\log\*.*
del /q .\install.*

echo INSTALLATION COMPLETE!    
echo Restart your shell before using.  You might find it convenient to put
echo %1\bin in your PATH.  Also, various configurations can 
echo be made in the file  %1\etc\basic_config.prop, 
echo including the debugging level for logging.  You may edit this file 
echo at any time.

goto end

:help
	echo FORMAT: install.bat install_root jdk_location 
	echo   install_root = directory where things is installed.
	echo   java_install = directory where the java jdk installed.
	echo EXAMPLE install.bat \opt\things \opt\jdk1.6.0_03
	echo The root is usually the same directory here you found this install.bat.
	echo This is a one-time script; you get to do it only once.  If you want to
	echo reinstall, you need to unpack the whole package again and run the script.
	echo.
goto end

:end

