package callgraph;

import Main.java.MakeRWLabel;
import ifc.LabelManager;
import ifc.RWLabel;
import soot.*;
import soot.baf.Inst;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;

import javax.swing.*;
import java.util.*;

/**
 * Created by asif on 5/27/17.
 */
public class InformationFlowAnalysis extends ForwardBranchedFlowAnalysis
{
  protected void copy(Object src,Object dest){
    FlowSet sourceSet = (FlowSet) src,
      destSet = (FlowSet) dest;
    sourceSet.copy(destSet);
  }
  
  protected void merge(Object src1,Object src2,Object dest){
    FlowSet srcSet1 = (FlowSet) src1;
    FlowSet srcSet2 = (FlowSet) src2;
    FlowSet destSet = (FlowSet) dest;
  }
  FlowSet fullSet, emptySet;
  FlowUniverse allRefLocals;
  public static Map unitToGenerateSet;
  public LabelManager labelManager;
  public MakeRWLabel makeRWLabel;
  public Dictionary subLabel;
  public String className;
  public InformationFlowAnalysis(UnitGraph g, LabelManager lblManager,Dictionary subLbl,String className){
    super(g);
    labelManager = lblManager;
    subLabel = subLbl;
    this.className = className;
    makeRWLabel = new MakeRWLabel();
    unitToGenerateSet  = new HashMap();
    Body b = g.getBody();
    List refLocals = new LinkedList();
    makeRWLabel = new MakeRWLabel();
    emptySet = new ArraySparseSet();
    fullSet = new ArraySparseSet();
    Iterator localIt = b.getLocals().iterator();
    System.out.println("locals");
    while(localIt.hasNext()){
      Local l = (Local) localIt.next();
      System.out.println(l.getName());
      String obj_id = l.getName()+this.className;
      if((labelManager.getLabel(obj_id)) != null){
        Dictionary objLabel = makeRWLabel.makeRWLabel(subLabel,obj_id,true,labelManager);
        labelManager.saveLabel(obj_id,(Dictionary) objLabel.get("label"));
      }
        
//      if(l.getType() instanceof RefLikeType) //RefLikeType --> null in java
      fullSet.add(l);
    }
    
//    Iterator unitIt = b.getUnits().iterator();
//    while(unitIt.hasNext()){
//      Unit u = (Unit) unitIt.next();
//      System.out.println(u.toString());
//      unitToGenerateSet.put(u,new ArraySparseSet());
//      if(u instanceof DefinitionStmt){
//        Value lo = ((DefinitionStmt) u).getLeftOp();
//        if(lo instanceof Local && lo.getType() instanceof RefLikeType)
//          addGensFor((DefinitionStmt)u);
//      }
//      Iterator boxIt = u.getUseAndDefBoxes().iterator();
//      while(boxIt.hasNext()){
//        Value boxValue = ((ValueBox) boxIt.next()).getValue();
//        Value base = null;
//
//        if(boxValue instanceof InstanceFieldRef) {
//          base = ((InstanceFieldRef) (boxValue)).getBase();
//        } else if (boxValue instanceof ArrayRef) {
//          base = ((ArrayRef) (boxValue)).getBase();
//        } else if (boxValue instanceof InstanceInvokeExpr) {
//          base = ((InstanceInvokeExpr) boxValue).getBase();
//        } else if (boxValue instanceof LengthExpr) {
//          base = ((LengthExpr) boxValue).getOp();
//        } else if (u instanceof ThrowStmt) {
//          base = ((ThrowStmt)u).getOp();
//        } else if (u instanceof MonitorStmt) {
//          base = ((MonitorStmt)u).getOp();
//        }
//
//        if (base != null &&
//          base instanceof Local &&
//          base.getType() instanceof RefLikeType)
//          addGen(u, base);
//      }
//    }

    // Call superclass method to do work.
    doAnalysis();
  }
  
 
  
