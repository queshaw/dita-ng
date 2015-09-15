@echo off

set PLUGIN_ID=org.dita-ng.doctypes

verify>nul
call :have_cmdextensions 2>nul
if not errorlevel 1 goto no_cmdextensions

: look for dita.bat
if exist "%~dp0\dita.bat" goto have_dita_bat
echo Did not find dita.bat in "%~dp0". 1>&2
exit /b 0

:have_dita_bat
set dita_cmd=%~dp0dita.bat
pushd %~dp0..
if defined DITA_HOME goto define_dita_home
set no_dita_home=1

:define_dita_home
set DITA_HOME=%CD%
popd

: check for -install/-uninstall
set dita_ng_tmp_file="%TEMP%\dita-ng%random%.temp"
echo %* > %dita_ng_tmp_file%

findstr /c:-install %dita_ng_tmp_file% >nul
if not errorlevel 0 goto set_no_classpath
findstr /c:-uninstall %dita_ng_tmp_file% >nul
if not errorlevel 0 goto set_no_classpath


: set dita.dir for -install/-uninstall
if defined ANT_OPTS goto have_ant_opts
set no_ant_opts=1
set ANT_OPTS=-Ddita.dir=%DITA_HOME%
goto set_the_classpath

:have_ant_opts
set ANT_OPTS=%ANT_OPTS% -Ddita.dir=%DITA_HOME%

:set_the_classpath
set plugin_dir=%DITA_HOME%\plugins\%PLUGIN_ID%
set CLASSPATH=%plugin_dir%
set CLASSPATH=%CLASSPATH%;%plugin_dir%\lib\dita-ng.jar
set CLASSPATH=%CLASSPATH%;%plugin_dir%\lib\jing.jar

:set_no_classpath
del %dita_ng_tmp_file% >nul

: invoke dita.bat
%dita_cmd% %*
if defined no_dita_home set DITA_HOME=
if defined no_ant_opts set ANT_OPTS=
exit /b %errorlevel%

:have_cmdextensions
verify error 2>nul
goto :eof

:no_cmdextensions
echo. 1>&2
echo This script depends on cmd.exe "command extensions" being enabled. 1>&2
echo. 1>&2
echo See "help cmd" for information (Command Extensions are enabled by default). 1>&2
echo. 1>&2

verify error 2>nul


