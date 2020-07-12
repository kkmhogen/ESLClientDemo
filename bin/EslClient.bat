@echo off

echo path:%~dp0

set base=%~dp0

set class=%base%\eslDemo
set libs=%base%\..\lib

set class_path=%class%;%libs%\commons-beanutils-1.9.2.jar;%libs%\commons-collections-3.2.1.jar;%libs%\commons-lang-2.6.jar;%libs%\commons-logging.jar;%libs%\ezmorph-1.0.6.jar;%libs%\json-lib-2.4-jdk15.jar;%libs%\org.eclipse.paho.client.mqttv3-1.2.0.jar;%libs%\qrcode.jar;%libs%\pic2mqttdata.jar;

java -classpath %class_path% eslDemo.EslMain
@pause