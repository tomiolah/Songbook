[Setup]
AppName=Projector
AppVersion=2.6.1
DefaultDirName={sd}\Projector
DisableDirPage=no
; Since no icons will be created in "{group}", we don't need the wizard
; to ask for a Start Menu folder name:
DisableProgramGroupPage=yes
UninstallDisplayIcon={app}\Projector.exe
Compression=lzma2
SolidCompression=yes
OutputDir=..\Projector-server\src\main\resources\static
OutputBaseFilename=projector-setup

[Files]     
Source: "..\Projector-server\src\main\resources\static\projector.exe"; DestDir: "{app}"
Source: "forSetup\data\projector.mv.db"; DestDir: "{app}\data"
Source: "forSetup\data\projector.trace.db"; DestDir: "{app}\data" 
Source: "forSetup\data\database.version"; DestDir: "{app}\data"
Source: "forSetup\songVersTimes"; DestDir: "{app}"
Source: "forSetup\settings.ini"; DestDir: "{app}"
Source: "forSetup\projector.log"; DestDir: "{app}"
Source: "forSetup\recent.txt"; DestDir: "{app}"

[Icons]
Name: "{commonprograms}\Projector"; Filename: "{app}\Projector.exe"
Name: "{commondesktop}\Projector"; Filename: "{app}\Projector.exe"

[Run]
Filename: "{app}\projector.exe"; WorkingDir: "{app}"; Description: "{cm:LaunchProgram,Projector}"; Flags: nowait postinstall skipifsilent