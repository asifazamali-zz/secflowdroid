package Main.java;
import Main.java.MakeRWLabel;
import beaver.*;
import beaver.Scanner;
import callgraph.InformationFlowAnalysis;
import fj.Hash;
import fj.data.Array;
import ifc.LabelManager;
import soot.*;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.util.Chain;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static callgraph.ProcessLocalMethod.createObjId;

public class Util {
    public static final Logger LOGGER = Logger.getLogger("APK_CG");
    public static final String UNKNOWN = "<unknown>";
    public static ArrayList<String> classes= new ArrayList<>();
    public static ArrayList<String> methods= new ArrayList<>();
    public static ArrayList<String> apis= new ArrayList<>();
    public static ArrayList<String> apiMethod = new ArrayList<>();
    public static HashMap<String,ArrayList> sensitive_api_method = new HashMap<>();
    public static HashMap<String,ArrayList> global_output_api_method = new HashMap<>();
    public static HashSet<String> sensitive_class = new HashSet<>();
    public static String appPackageName="";
    public static HashMap<SootClass,ArrayList> classMethodDict = new HashMap<>();
    public static Dictionary dict_class_method = new Hashtable(); // SootClass-->{SootMethod}
    public static Dictionary dict_methodName_method = new Hashtable();//MethodName-->{SootMethod}
    public static LabelManager labelManager;
    public static MakeRWLabel makeRWLabel;
    public static PrintStream ps;
    public static Dictionary subLabel;
//    public static String getTimeString() {
//        long timeMillis = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
//        Date date = new Date(timeMillis);
//        return sdf.format(date);
//    }

//    public static void logException(Exception e) {
//        StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        Util.LOGGER.warning(sw.toString());
//    }
//
//    public static float safeDivide(int obfuscated, int total) {
//        if (total <= 0) return 1;
//        return (float) obfuscated / total;
//    }
//
//    public static CallGraph generateCG() {
//        PackManager.v().runPacks();
//        return Scene.v().getCallGraph();
//    }
//    
    
