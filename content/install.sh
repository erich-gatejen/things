#!/bin/sh

function print_help {
	echo "FORMAT: install.sh install_root jdk_location " 
	echo "  install_root = directory where things is installed."
	echo "  java_install = directory where the java jdk installed."
	echo "EXAMPLE ./install.sh /opt/things /usr/java/jdk1.6.0_03"
	echo "The root is usually the same directory where you found this install.sh."
	echo "This is a one-time script; you get to do it only once.  If you want to"
	echo "reinstall, you need to unpack the whole package again and run the script."
	echo
}

# Validate the parameters
if [ $# -lt 2 ] ; then
	echo "FAULT[99] Bad parameters."
	print_help
	exit 99
fi

# check java
$2/bin/java -version  2>&1 | grep -qi "Java(TM)" 
if [ $? -ne 0 ] ; then
	echo "FAULT[99] Bad java executable.  Did you give the right path as the second argument?"
	print_help
	exit 99
fi 

# check for the profile setup
if [ ! -d "/etc/profile.d" ]; then
	echo "PANIC[999] Expecting /etc/profile.d directory to be present."
	echo "   There is something wrong about this server.  Cannot install."
	print_help
	exit 99
fi 

# check root - see if the install.sh is there
if [ ! -f "$1/install.sh" ]; then
	echo "FAULT[99] Bad install root.  Did you give the right path as the first argument?"
	echo "  Expecting to find the file 'install.sh' under the root."
	print_help
	exit 99
fi 

echo "START INSTALL. "   

# Create install file
echo "# AUTO-GENERATED INSTALL CONFIGURATION." > "$1/etc/install.config.auto"
echo "java.jdk=$2" >> "$1/etc/install.config.auto"
echo "install.root=$1" >> "$1/etc/install.config.auto"
echo "" >> "$1/etc/install.config.auto"

# checkpoint factory
$2/bin/java -classpath $1/lib/thingssystem.jar:$1/lib/commons-collections-3.1.jar things.common.configuration.ConfigureByProps checkpoint "$1/etc/config.layout" "$1" "$1/etc/checkpoint/factory"

#  configure
$2/bin/java -classpath $1/lib/thingssystem.jar:$1/lib/commons-collections-3.1.jar things.common.configuration.ConfigureByProps configure "$1/etc/config.layout" "$1/etc/checkpoint/factory" "$1" "$1/etc/install.config.auto" 

# checkpoint after install
$2/bin/java -classpath $1/lib/thingssystem.jar:$1/lib/commons-collections-3.1.jar things.common.configuration.ConfigureByProps checkpoint "$1/etc/config.layout" "$1" "$1/etc/checkpoint/install"

# Fix files and clear or make log
chmod 755 $1/bin/*
mkdir -p $1/log
rm -rf $1/log/*

# Get rid of DOS files.
rm -f $1/bin/*.bat
rm -f ./install.bat

# Get rid of the installer.  Can't run it again.
rm -f ./install.sh

echo "INSTALLATION COMPLETE! "   
echo "Restart your shell before using.  You might find it convenient to put"
echo "$1/bin in your path in .profile or .bash_profile.  Also, various configurations"
echo "can be made in the file $1/etc/basic_config.prop, including the debugging level"
echo "for logging.  You may edit this file at any time."



