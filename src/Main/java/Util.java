import beaver.*;
import beaver.Scanner;
import fj.Hash;
import soot.*;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Util {
    public static final Logger LOGGER = Logger.getLogger("APK_CG");
    public static final String UNKNOWN = "<unknown>";
    public static ArrayList<String> classes= new ArrayList<>();
    public static ArrayList<String> methods= new ArrayList<>();
    public static ArrayList<String> apis= new ArrayList<>();

    public static String appPackageName="";
//    public static HashMap<SootClass,ArrayList> classMethodDict = new HashMap<>();
    public static Dictionary dict_class_method = new Hashtable(); // SootClass-->{SootMethod}
    public static Dictionary dict_methodName_method = new Hashtable();//MethodName-->{SootMethod}

    public static String getTimeString() {
        long timeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
        Date date = new Date(timeMillis);
        return sdf.format(date);
    }

    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Util.LOGGER.warning(sw.toString());
    }

    public static float safeDivide(int obfuscated, int total) {
        if (total <= 0) return 1;
        return (float) obfuscated / total;
    }

    public static CallGraph generateCG() {
        PackManager.v().runPacks();
        return Scene.v().getCallGraph();
    }
    public static void readLogFile(){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.filesToProcess));
            String line = bufferedReader.readLine();
            while(line!=null){
                String[] parts = line.split(":");
                System.out.println(line);
                System.out.println("parts:"+parts[1]);
                String[] partsSplit = parts[1].split("->");
                System.out.println("partSplit:"+partsSplit[0]+" "+partsSplit[1]+" "+partsSplit[2]);
                classes.add(partsSplit[0]);
                methods.add(trimMethods(partsSplit[1]));
                apis.add(trimApis(partsSplit[2]));
                line = bufferedReader.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Classes");
//        printArrayListString(classes);
        System.out.println("Methods");
//        printArrayListString(methods);
        System.out.println("apis");
//        printArrayListString(apis);
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
        readLogFile();
        Chain<SootClass> classes = Scene.v().getClasses();
        Iterator<SootClass> sootClassIterator = classes.iterator();
        HashSet<SootClass> sootClassHashSet = new HashSet<SootClass>();
        while(sootClassIterator.hasNext())
        {
            SootClass sootClass = sootClassIterator.next();
            String className = sootClass.getName();
//            System.out.println("Preprocess ClassName:"+className);
            if(className.contains(appPackageName))
            {
                sootClassHashSet.add(sootClass);
                List<SootMethod> methodList = sootClass.getMethods();
                Iterator<SootMethod> methodIterator = methodList.iterator();
                while(methodIterator.hasNext()){
                    SootMethod sootMethod = methodIterator.next();
//                    System.out.println(sootMethod.getName());
                    dict_methodName_method.put(sootMethod.getName(),sootMethod);
                }
                dict_class_method.put(sootClass,methodList);
            }    
        }
        flowControl();
    }
    
    public static void flowControl(){
        System.out.println("flowAnalysis");
        for(int i =0;i<classes.size();i++){
            System.out.println("---------------------------------------------------");
            System.out.println(classes.get(i)+"."+methods.get(i)+"."+apis.get(i));
            System.out.println("---------------------------------------------------");
            System.out.println("methodName:"+methods.get(i)+"methodObject"+dict_class_method.get(methods.get(i)));
            SootMethod sootMethod = (SootMethod) dict_methodName_method.get(methods.get(i));
            Body b = sootMethod.retrieveActiveBody();
            Chain<Unit> unitChain = b.getUnits();
            Iterator<Unit> unitIterator = unitChain.iterator();
            while(unitIterator.hasNext()){
                Unit unit = unitIterator.next();
                System.out.println(unit.toString());
            }
            
        }
    }
    
