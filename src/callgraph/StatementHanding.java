package callgraph;

import Main.java.MakeRWLabel;
import Main.java.Util;
import ifc.LabelManager;
import ifc.RWLabel;
import polyglot.ast.If;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.internal.JNeExpr;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import static callgraph.InformationFlowAnalysis.checkAndDef;
import static callgraph.InformationFlowAnalysis.labelManager;
import static callgraph.InformationFlowAnalysis.subLabel;

/**
 * Created by asif on 5/31/17.
 */
public class StatementHanding
{
  
  public String className,mthdName;
  public StatementHanding(String className,String mthdName){
    this.className = className;
    this.mthdName = mthdName;
  }

  public boolean handleLocalStatement(Value lo, Value ro)
  {
    checkAndDef(lo.toString(), className, mthdName);
    checkAndDef(ro.toString(), className, mthdName);
    // lo <- ro checking and changing labels of subjects

    subLabel = new RWLabel().checkRead(subLabel, labelManager.getLabel(ro.toString(), className, mthdName), labelManager);
    // update label of lo 
    labelManager.updateLabel(lo.toString(), labelManager.getLabel(ro.toString(), className, mthdName), className, mthdName);
    return true;
  }

  public boolean handleInterfaceInvokeExpr(Value lo, Value ro)
  {
    System.out.println("handling Invoke Expr");
    return true;
  }

  public boolean handleInvokeExpr(Value lo, Value ro)
  {
    checkAndDef(lo.toString(), className, mthdName);
    checkAndDef(ro.toString(), className, mthdName);
    Dictionary ret = null;
    InvokeExpr invokeExpr = (InvokeExpr) ro;
    SootMethod sootMethod = invokeExpr.getMethod();
    List<Value> args = invokeExpr.getArgs();
    String method = sootMethod.getName();

    String cName = sootMethod.getDeclaringClass().getName();
    System.out.println("Class :" + cName + " method :" + method);
    System.out.println("arguments");
    if (cName.contains(Util.appPackageName))
    {
      System.out.println("Parse Method: " + method);
      ProcessLocalMethod processLocalMethod = new ProcessLocalMethod();
      ret = processLocalMethod.processLocalMethod(sootMethod, cName);
      System.out.println("Returning from processLocalMethod");
      System.out.println("----------------------------------------------");
      System.out.println(LabelManager.d);
      System.out.println("----------------------------------------------");
      System.out.println(Util.global_output_api_method);
//            labelManager.updateLabel(lo.toString(),labelManager.getLabel(ro.toString()));
    } else if (Util.global_output_api_method.containsKey(cName) && Util.global_output_api_method.get(cName).contains(method))
    {
//            System.out.println("Inside global_output_apis_method");
      Dictionary publicLabel = new MakeRWLabel().createPublicLabel();
      new RWLabel().checkRead(subLabel, publicLabel, labelManager);
    }
    for (Value v : args)
    {
      System.out.print(v);
      if (labelManager.getLabel(v.toString(), className, mthdName) != null)
      {
        labelManager.updateLabel(lo.toString(), labelManager.getLabel(v.toString(), className, mthdName), className, mthdName);
      }
    }
    if (ret != null)
      labelManager.updateLabel(lo.toString(), ret, className, mthdName);
    return true;
  }

