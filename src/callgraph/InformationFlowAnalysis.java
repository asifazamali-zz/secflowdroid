package callgraph;

import Main.java.MakeRWLabel;
import com.beust.jcommander.Parameter;
import ifc.LabelManager;
import ifc.RWLabel;
import jas.Label;
import polyglot.ast.If;
import polyglot.ast.Return;
import soot.*;
import soot.Body;
import soot.JastAddJ.*;
import soot.baf.Inst;
import soot.dava.toolkits.base.AST.transformations.IfElseSplitter;
import soot.jimple.*;
import soot.jimple.IfStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;
import Main.java.Util;
import soot.util.Chain;

import javax.swing.*;
import java.util.*;
import java.util.List;

import static Main.java.Util.*;

/**
 * Created by asif on 5/27/17.
 */
public class InformationFlowAnalysis 
//  extends ForwardBranchedFlowAnalysis
{
//  protected void copy(Object src, Object dest)
//  {
//    FlowSet sourceSet = (FlowSet) src,
//      destSet = (FlowSet) dest;
//    sourceSet.copy(destSet);
//  }
//
//  protected void merge(Object src1, Object src2, Object dest)
//  {
//    FlowSet srcSet1 = (FlowSet) src1;
//    FlowSet srcSet2 = (FlowSet) src2;
//    FlowSet destSet = (FlowSet) dest;
//  }

//  FlowSet fullSet, emptySet;
//  FlowUniverse allRefLocals;
  public static Map          unitToGenerateSet;
  public static LabelManager labelManager;
  public static MakeRWLabel  makeRWLabel;
  public String       className;
  public String       mthdName;
  public StatementHanding statementHanding;
  public ArrayList<Dictionary> paraLabels;
  public int paraIndex;
  public UnitGraph g;
  public InformationFlowAnalysis(UnitGraph g, LabelManager lblManager, Dictionary subLbl, String className, String mthdName,ArrayList paraLabels,StatementHanding statementHanding)
  {
//    super(g);
    labelManager = Util.labelManager;
    makeRWLabel = Util.makeRWLabel;
    this.className = className;
    this.mthdName = mthdName;
    this.g = g;
    this.paraLabels = paraLabels;
    this.statementHanding = statementHanding;
    paraIndex = -1;
    unitToGenerateSet = new HashMap();

    
  }

  public Dictionary iterateOverGraph(){
    /*
    Iterate over functionGraphs
    and handle each statement
     */
    Iterator itr = g.iterator();
    Dictionary ret = null;
    while(itr.hasNext())
    {
      Unit s = (Unit) itr.next();

      ret = handleStatement(itr, s);
    }
    return ret;
  }
//  protected void flowThrough(Object srcValue, Unit unit,
//                             List fallOut, List branchOuts)
//  {
//    FlowSet dest;
//    FlowSet src = (FlowSet) srcValue;
//    Unit s = (Unit) unit;
//
//    // Create working set.
//    dest = (FlowSet) src.clone();
//    System.out.println(s.toString());
//
//    // Take out kill set.
////    boolean defBoxesFlag = false;
////    Iterator defBoxes = s.getDefBoxes().iterator();
////    while (defBoxes.hasNext()) {
////      ValueBox defBox = (ValueBox) defBoxes.next();
////      Value value = defBox.getValue();
////      System.out.println("def box "+value);
////      defBoxesFlag = true;
//////      if (value instanceof Local &&
//////        value.getType() instanceof RefLikeType)
//////        dest.remove(value);
////    }
////    if(defBoxesFlag) // removing non-assigment statement--- not clean way
////    {
////      Iterator useBoxes = s.getUseBoxes().iterator();
////      while (useBoxes.hasNext())
////      {
////        ValueBox useBox = (ValueBox) useBoxes.next();
////        Value value = useBox.getValue();
////        if (value instanceof InvokeExpr)
////        {
////          InvokeExpr invokeExpr = (InvokeExpr) value;
////          System.out.println("Class: " + invokeExpr.getMethod().getDeclaringClass().getName() + " Method " + invokeExpr.getMethod().getName());
////        } else
////          System.out.println("value box" + value);
////      }
////    }
//    // Perform gen.
//    dest.union((FlowSet) unitToGenerateSet.get(unit), dest);
//
//    // Handle copy statements: 
//    //    x = y && 'y' in src => add 'x' to dest
////    if (s instanceof DefinitionStmt)
////    {
////
////      DefinitionStmt as = (DefinitionStmt) s;
////      Value lo = as.getLeftOp();
////      Value ro = as.getRightOp();
//////      System.out.println("Definition Stmt "+as);
//////      System.out.println("Value "+ro);
////      // extract cast argument
////      if (s instanceof IdentityStmt)
////      {
////        System.out.println("Identity Statement");
////        statementHanding.handleIdentityStmt(s);
////      }
////      if (s instanceof AssignStmt)
////      {
////        System.out.println("Assignment Statment");
////        statementHanding.handleAssignmentStmt(s);
////      }
////      if(s instanceof IfStmt){
////        Chain<Unit> s =  ((UnitGraph) g)
////      }
//
//      if (ro instanceof CastExpr)
//        ro = ((CastExpr) ro).getOp();
//
//      if (src.contains(ro) &&
//        as.getLeftOp() instanceof Local)
//        dest.add(as.getLeftOp());
//    }
////    else if(s instanceof IfStmt){
////      statementHanding.handleIfStmt(s);
////    }
//
//    // Copy the out value to the fallthrough box (don't need iterator)
//   
//
//    // Handle if statements by patching dest sets.
//    if (unit instanceof IfStmt)
//    {
//      Value cond = ((IfStmt) unit).getCondition();
//      Value op1 = ((BinopExpr) cond).getOp1();
//      Value op2 = ((BinopExpr) cond).getOp2();
//      boolean isNeg = cond instanceof NeExpr;
//      Value toGen = null;
//
//      // case 1: opN is a local and opM is NullConstant
//      //          => opN nonnull on ne branch.
//      if (op1 instanceof Local && op2 instanceof NullConstant)
//        toGen = op1;
//
//      if (op2 instanceof Local && op1 instanceof NullConstant)
//        toGen = op2;
//
//      if (toGen != null)
//      {
//        Iterator it = null;
//
//        // if (toGen != null) goto l1: on branch, toGen nonnull.
//        if (isNeg)
//          it = branchOuts.iterator();
//        else
//          it = fallOut.iterator();
//
//        while (it.hasNext())
//        {
//          FlowSet fs = (FlowSet) (it.next());
//          fs.add(toGen);
//        }
//      }
//
//      // case 2: both ops are local and one op is non-null and testing equality
//      if (op1 instanceof Local && op2 instanceof Local &&
//        cond instanceof EqExpr)
//      {
//        toGen = null;
//
//        if (src.contains(op1))
//          toGen = op2;
//        if (src.contains(op2))
//          toGen = op1;
//
//        if (toGen != null)
//        {
//          Iterator branchIt = branchOuts.iterator();
//          while (branchIt.hasNext())
//          {
//            FlowSet fs = (FlowSet) (branchIt.next());
//            fs.add(toGen);
//          }
//        }
//      }
//    }
//    {
//      Iterator it = fallOut.iterator();
////      System.out.println("FallsOut");
//      while (it.hasNext())
//      {
//        Object object = it.next();
//        FlowSet fs = (FlowSet) (object);
////        System.out.println(dest);
////        System.out.println(object.toString());
//        copy(dest,fs);
//      }
////      System.out.println("");
//    }
//
//    // Copy the out value to all branch boxes.
//    {
//      Iterator it = branchOuts.iterator();
////      System.out.println("branch boxes");
//      while (it.hasNext())
//      {
//
//        Object object = it.next();
//        FlowSet fs = (FlowSet) (object);
////        System.out.println(dest);
////        System.out.println(object.toString());
//        copy(dest, fs);
//      }
//
////      System.out.println("");
//    }
//  }

//  protected Object newInitialFlow()
//  {
//    return fullSet.clone();
//  }
//
//  protected Object entryInitialFlow()
//  {
//    // everything could be null
//    return emptySet.clone();
//  }
//
//  private void addGen(Unit u, Value v)
//  {
//    ArraySparseSet l = (ArraySparseSet) unitToGenerateSet.get(u);
//    l.add(v);
//  }
//
//  private void addGensFor(DefinitionStmt u)
//  {
//    Value lo = u.getLeftOp();
//    Value ro = u.getRightOp();
//
//    if (ro instanceof NewExpr ||
//      ro instanceof NewArrayExpr ||
//      ro instanceof NewMultiArrayExpr ||
//      ro instanceof ThisRef ||
//      ro instanceof CaughtExceptionRef)
//      addGen(u, lo);
//  }
//
//  public static ArraySparseSet getInOutBefore(Unit u)
//  {
//    return (ArraySparseSet) unitToGenerateSet.get(u);
//  }
//
//  public static ArraySparseSet getInOutAfter(Unit u)
//  {
//    return (ArraySparseSet) unitToGenerateSet.get(u);
//  }




  public Dictionary handleStatement(Iterator itr, Unit s){
    ps.println("stmt "+s);
    System.out.println("method "+mthdName+ " stmt "+s);
    Dictionary ret = null;
    
//    System.out.println("handle statement called");
    
    if (s instanceof DefinitionStmt)
    {
      DefinitionStmt as = (DefinitionStmt) s;
      Value lo = as.getLeftOp();
//            System.out.println("Definition Stmt "+as);
      //      System.out.println("Value "+ro);
      // extract cast argument
      if (s instanceof IdentityStmt)
      {
        System.out.println("Identity Statement");
        ret = statementHanding.handleIdentityStmt(this,s);
        System.out.println("returning Identity Statement"+ret);
      }
      if (s instanceof AssignStmt)
      {
        System.out.println("Assignment Statment");
        ret = statementHanding.handleAssignmentStmt(s);
        System.out.println("returning Assignment statement" +ret);
      }
      ps.println(ret);

    }
    else if (s instanceof IfStmt)
    {
//      System.out.println("If statement ");
//      System.out.println(s);
      ValueBox valueBox = ((IfStmt) s).getConditionBox();
//      System.out.println("getCondition " + ((IfStmt) s).getCondition());
//
//      System.out.println("getCondition " + valueBox.getValue());
      if(((IfStmt) s).getTarget() instanceof ReturnVoidStmt)
       ret =  statementHanding.handleIfStatement(this,itr,s);
      else
        ret = statementHanding.handleIfElseStatement(this, itr,s);
      System.out.println("returning If statement "+ret);
    }
    else if (s instanceof ReturnVoidStmt){
      System.out.println("returnvoid"+s);
      ret = statementHanding.handleReturnVoid(s);
      System.out.println("returning ReturnVoid"+ret);
    }
    else if(s instanceof Return){
      System.out.println("return value");
      ret = statementHanding.handleReturn(s);
      System.out.println("returning Return "+ret);
    }
    else if(s instanceof InvokeStmt){
      ret = statementHanding.handleOnlyInvokeStmt(((InvokeStmt) s).getInvokeExpr());
//      System.out.println("invokeStmt"+s);
//      InvokeExpr invokeExpr = ((InvokeStmt) s).getInvokeExpr();
      System.out.println("returning invokeStmt "+ret);
    }
    else if(s instanceof InterfaceInvokeExpr){
      System.out.println("invokeExper"+s);
      ret = null;
    }
    else if (s instanceof ReturnStmt){
      System.out.println("ReturnStmt");
      
      ret = statementHanding.handleReturnStmt(s);
      System.out.println("return ReturnStmt"+ret);
    }
    else
    {
      System.out.println("handleStatement+  else");
      System.out.println(s);
      ret = null;
    }
    return ret;
  }

}
