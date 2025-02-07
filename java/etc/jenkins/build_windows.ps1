param (
    [Parameter(Mandatory=$true)]
	[string]$vsGen,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber
)

Set-PSDebug -Trace 1

# Build Couchbase Lite Java for Windows, Community Edition
$toolsDir = "$PSScriptRoot\..\..\..\..\common\tools"

Write-Host "======== BUILD Couchbase Lite Java for Windows, Community Edition"
Write-Host "======== Clean up"
& $toolsDir/clean_litecore.ps1

Write-Host "======== Download Lite Core"
& $toolsDir/fetch_java_litecore.ps1 -Edition "CE"

Write-Host "======== Build Java"
$process = Start-Process -FilePath "$PSScriptRoot\..\..\gradlew.bat" -ArgumentList "--no-daemon ciBuild -PbuildNumber=$buildNumber" -PassThru -Wait
if($process.ExitCode -ne 0){
    Write-Host "Failed with error" $process.ExitCode 
    exit $process.ExitCode
}

Write-Host "======== BUILD COMPLETE"
exit 0

