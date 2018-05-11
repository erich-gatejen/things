@call ./set-build-env.bat
@echo NOTE: Sometimes this appears to hang.  Hold ENTER
@echo to get it to unstick.  It doesn't do this with other
@echo methods of running the test--just through ant.  I
@echo don't have time to worry about it now.
@echo
%ANT_HOME%\bin\ant %1 run.systemtest


