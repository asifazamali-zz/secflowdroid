
import Main.java.Config;
import Main.java.Util;
import beaver.Scanner;
import soot.*;
import soot.SootClass;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;

import java.util.HashSet;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        
        /*first run APIMonitor*/ 
        
        /* apk path and processedFile contains absolute path of apk and log file generated from APIMonitor */
        /* can also passed with command line arguments */
        String apkPath = null,processedFile =null;
        apkPath = "/home/asif/MTP/Experiments/apimonitor/APIMonitor-beta/apks_old/GPS_SMS.apk";
        processedFile = "/home/asif/MTP/Experiments/apimonitor/APIMonitor-beta/logs/GPS_SMS.log";
        //        apkPath = args[0];
//        processedFile = args[1];
        if(apkPath == null || processedFile == null){
            System.out.println("Provide apkPath and logFile");
            System.exit(1);
        }
        generateCGForAPK(
                // TODO @Yuxuan, replace this with your APK path
                apkPath,
                "output", 
                "android.jar",
                processedFile,
                "sensitive_apis.txt", /*sensitive api (source) file */
                "global_output_apis.txt"); /*global output (sink) file */

    }

    public static void generateCGForAPK(String apkPath, String outputPath, String sdkPath,String processedFile,String sensitive_apis,String global_output_apis) {
//        System.out.println(global_output_apis);
        String[] args = {"-i", apkPath, "-o", outputPath, "-sdk", sdkPath,"-file",processedFile,"-sensitive_apis",sensitive_apis,"-global_output_apis",global_output_apis};
        String appPackageName="";
        if (!Config.parseArgs(args)) {
            return;
        }
        Config.init();
        /////////////////////////////////////////
        try
        {
            /*get appPackageName */
            
            ProcessManifest processManifest = new ProcessManifest(apkPath);
            appPackageName = processManifest.getPackageName();
            System.out.println(appPackageName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
           Util.processing(apkPath);
        
        

    }


}
