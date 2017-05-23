import beaver.*;
import beaver.Scanner;
import fj.Hash;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Util {
    public static final Logger LOGGER = Logger.getLogger("APK_CG");
    public static final String UNKNOWN = "<unknown>";

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

    public static HashSet<String> readFunctionName(){
        HashSet<String> hashSet= new HashSet<String>();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Config.filesToProcess));
            String line = bufferedReader.readLine();
            while(line!=null){
                hashSet.add(line);
                line = bufferedReader.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return hashSet;
        
    }
    
    
    public static void printCG(CallGraph cg, PrintStream ps,String appPackageName) {
        Iterator<Edge> edgeItr = cg.iterator();
        List<String> edgeList = new ArrayList<>();
        HashSet<String> hashSet = new HashSet<String>();
        HashSet<String> functionName = new HashSet<String>();
        functionName = readFunctionName();
//        ps.println("Printing functionNames");
//        printHashSet(ps,functionName);
//        ps.println("Printing function definition");
        while(edgeItr.hasNext()){
            Edge edge = edgeItr.next();

            SootMethod srcMethod = edge.getSrc().method();
            
            //SootMethod is obtained create UnitGraph and iterate over it.
            //Using In out statement
            //////////////////////////////////////////////////////////////////
            String className = srcMethod.getDeclaringClass().getName();
            String methodName = srcMethod.getName();
            if((className.contains(appPackageName) || className.equals("dummyMainClass")) && !hashSet.contains(className+methodName) && functionName.contains(className)){
                hashSet.add(className+methodName);
                //printHashSet(ps,hashSet);
                ps.println("--------------------------------------------");
                ps.println("Analysis of method " + className + "." + methodName + " :");
                ps.println("--------------------------------------------\n");
                Body b = srcMethod.retrieveActiveBody();
                UnitGraph g = new ExceptionalUnitGraph(b);

                GetInOut getio = new GetInOut(g);

                Iterator i = g.iterator();
                while (i.hasNext())
                {
                    Unit u = (Unit) i.next();
//                    List IN = getio.getInOutBefore(u);
//                    List OUT = getio.getInOutAfter(u);

//                    ps.print("IN : ");
//                    ps.println(IN.toString());
//                    ps.print("Statement : ");
                    ps.println(u.toString());
//                    ps.print("Out : ");
//                    ps.println(OUT.toString() + "\n");

                }
            }


            
            
            
            
            ///////////////////////////////////////////////////////////////////

            
            ///////////////////////////////////////////////////////////////////
//            String srcMethodDeclaration = srcMethod.getDeclaringClass().toString() + "." +
//                  srcMethod.getName() +
//                  srcMethod.getParameterTypes().toString()
//                    .replace('[', '(').replace(']', ')');
//
//                SootMethod tgtMethod = edge.getTgt().method();
//                String tgtMethodDeclaration = tgtMethod.getDeclaringClass().toString() + "." +
//                  tgtMethod.getName() +
//                  tgtMethod.getParameterTypes().toString()
//                    .replace('[', '(').replace(']', ')');
//
//                edgeList.add(srcMethodDeclaration + " => " + tgtMethodDeclaration);
            
            //ps.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
//            System.out.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
           
        }
        //System.out.println(applicationCallGraph.size());
//        for (String edgeStr : edgeList){
//            ps.println(edgeStr);
//        }
    }
    public static void  printHashSet(PrintStream ps,HashSet<String> hs){
        Iterator<String> itr = hs.iterator();
        while(itr.hasNext()){
            ps.println(itr.next());
        }


    }
}