  public boolean handleIdentityStmt(Unit s)
  {
    Value lo = ((DefinitionStmt) s).getLeftOp();
    Value ro = ((DefinitionStmt) s).getRightOp();
    if(lo instanceof Local)
    {
      if (ro.toString().contains("@this")){
        checkAndDef(lo.toString(),className,mthdName);
      }

      else if (ro.toString().contains("@parameter")){
        checkAndDef(lo.toString(),className,mthdName);
//        System.out.println("handle it later");
//        System.out.println("Identity parameter "+((Parameter) ro).description());
      }
      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    return true;
  }

  public boolean handleAssignmentStmt(Unit s)
  {
    Value lo = ((DefinitionStmt) s).getLeftOp();
    Value ro = ((DefinitionStmt) s).getRightOp();
    if (lo instanceof Local)
    {
      if (ro instanceof Local)
      {
        System.out.println("Check for subject label and assign sublabel to lo");

        handleLocalStatement(lo,ro);
      } else if (ro instanceof InterfaceInvokeExpr)
      {
        handleInterfaceInvokeExpr(lo,ro);
      } else if (ro instanceof InvokeExpr)
      {
        handleInvokeExpr(lo,ro);
      }

      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }

    return true;
  }
  /*
  if cond then{ l1 = r1
                l2 = r2
                }
  else{
    l3 = r3
    l4 = r4.fnc
    }
    
  cond : r.functioncall or a binop b // can't be the expression because jimple is 3 address language
  
  
  
  
  
   */
  public Dictionary handleIfStatement(Iterator itr, Unit s){
    Value v = ((IfStmt) s).getCondition();
    Dictionary ret = null;
    if(v instanceof JInstanceOfExpr){
      JInstanceOfExpr expr = (JInstanceOfExpr) v;
      Value val = handleInstanceExper(expr);
      new RWLabel().checkRead(subLabel,labelManager.getLabel(val.toString(),className,mthdName),labelManager);
    }
    else if(v instanceof JEqExpr || v instanceof JNeExpr){
      System.out.println(v);
      AbstractBinopExpr abstractBinopExpr = (AbstractBinopExpr) v;
      handleEqualityOrNonEqualityCheck(abstractBinopExpr);
    }
    while(itr.hasNext()){
//      System.out.println("Inside if");
      Unit unit  = (Unit) itr.next();
      System.out.println(unit);
      if(unit instanceof  ReturnVoidStmt){
//        System.out.println("Return reached "+unit);
        return ret;
      }
      else{
        InformationFlowAnalysis.handleStatement(itr,unit);
        // lub with left hand operator
      }
    }
    
    
    return ret;
  }
  public Dictionary handleIfElseStatement(Iterator itr,Unit s){
    Dictionary ret = null;
//    System.out.println("Inside If/else");
    ret = handleIfStatement(itr,s);
    while(itr.hasNext()){
//      System.out.println("Inside else");
      Unit unit = (Unit) itr.next();
      System.out.println(unit);
      if(unit instanceof ReturnVoidStmt){
//        System.out.println("Else Return reached "+unit);
        return ret;
      }
      else{
        InformationFlowAnalysis.handleStatement(itr,unit);
        //lub with hand operator
      }
    }
    return ret;
  }

//  public boolean handleIfStmt(Unit s){
//    System.out.println("If stmt");
//    Value v = ((IfStmt) s).getCondition();
//    System.out.println(v);
//    ValueBox vb = ((IfStmt) s).getConditionBox();
//    Stmt stmt = ((IfStmt) s).getTarget();
//    UnitBox unitBox = ((IfStmt) s).getTargetBox();
//    System.out.println(vb.getValue());
//    System.out.println(stmt.toString());
//    System.out.println(unitBox.toString());
//    return true;
//  }
  
  public Value handleInstanceExper(JInstanceOfExpr expr){
    Value op = expr.getOp();
    
//  return label of op
    return op;
  }
  
  public boolean handleEqualityOrNonEqualityCheck(AbstractBinopExpr abstractBinopExpr){
    Value lo = abstractBinopExpr.getOp1();
    Value ro = abstractBinopExpr.getOp2();
    checkAndDef(lo.toString(),className,mthdName);
//    System.out.println(lo);
//    System.out.println(labelManager.getLabel(lo.toString(),className,mthdName));
//    System.out.println(ro.toString());
    // return LUB lo and ro
    new RWLabel().checkRead(subLabel,labelManager.getLabel(lo.toString(),className,mthdName),labelManager);
    if( ! (ro instanceof Constant || ro == NullConstant.v()))
       new RWLabel().checkRead(subLabel,labelManager.getLabel(ro.toString(),className,mthdName),labelManager);
    return false;
  }
}
