@echo off
set JAR_PATH=Media-Browser.jar
set PROPERTIES_PATH=application.properties
java -jar %JAR_PATH% --spring.config.location=%PROPERTIES_PATH%
pause