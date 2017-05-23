import beaver.Scanner;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;

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
                "/home/asif/MTP/Experiments/APK_CG/APK_CG/FileInputOutput.apk",
                "output",
                "android.jar",
                "/home/asif/MTP/Experiments/apimonitor/APIMonitor-beta/processing/temp.txt");

    }

    public static void generateCGForAPK(String apkPath, String outputPath, String sdkPath,String processedFile) {
        String[] args = {"-i", apkPath, "-o", outputPath, "-sdk", sdkPath,"-file",processedFile};
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
        /////////////////////////////////////////
        CallGraph cg = Util.generateCG();
        // TODO @Yuxuan, replace this with your cg2dot
        Util.printCG(cg, Config.getResultPs(),appPackageName);
    }

}
