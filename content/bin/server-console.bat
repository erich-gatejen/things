@echo off
@set CLASSPATH=$install.root$/lib/things.jar;$install.root$/lib;$java.jdk$\lib\tools.jar
$java.jdk$\bin\java.exe things.thinger.kernel.basic.KernelBasic_System_Bootstrap $install.root$/etc/basic_config.prop

