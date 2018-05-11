@echo off
@set CLASSPATH=$install.root$/lib/things.jar;$install.root$/lib/thingstest.jar;$install.root$/lib;$java.jdk$\lib\tools.jar
$java.jdk$\bin\java test.things.CLIThingsUnitSuiteTester $install.root$/etc/test/things/suite.prop

