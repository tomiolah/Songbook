call gradle jpackageImage
@REM call ant -buildfile build\jfx\app
@REM call copy ..\all.jar ..\Projector-server\src\main\resources\static\projector.jar
@REM call "C:\Program Files (x86)\Launch4j\launch4jc.exe" ..\launch4j.xml
call "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" setup.iss