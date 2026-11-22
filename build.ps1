param([string]$Sdk=$env:ANDROID_HOME,[string]$Jdk=$env:JAVA_HOME,[string]$Keystore,
      [string]$PasswordVariable='KQ_KEYSTORE_PASSWORD')
$ErrorActionPreference='Stop'
$projectPath=$PSScriptRoot
if(!$Jdk){$Jdk=(Get-ChildItem -Directory "$projectPath/tools/jdk17" | Select-Object -First 1).FullName}
if(!$Sdk){$Sdk=Join-Path $projectPath 'tools/sdk'}
$env:JAVA_HOME=$Jdk
$bt=Join-Path $Sdk 'bt/android-14'
if(!(Test-Path "$bt/aapt2.exe")){
    $bt=(Get-ChildItem -Directory "$Sdk/build-tools" | Where-Object {Test-Path "$($_.FullName)/d8.bat"} | Sort-Object Name -Descending | Select-Object -First 1).FullName
}
$api=Join-Path $Sdk 'platforms/android-35/android.jar'
if(!(Test-Path $api)){$api=Join-Path $Sdk 'pf/android-35/android.jar'}
$app=Join-Path $projectPath 'app/src/main'
$work=Join-Path $projectPath ('build/manual-'+[Guid]::NewGuid().ToString('N'))
$dist=Join-Path $projectPath 'dist'
$null=New-Item -Force -ItemType Directory "$work/classes","$work/dex","$work/verify",$dist
function Run([string]$exe,[string[]]$flags){
    & $exe @flags
    if($LASTEXITCODE -ne 0){throw "Build step failed: $exe ($LASTEXITCODE)"}
}
$javaFiles=@(Get-ChildItem -Recurse -File "$app/java" -Filter '*.java' | ForEach-Object FullName)
Run "$Jdk/bin/javac.exe" (@('--release','8','-encoding','UTF-8','-cp',$api,'-d',"$work/classes")+$javaFiles)
Add-Type -AssemblyName System.IO.Compression.FileSystem
[IO.Compression.ZipFile]::CreateFromDirectory("$work/classes","$work/classes.jar")
Run "$bt/d8.bat" @('--release','--min-api','24','--lib',$api,'--output',"$work/dex","$work/classes.jar")
Run "$bt/aapt2.exe" @('compile','--dir',"$app/res",'-o',"$work/res.zip")
$xml=[IO.File]::ReadAllText("$app/AndroidManifest.xml").Replace('<manifest ','<manifest package="com.chaos.keyquota" ')
[IO.File]::WriteAllText("$work/manifest.xml",$xml,[Text.UTF8Encoding]::new($false))
Run "$bt/aapt2.exe" @('link','-o',"$work/base.apk",'-I',$api,'--manifest',"$work/manifest.xml",'-R',"$work/res.zip",'--min-sdk-version','24','--target-sdk-version','34','--auto-add-overlay')
$assetRoot=(Get-Item -LiteralPath "$app/assets").FullName
$zip=[IO.Compression.ZipFile]::Open("$work/base.apk",'Update')
try {
    foreach($asset in Get-ChildItem -LiteralPath $assetRoot -Recurse -File){
        $relative=$asset.FullName.Substring($assetRoot.Length+1).Replace('\','/')
        $level=[IO.Compression.CompressionLevel]::Optimal
        if($asset.Extension -in '.mp4','.jpg','.jpeg'){
            $level=[IO.Compression.CompressionLevel]::NoCompression
        }
        $null=[IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,$asset.FullName,"assets/$relative",$level)
    }
    foreach($dex in Get-ChildItem "$work/dex/*.dex"){
        $null=[IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,$dex.FullName,$dex.Name)
    }
} finally {$zip.Dispose()}
Run "$bt/zipalign.exe" @('-f','-p','4',"$work/base.apk","$work/aligned.apk")
if(!$Keystore){$Keystore=Join-Path $projectPath 'tools/chaos.keystore'}
$pwdArg="env:$PasswordVariable"
if(![Environment]::GetEnvironmentVariable($PasswordVariable)){$pwdArg='pass:android'}
if(!(Test-Path $Keystore)){
    $Keystore=Join-Path $dist 'local-test.keystore'
    $pwdArg='pass:android'
    if(!(Test-Path $Keystore)){Run "$Jdk/bin/keytool.exe" @('-genkeypair','-keystore',$Keystore,'-alias','debug','-keyalg','RSA','-keysize','2048','-validity','3650','-storepass','android','-keypass','android','-dname','CN=Key Quota local build')}
}
$apk=Join-Path $dist 'KeyQuota-Android-2.1.9.apk'
Run "$bt/apksigner.bat" @('sign','--ks',$Keystore,'--ks-pass',$pwdArg,'--key-pass',$pwdArg,'--v4-signing-enabled','false','--out',$apk,"$work/aligned.apk")
Run "$bt/apksigner.bat" @('verify','--verbose',$apk)
Run "$bt/zipalign.exe" @('-c','-p','4',$apk)
Run "$Jdk/bin/javac.exe" @('--release','8','-encoding','UTF-8','-d',"$work/verify","$projectPath/tools/VerifyApkAssets.java")
Run "$Jdk/bin/java.exe" @('-cp',"$work/verify",'VerifyApkAssets',$apk,$assetRoot)
Write-Output $apk
