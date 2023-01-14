[Setup]
AppName=Projector
AppId=Projector_3
AppVersion=3.1.4
AppVerName=Projector
AppPublisher=SongPraise.com
AppPublisherURL=http://www.songpraise.com/#/desktop-app
UsePreviousAppDir=yes
DefaultDirName={sd}\Projector
DisableDirPage=no
; Since no icons will be created in "{group}", we don't need the wizard
; to ask for a Start Menu folder name:
DisableProgramGroupPage=yes
UninstallDisplayIcon={app}\Projector.exe
Compression=lzma2
SolidCompression=yes
OutputDir=..\Projector-server\aPublic_folder
OutputBaseFilename=projector-setup
SetupIconFile=projector_icon.ico

[Files]     
Source: ".\build\jpackage\Projector\Projector.exe"; DestDir: "{app}"
Source: ".\build\jpackage\Projector\Projector.ico"; DestDir: "{app}"
Source: ".\build\jpackage\Projector\app\.jpackage.xml"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\Projector-Desktop.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\Projector.cfg"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\converter-gson-2.3.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\gson-2.8.6.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\h2-1.4.193.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-base-19-win.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-base-19.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-controls-19-win.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-controls-19.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-fxml-19-win.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-graphics-19-win.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\javafx-graphics-19.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\jnativehook-2.1.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\log4j-api-2.9.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\log4j-core-2.9.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\log4j-slf4j-impl-2.9.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\logging-interceptor-3.8.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\okhttp-3.8.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\okio-1.13.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\ormlite-core-4.48.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\ormlite-jdbc-4.48.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\projector-common.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\retrofit-2.3.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\slf4j-api-1.7.25.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\app\sqlite-jdbc-3.39.3.0.jar"; DestDir: "{app}\app"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-console-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-console-l1-2-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-datetime-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-debug-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-errorhandling-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-file-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-file-l1-2-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-file-l2-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-handle-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-heap-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-interlocked-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-libraryloader-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-localization-l1-2-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-memory-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-namedpipe-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-processenvironment-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-processthreads-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-processthreads-l1-1-1.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-profile-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-rtlsupport-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-string-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-synch-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-synch-l1-2-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-sysinfo-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-timezone-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-core-util-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-conio-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-convert-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-environment-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-filesystem-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-heap-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-locale-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-math-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-multibyte-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-private-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-process-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-runtime-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-stdio-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-string-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-time-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\api-ms-win-crt-utility-l1-1-0.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\awt.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\fontmanager.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\freetype.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\instrument.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\java.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\java.exe"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\javajpeg.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\javaw.exe"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jawt.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jfr.exe"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jimage.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jli.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jrunscript.exe"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\jsound.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\keytool.exe"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\lcms.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\management.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\mlib_image.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\msvcp140.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\net.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\nio.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\prefs.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\server\jvm.dll"; DestDir: "{app}\runtime\bin\server"
Source: ".\build\jpackage\Projector\runtime\bin\splashscreen.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\ucrtbase.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\vcruntime140.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\verify.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\bin\zip.dll"; DestDir: "{app}\runtime\bin"
Source: ".\build\jpackage\Projector\runtime\conf\logging.properties"; DestDir: "{app}\runtime\conf"
Source: ".\build\jpackage\Projector\runtime\conf\net.properties"; DestDir: "{app}\runtime\conf"
Source: ".\build\jpackage\Projector\runtime\conf\security\java.policy"; DestDir: "{app}\runtime\conf\security"
Source: ".\build\jpackage\Projector\runtime\conf\security\java.security"; DestDir: "{app}\runtime\conf\security"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\README.txt"; DestDir: "{app}\runtime\conf\security\policy"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\limited\default_US_export.policy"; DestDir: "{app}\runtime\conf\security\policy\limited"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\limited\default_local.policy"; DestDir: "{app}\runtime\conf\security\policy\limited"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\limited\exempt_local.policy"; DestDir: "{app}\runtime\conf\security\policy\limited"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\unlimited\default_US_export.policy"; DestDir: "{app}\runtime\conf\security\policy\unlimited"
Source: ".\build\jpackage\Projector\runtime\conf\security\policy\unlimited\default_local.policy"; DestDir: "{app}\runtime\conf\security\policy\unlimited"
Source: ".\build\jpackage\Projector\runtime\conf\sound.properties"; DestDir: "{app}\runtime\conf"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\LICENSE"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\aes.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\asm.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\c-libutl.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\cldr.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\icu.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\public_suffix.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\unicode.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\wepoll.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.base\zlib.md"; DestDir: "{app}\runtime\legal\java.base"
Source: ".\build\jpackage\Projector\runtime\legal\java.compiler\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.compiler"
Source: ".\build\jpackage\Projector\runtime\legal\java.compiler\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.compiler"
Source: ".\build\jpackage\Projector\runtime\legal\java.compiler\LICENSE"; DestDir: "{app}\runtime\legal\java.compiler"
Source: ".\build\jpackage\Projector\runtime\legal\java.datatransfer\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.datatransfer"
Source: ".\build\jpackage\Projector\runtime\legal\java.datatransfer\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.datatransfer"
Source: ".\build\jpackage\Projector\runtime\legal\java.datatransfer\LICENSE"; DestDir: "{app}\runtime\legal\java.datatransfer"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\LICENSE"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\colorimaging.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\freetype.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\giflib.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\harfbuzz.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\jpeg.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\lcms.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\libpng.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.desktop\mesa3d.md"; DestDir: "{app}\runtime\legal\java.desktop"
Source: ".\build\jpackage\Projector\runtime\legal\java.instrument\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.instrument"
Source: ".\build\jpackage\Projector\runtime\legal\java.instrument\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.instrument"
Source: ".\build\jpackage\Projector\runtime\legal\java.instrument\LICENSE"; DestDir: "{app}\runtime\legal\java.instrument"
Source: ".\build\jpackage\Projector\runtime\legal\java.logging\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.logging"
Source: ".\build\jpackage\Projector\runtime\legal\java.logging\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.logging"
Source: ".\build\jpackage\Projector\runtime\legal\java.logging\LICENSE"; DestDir: "{app}\runtime\legal\java.logging"
Source: ".\build\jpackage\Projector\runtime\legal\java.management\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.management"
Source: ".\build\jpackage\Projector\runtime\legal\java.management\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.management"
Source: ".\build\jpackage\Projector\runtime\legal\java.management\LICENSE"; DestDir: "{app}\runtime\legal\java.management"
Source: ".\build\jpackage\Projector\runtime\legal\java.naming\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.naming"
Source: ".\build\jpackage\Projector\runtime\legal\java.naming\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.naming"
Source: ".\build\jpackage\Projector\runtime\legal\java.naming\LICENSE"; DestDir: "{app}\runtime\legal\java.naming"
Source: ".\build\jpackage\Projector\runtime\legal\java.prefs\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.prefs"
Source: ".\build\jpackage\Projector\runtime\legal\java.prefs\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.prefs"
Source: ".\build\jpackage\Projector\runtime\legal\java.prefs\LICENSE"; DestDir: "{app}\runtime\legal\java.prefs"
Source: ".\build\jpackage\Projector\runtime\legal\java.scripting\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.scripting"
Source: ".\build\jpackage\Projector\runtime\legal\java.scripting\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.scripting"
Source: ".\build\jpackage\Projector\runtime\legal\java.scripting\LICENSE"; DestDir: "{app}\runtime\legal\java.scripting"
Source: ".\build\jpackage\Projector\runtime\legal\java.security.sasl\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.security.sasl"
Source: ".\build\jpackage\Projector\runtime\legal\java.security.sasl\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.security.sasl"
Source: ".\build\jpackage\Projector\runtime\legal\java.security.sasl\LICENSE"; DestDir: "{app}\runtime\legal\java.security.sasl"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql.rowset\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.sql.rowset"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql.rowset\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.sql.rowset"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql.rowset\LICENSE"; DestDir: "{app}\runtime\legal\java.sql.rowset"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.sql"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.sql"
Source: ".\build\jpackage\Projector\runtime\legal\java.sql\LICENSE"; DestDir: "{app}\runtime\legal\java.sql"
Source: ".\build\jpackage\Projector\runtime\legal\java.transaction.xa\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.transaction.xa"
Source: ".\build\jpackage\Projector\runtime\legal\java.transaction.xa\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.transaction.xa"
Source: ".\build\jpackage\Projector\runtime\legal\java.transaction.xa\LICENSE"; DestDir: "{app}\runtime\legal\java.transaction.xa"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\LICENSE"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\bcel.md"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\dom.md"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\jcup.md"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\xalan.md"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\java.xml\xerces.md"; DestDir: "{app}\runtime\legal\java.xml"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.jfr\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\jdk.jfr"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.jfr\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\jdk.jfr"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.jfr\LICENSE"; DestDir: "{app}\runtime\legal\jdk.jfr"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.unsupported\ADDITIONAL_LICENSE_INFO"; DestDir: "{app}\runtime\legal\jdk.unsupported"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.unsupported\ASSEMBLY_EXCEPTION"; DestDir: "{app}\runtime\legal\jdk.unsupported"
Source: ".\build\jpackage\Projector\runtime\legal\jdk.unsupported\LICENSE"; DestDir: "{app}\runtime\legal\jdk.unsupported"
Source: ".\build\jpackage\Projector\runtime\lib\classlist"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\fontconfig.bfc"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\fontconfig.properties.src"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\jawt.lib"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\jfr\default.jfc"; DestDir: "{app}\runtime\lib\jfr"
Source: ".\build\jpackage\Projector\runtime\lib\jfr\profile.jfc"; DestDir: "{app}\runtime\lib\jfr"
Source: ".\build\jpackage\Projector\runtime\lib\jrt-fs.jar"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\jvm.cfg"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\jvm.lib"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\modules"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\psfont.properties.ja"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\psfontj2d.properties"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\security\blocked.certs"; DestDir: "{app}\runtime\lib\security"
Source: ".\build\jpackage\Projector\runtime\lib\security\cacerts"; DestDir: "{app}\runtime\lib\security"
Source: ".\build\jpackage\Projector\runtime\lib\security\default.policy"; DestDir: "{app}\runtime\lib\security"
Source: ".\build\jpackage\Projector\runtime\lib\security\public_suffix_list.dat"; DestDir: "{app}\runtime\lib\security"
Source: ".\build\jpackage\Projector\runtime\lib\tzdb.dat"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\lib\tzmappings"; DestDir: "{app}\runtime\lib"
Source: ".\build\jpackage\Projector\runtime\release"; DestDir: "{app}\runtime"
; Source: "..\Projector-server\src\main\resources\static\projector.exe"; DestDir: "{app}"
Source: "forSetup\data\projector.mv.db"; DestDir: "{app}\data"
Source: "forSetup\data\projector.trace.db"; DestDir: "{app}\data" 
Source: "forSetup\data\database.version"; DestDir: "{app}\data"
Source: "forSetup\songVersTimes"; DestDir: "{app}"
Source: "forSetup\settings.ini"; DestDir: "{app}"
Source: "forSetup\projector.log"; DestDir: "{app}"
Source: "forSetup\recent.txt"; DestDir: "{app}"
Source: "forSetup\application.version"; DestDir: "{app}"
Source: "forSetup\app\projector-updater.jar"; DestDir: "{app}\app"
Source: "forSetup\app\updater.cfg"; DestDir: "{app}\app"
Source: "forSetup\updater.ico"; DestDir: "{app}"
Source: "forSetup\updater.exe"; DestDir: "{app}"

[Icons]
Name: "{commonprograms}\Projector"; Filename: "{app}\Projector.exe"
Name: "{commondesktop}\Projector"; Filename: "{app}\Projector.exe"

[Run]
Filename: "{app}\projector.exe"; WorkingDir: "{app}"; Description: "{cm:LaunchProgram,Projector}"; Flags: nowait postinstall skipifsilent