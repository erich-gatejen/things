@echo off
@set CLASSPATH=$install.root$/lib/things.jar;$install.root$/lib/thingstest.jar;$install.root$/lib;$java.jdk$\lib\tools.jar
$java.jdk$\bin\java.exe test.system.CLISystemSuiteTester  $install.root$/etc/test/system/suite.prop $install.root$/etc/test/system/basic_config.prop