  protected void flowThrough(Object srcValue, Unit unit,
                             List fallOut, List branchOuts)
  {
    FlowSet dest;
    FlowSet src  = (FlowSet) srcValue;
    Unit    s    = (Unit)    unit;

    // Create working set.
    dest = (FlowSet)src.clone();
    System.out.println(s.toString());

    // Take out kill set.
//    boolean defBoxesFlag = false;
//    Iterator defBoxes = s.getDefBoxes().iterator();
//    while (defBoxes.hasNext()) {
//      ValueBox defBox = (ValueBox) defBoxes.next();
//      Value value = defBox.getValue();
//      System.out.println("def box "+value);
//      defBoxesFlag = true;
////      if (value instanceof Local &&
////        value.getType() instanceof RefLikeType)
////        dest.remove(value);
//    }
//    if(defBoxesFlag) // removing non-assigment statement--- not clean way
//    {
//      Iterator useBoxes = s.getUseBoxes().iterator();
//      while (useBoxes.hasNext())
//      {
//        ValueBox useBox = (ValueBox) useBoxes.next();
//        Value value = useBox.getValue();
//        if (value instanceof InvokeExpr)
//        {
//          InvokeExpr invokeExpr = (InvokeExpr) value;
//          System.out.println("Class: " + invokeExpr.getMethod().getDeclaringClass().getName() + " Method " + invokeExpr.getMethod().getName());
//        } else
//          System.out.println("value box" + value);
//      }
//    }
    // Perform gen.
    dest.union((FlowSet)unitToGenerateSet.get(unit), dest);

    // Handle copy statements: 
    //    x = y && 'y' in src => add 'x' to dest
    if (s instanceof DefinitionStmt)
    {
      
      DefinitionStmt as = (DefinitionStmt) s;
      Value lo = as.getLeftOp();
      Value ro = as.getRightOp();
//      System.out.println("Definition Stmt "+as);
//      System.out.println("Value "+ro);
      // extract cast argument
      if(lo instanceof Local){
        if(ro instanceof Local){
          checkAndDef(lo.toString());
          checkAndDef(ro.toString());
          // lo <- ro checking and changing labels of subjects
          
          subLabel = new RWLabel().checkRead(subLabel,labelManager.getLabel(ro.toString()),labelManager);
          // update label of lo 
          labelManager.updateLabel(lo.toString(),labelManager.getLabel(ro.toString()));
        }
        else if(ro instanceof InvokeExpr){
          InvokeExpr invokeExpr = (InvokeExpr) ro;
          SootMethod sootMethod = invokeExpr.getMethod();
          List<Value> args = invokeExpr.getArgs();
          String method = sootMethod.getName();
          
          String className = sootMethod.getDeclaringClass().getName();
          System.out.println("Class :"+className+" method :"+method);
          System.out.println("arguments");
          for(Value v: args){
            System.out.print(v);
          }
          System.out.println("");
        }
      }
      if (ro instanceof CastExpr)
        ro = ((CastExpr) ro).getOp();

      if (src.contains(ro) &&
        as.getLeftOp() instanceof Local)
        dest.add(as.getLeftOp());
    }

    // Copy the out value to the fallthrough box (don't need iterator)
    {
      Iterator it = fallOut.iterator();
      while (it.hasNext()) {
        FlowSet fs = (FlowSet) (it.next());
        copy(dest, fs);
      }
    }

    // Copy the out value to all branch boxes.
    {
      Iterator it = branchOuts.iterator();
      while (it.hasNext()) {
        FlowSet fs = (FlowSet) (it.next());
        copy(dest, fs);
      }
    }

    // Handle if statements by patching dest sets.
    if (unit instanceof IfStmt)
    {
      Value cond = ((IfStmt)unit).getCondition();
      Value op1 = ((BinopExpr) cond).getOp1();
      Value op2 = ((BinopExpr) cond).getOp2();
      boolean isNeg = cond instanceof NeExpr;
      Value toGen = null;

      // case 1: opN is a local and opM is NullConstant
      //          => opN nonnull on ne branch.
      if (op1 instanceof Local && op2 instanceof NullConstant)
        toGen = op1;

      if (op2 instanceof Local && op1 instanceof NullConstant)
        toGen = op2;

      if (toGen != null)
      {
        Iterator it = null;

        // if (toGen != null) goto l1: on branch, toGen nonnull.
        if (isNeg)
          it = branchOuts.iterator();
        else
          it = fallOut.iterator();

        while(it.hasNext()) {
          FlowSet fs = (FlowSet) (it.next());
          fs.add(toGen);
        }
      }

      // case 2: both ops are local and one op is non-null and testing equality
      if (op1 instanceof Local && op2 instanceof Local &&
        cond instanceof EqExpr)
      {
        toGen = null;

        if (src.contains(op1))
          toGen = op2;
        if (src.contains(op2))
          toGen = op1;

        if (toGen != null)
        {
          Iterator branchIt = branchOuts.iterator();
          while (branchIt.hasNext()) {
            FlowSet fs = (FlowSet) (branchIt.next());
            fs.add(toGen);
          }
        }
      }
    }
  }

  protected Object newInitialFlow()
  {
    return fullSet.clone();
  }

  protected Object entryInitialFlow()
  {
    // everything could be null
    return emptySet.clone();
  }

  private void addGen(Unit u, Value v)
  {
    ArraySparseSet l = (ArraySparseSet)unitToGenerateSet.get(u);
    l.add(v);
  }

  private void addGensFor(DefinitionStmt u)
  {
    Value lo = u.getLeftOp();
    Value ro = u.getRightOp();

    if (ro instanceof NewExpr ||
      ro instanceof NewArrayExpr ||
      ro instanceof NewMultiArrayExpr ||
      ro instanceof ThisRef ||
      ro instanceof CaughtExceptionRef)
      addGen(u, lo);
  }
  public static ArraySparseSet getInOutBefore(Unit u){
    return  (ArraySparseSet) unitToGenerateSet.get(u);
  }
  public static ArraySparseSet getInOutAfter(Unit u){
    return (ArraySparseSet) unitToGenerateSet.get(u);
  }
  public boolean checkAndDef(String obj_id){
    Dictionary lolabel = labelManager.getLabel(obj_id);
    if(lolabel == null){
      Dictionary objLabel = makeRWLabel.makeRWLabel(subLabel,obj_id,true,labelManager);
      labelManager.saveLabel(obj_id,objLabel);
      return false;
    }
    return true;
  }
}
