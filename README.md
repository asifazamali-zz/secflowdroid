# SecFlowDroid
secFlowDroid built on modifying soot-infoflow-android which is a part of flowDroid tool. It performs information flow analysis of android applications.



# Usage
1. Pull the source code and import as a intellij project.
2. Set the configurations as:
  a. Dependencies
  > Module SDK: Project SDK (1.7)
  > Java Version: 1.7
    Import jars
  > soot-infoflow.jar
  > soot-trunk.jar
  > slf4j-simple-1.7.5.jar
  > slf4j-api-1.7.5.jar
  > axml-2.0.jar
  > commons-cli-1.3.1.jar
3. Set path of `apk File` and `log file` from apiMonitor
3. Run `Main.java` from  source
For more information visit:
 APK_CG : https://github.com/ylimit/APK_CG  
 soot-infoflow-android-wiki: https://github.com/secure-software-engineering/soot-infoflow-android/wiki
 flowdroid: http://sseblog.ec-spride.de/android/flowdroid/
 
