@echo off
setlocal

REM Delete old file
del *.jar
del *.tmp
del jar_manifes
..\ibuild\ibuild.cmd