    public static void readLogFile(){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.filesToProcess));
            String line = bufferedReader.readLine();
            HashSet<String> hashSet = new HashSet<>();
            while(line!=null)
            {
                String[] parts = line.split(":");
                System.out.println(line);
                System.out.println("parts:" + parts[1]);
                String[] partsSplit = parts[1].split("->");
                String key = "partSplit:" + partsSplit[0] + " " + partsSplit[1];
                System.out.println("partSplit:" + partsSplit[0] + " " + partsSplit[1] + " " + partsSplit[2] + partsSplit[3]);
                if (!hashSet.contains(key)){
                    classes.add(partsSplit[0]);
                    System.out.println(partsSplit[0]);
                    methods.add(trimMethods(partsSplit[1]));
                    apis.add(partsSplit[2]);
                    apiMethod.add(trimApiMethod(partsSplit[3]));
                    hashSet.add(key);
                }
                line = bufferedReader.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void readSensitiveApiFile(){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.sensitiveApiFile));
            System.out.println("sensitive_apis.txt");
            String line = bufferedReader.readLine();
            while(line!=null){
                String[] parts = line.split("->");
                System.out.println(line);
                if(parts.length >1)
                {
                    if (sensitive_api_method.get(parts[0]) != null)
                        sensitive_api_method.get(parts[0]).add(convertClass(parts[0]));
                    else
                    {
                        ArrayList<String> stringArrayList = new ArrayList();
                        stringArrayList.add(parts[1]);
                        sensitive_api_method.put(convertClass(parts[0]), stringArrayList);
                    }
                
                }
                else{
                    sensitive_class.add(convertClass(line));
                }
                line = bufferedReader.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void readGlobalOutputFile(){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.outputGlobalFile));
            String line = bufferedReader.readLine();
            while(line!=null){
                System.out.println(line);
                String[] parts = line.split("->");
                if(global_output_api_method.get(parts[0]) != null)
                    global_output_api_method.get(convertClass(parts[0])).add(parts[1]);
                else{
                    ArrayList<String> stringArrayList = new ArrayList();
                    stringArrayList.add(parts[1]);
                    global_output_api_method.put(convertClass(parts[0]),stringArrayList);
                }
                line = bufferedReader.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void processing(String apkPath){
        try
        {
            ProcessManifest processManifest = new ProcessManifest(apkPath);
            appPackageName = processManifest.getPackageName();
            System.out.println(appPackageName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        ps = Config.getResultPs();
        System.out.println("Reading log file");
        readLogFile();
        System.out.println("Reading sensitiveApi file");
        readSensitiveApiFile();
        System.out.println("Reading global output file");
        readGlobalOutputFile();
        Chain<SootClass> classes = Scene.v().getClasses();
        Iterator<SootClass> sootClassIterator = classes.iterator();
        HashSet<SootClass> sootClassHashSet = new HashSet<SootClass>();
        System.out.println("package name"+ appPackageName);
        while(sootClassIterator.hasNext())
        {
            SootClass sootClass = sootClassIterator.next();
            String className = sootClass.getName();
            if(className.contains(appPackageName))
            {
                sootClassHashSet.add(sootClass);
                List<SootMethod> methodList = sootClass.getMethods();
                Iterator<SootMethod> methodIterator = methodList.iterator();
                System.out.println("className "+className);
                while(methodIterator.hasNext()){
                    SootMethod sootMethod = methodIterator.next();
                      System.out.println(sootMethod.getName());
                    dict_methodName_method.put(sootMethod.getName(),sootMethod);
                }
                dict_class_method.put(sootClass,methodList);
            }    
        }
//        getJimpleFile();   <-----------gives only jimple file

       flowControl();        //<----------- labeling
    }
    public static void getJimpleFile()
    {
        for (int i = 0; i < classes.size(); i++)
        {
            ps.println("---------------------------------------------------");
            ps.println(classes.get(i) + "." + methods.get(i) + "." + apis.get(i));
            ps.println("---------------------------------------------------");

            SootMethod sootMethod = (SootMethod) dict_methodName_method.get(methods.get(i));

            if(sootMethod != null)
            {
                Body b = sootMethod.retrieveActiveBody();
                UnitGraph unitGraph = new ExceptionalUnitGraph(b);
                Iterator itr = unitGraph.iterator();
                while (itr.hasNext())
                {
                    Unit u = (Unit) itr.next();
                    ps.println(u.toString());
                    //                ps.print("Out : ");
                    //                ps.println(OUT.toString() + "\n");
                }
            }
        }
    }
    public static void flowControl(){
        Util.ps.println("flowAnalysis");
        
        createSubjectLabel();
        for(int i =0;i<classes.size();i++){
            ps.println("---------------------------------------------------");
            ps.println(classes.get(i)+"."+methods.get(i)+"."+apis.get(i));
            ps.println("---------------------------------------------------");
            System.out.println("---------------------------------------------------");
            System.out.println(classes.get(i)+"."+methods.get(i)+"."+apis.get(i));
            System.out.println("---------------------------------------------------");
            SootMethod sootMethod = (SootMethod) dict_methodName_method.get(methods.get(i));
            if(sootMethod != null)
            {
                Body b = sootMethod.retrieveActiveBody();
                UnitGraph unitGraph = new ExceptionalUnitGraph(b);
                InformationFlowAnalysis informationFlowAnalysis = new InformationFlowAnalysis(unitGraph, labelManager, subLabel, convertClass(classes.get(i)), methods.get(i));
//                Iterator itr = unitGraph.iterator();
//                while (itr.hasNext())
//                {
//                    Unit u = (Unit) itr.next();
//                    ps.println(u.toString());
//                }
            }
        }
    }
    

    
    public static String convertClass(String str){ 
        String str1 = str.split("L",2)[1];
        String str2 = str1.replace('/','.');
        str1 =str2.split(";",2)[0];
        return str1;
    }
    public static void createSubjectLabel(){
        makeRWLabel = new MakeRWLabel();
        HashSet<String> subOwner = new HashSet<>();
        subOwner.add(appPackageName);
        HashSet<String> readers = new HashSet<>();
        readers.add(appPackageName);
        readers.add("public");
        
        subLabel = makeRWLabel.makeSubLabel(subOwner,readers); // subjectLevel(packageName,packageName,packageName)
        labelManager = new LabelManager();
        labelManager.saveLabel("S1",subLabel);

    }
    public static String trimMethods(String str){
       return str.split("\\(",2)[0];
    }
    public static String trimApiMethod(String str){
       return str.split("\\(",2)[0]; 
    }
    public static void printLabel(String var, String className, String methodName){
//        String obj_id = createObjId(var,className,methodName);
        System.out.println(var+" "+labelManager.getLabel(var,className,methodName));
    }
    
}