//    public static HashSet<String> readFunctionName(){
//        HashSet<String> hashSet= new HashSet<String>();
//        try{
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.filesToProcess));
//            String line = bufferedReader.readLine();
//            while(line!=null){
//                hashSet.add(line);
//                line = bufferedReader.readLine();
//            }
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//        return hashSet;
//        
//    }
    
    
//    public static void printCG(CallGraph cg, PrintStream ps,String appPackageName) {
//        Iterator<Edge> edgeItr = cg.iterator();
//        List<String> edgeList = new ArrayList<>();
//        HashSet<String> hashSet = new HashSet<String>();
//        HashSet<String> functionName = new HashSet<String>();
//        functionName = readFunctionName();
////        ps.println("Printing functionNames");
////        printHashSet(ps,functionName);
////        ps.println("Printing function definition");
//        while(edgeItr.hasNext()){
//            Edge edge = edgeItr.next();
//
//            SootMethod srcMethod = edge.getSrc().method();
//            
//            //SootMethod is obtained create UnitGraph and iterate over it.
//            //Using In out statement
//            //////////////////////////////////////////////////////////////////
//            String className = srcMethod.getDeclaringClass().getName();
//            String methodName = srcMethod.getName();
//            if((className.contains(appPackageName) || className.equals("dummyMainClass")) && !hashSet.contains(className+methodName) && functionName.contains(className)){
//                hashSet.add(className+methodName);
//                //printHashSet(ps,hashSet);
//                ps.println("--------------------------------------------");
//                ps.println("Analysis of method " + className + "." + methodName + " :");
//                ps.println("--------------------------------------------\n");
//                Body b = srcMethod.retrieveActiveBody();
//                UnitGraph g = new ExceptionalUnitGraph(b);
//
//                GetInOut getio = new GetInOut(g);
//
//                Iterator i = g.iterator();
//                while (i.hasNext())
//                {
//                    Unit u = (Unit) i.next();
////                    List IN = getio.getInOutBefore(u);
////                    List OUT = getio.getInOutAfter(u);
//
////                    ps.print("IN : ");
////                    ps.println(IN.toString());
////                    ps.print("Statement : ");
//                    ps.println(u.toString());
////                    ps.print("Out : ");
////                    ps.println(OUT.toString() + "\n");
//
//                }
//            }
//
//
//            
//            
//            
//            
//            ///////////////////////////////////////////////////////////////////
//
//            
//            ///////////////////////////////////////////////////////////////////
////            String srcMethodDeclaration = srcMethod.getDeclaringClass().toString() + "." +
////                  srcMethod.getName() +
////                  srcMethod.getParameterTypes().toString()
////                    .replace('[', '(').replace(']', ')');
////
////                SootMethod tgtMethod = edge.getTgt().method();
////                String tgtMethodDeclaration = tgtMethod.getDeclaringClass().toString() + "." +
////                  tgtMethod.getName() +
////                  tgtMethod.getParameterTypes().toString()
////                    .replace('[', '(').replace(']', ')');
////
////                edgeList.add(srcMethodDeclaration + " => " + tgtMethodDeclaration);
//            
//            //ps.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
////            System.out.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
//           
//        }
//        //System.out.println(applicationCallGraph.size());
////        for (String edgeStr : edgeList){
////            ps.println(edgeStr);
////        }
//    }
//    public static void  printHashSet(PrintStream ps,HashSet<String> hs){
//        Iterator<String> itr = hs.iterator();
//        while(itr.hasNext()){
//            ps.println(itr.next());
//        }
//
//
//    }

//    public static void printSootClassHashSet(HashSet<SootClass> sootClassHashSet){
//        Iterator<SootClass> sootClassIterator = sootClassHashSet.iterator();
//        while(sootClassIterator.hasNext()){
//            String className = sootClassIterator.next().getName();
//            System.out.println("class:"+className);
//        }
//    }
//    public static void printArrayListString(ArrayList<String> stringArrayList){
//        Iterator<String> stringIterator = stringArrayList.iterator();
//        while(stringIterator.hasNext()){
//            System.out.println(stringIterator.next());
//        }
//    }
    
    public static String trimMethods(String str){
       return str.split("\\(")[0];
    }
    public static String trimApis(String str){
       return str.split("\\(")[0]; 
    }
    
}
