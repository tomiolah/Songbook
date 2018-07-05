:loop
xcopy c:\workspace\Projector\projector-web\dist\static c:\workspace\Projector\Projector-server\out\production\resources\static /S /I /Y
pause
goto loop