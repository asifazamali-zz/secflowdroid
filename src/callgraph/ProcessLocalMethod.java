package callgraph;

import Main.java.Util;
import fj.Hash;
import heros.flowfunc.Identity;
import ifc.LabelManager;
import ifc.RWLabel;
import org.jf.dexlib2.dexbacked.raw.ItemType;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MemoryEfficientGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.*;

import static Main.java.Util.*;

/**
 * Created by asif on 5/28/17.
 */
public class ProcessLocalMethod
{
  public SootMethod sootMethod;
  String  className;
  HashSet refLocals;
  ArrayList paraLabels;
  Body b;
  UnitGraph g;
  public ProcessLocalMethod( String className,SootMethod sootMethod, ArrayList paraLabels){
    this.sootMethod = sootMethod;
    this.className = className;
    this.paraLabels = paraLabels;
    ps.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    ps.println(className+"."+sootMethod.getName().toString());
    ps.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    System.out.println(className+"."+sootMethod.getName().toString());
    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

    b = sootMethod.retrieveActiveBody();
    g = new ExceptionalUnitGraph(b);
  }
  public Dictionary processLocalMethod()
  {
    Chain units = b.getUnits();
    String methodName = sootMethod.getName();
    Iterator itr = units.iterator();
    Dictionary ret = null;
    refLocals = new HashSet<>();
    Iterator localIt = b.getLocals().iterator();
    while (localIt.hasNext())
    {
      Local l = (Local) localIt.next();
      System.out.println(l.getName());
      refLocals.add(l.toString());
    }
    StatementHanding statementHanding = new StatementHanding(className,methodName,refLocals);
    InformationFlowAnalysis informationFlowAnalysis = new InformationFlowAnalysis(g,labelManager,subLabel,className,methodName,paraLabels,statementHanding);
    ret = informationFlowAnalysis.trials();
    System.out.println("returning from processingLocalMethod "+methodName+" "+ret);
    return ret;
  }
  
}
