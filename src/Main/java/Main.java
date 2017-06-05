
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
        

//        if (!Config.parseArgs(args)) {
//            return;
//        }
//
//        Config.init();
//        CallGraph cg = Util.generateCG();
//        Util.printCG(cg, System.out);

        generateCGForAPK(
                // TODO @Yuxuan, replace this with your APK path
                "/home/asif/MTP/Experiments/apimonitor/APIMonitor-beta/apks_old/GPS_SMS.apk",
                "output",
                "android.jar",
                "/home/asif/MTP/Experiments/apimonitor/APIMonitor-beta/logs/GPS_SMS.log",
                "sensitive_apis.txt",
                "global_output_apis.txt");

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
            ProcessManifest processManifest = new ProcessManifest(apkPath);
            appPackageName = processManifest.getPackageName();
            System.out.println(appPackageName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
           Util.processing(apkPath);
        
        
        /////////////////////////////////////////
//        Chain<SootClass> classes = Scene.v().getClasses();
//        Iterator<SootClass> sootClassIterator = classes.iterator();
//        HashSet<SootClass> sootClassHashSet = new HashSet<SootClass>();
//        while(sootClassIterator.hasNext())
//        {
//            SootClass sootClass = sootClassIterator.next();
//            String className = sootClass.getName();
//            if(className.contains(appPackageName))
//                sootClassHashSet.add(sootClass);
//        }
//        printSootClassHashSet(sootClassHashSet); 
//        CallGraph cg = Util.generateCG();
//        // TODO @Yuxuan, replace this with your cg2dot
//        Util.printCG(cg, Config.getResultPs(),appPackageName);
    }
//    public static void printSootClassHashSet(HashSet<SootClass> sootClassHashSet){
//        Iterator<SootClass> sootClassIterator = sootClassHashSet.iterator();
//        while(sootClassIterator.hasNext()){
//            String className = sootClassIterator.next();
//            System.out.println("class:"+className
//        }
//    }

}
