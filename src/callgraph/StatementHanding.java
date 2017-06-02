package callgraph;

import Main.java.MakeRWLabel;
import Main.java.Util;
import fj.data.State;
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

import static Main.java.Util.*;
import static callgraph.InformationFlowAnalysis.*;
import static callgraph.InformationFlowAnalysis.labelManager;
import static callgraph.InformationFlowAnalysis.makeRWLabel;
import static callgraph.InformationFlowAnalysis.subLabel;
import static callgraph.ProcessLocalMethod.createObjId;

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
    String obj_id = createObjId(lo.toString(),className,mthdName);
    labelManager.updateLabel(obj_id, labelManager.getLabel(ro.toString(), className, mthdName));
    return true;
  }
  public boolean handleOnlyInvokeStmt(InvokeExpr invokeExpr){
    if(invokeExpr instanceof InterfaceInvokeExpr){
      Value base = ((InterfaceInvokeExpr) invokeExpr).getBase();
//      System.out.println("interface base"+ base);
      List args = invokeExpr.getArgs();
      Iterator iterator = args.iterator();
      while(iterator.hasNext()){
        Value arg = (Value) iterator.next();
//        System.out.println("arg: "+arg);
        if(refLocals.contains(arg.toString())){
          checkAndDef(arg.toString(),className,mthdName);
        }
        printLabel(arg.toString(),className,mthdName);
      }
//      String obj_id = createObjId(base.toString(),className,mthdName);

      printLabel(base.toString(),className,mthdName);
    }
    return false;
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
    System.out.println("arguments");
    for (Value v : args)
    {
      System.out.print(v);
      if (labelManager.getLabel(v.toString(), className, mthdName) != null)
      {
        String obj_id = createObjId(lo.toString(),className,mthdName);
        labelManager.updateLabel(obj_id, labelManager.getLabel(v.toString(), className, mthdName));
      }
    }
    if (ret != null)
    {
      String obj_id = createObjId(lo.toString(),className,mthdName);
      labelManager.updateLabel(obj_id, ret);
    }
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
      handleRightOperand(lo,ro);
      
      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    else if(lo instanceof ArrayRef){
//      System.out.println("Array Ref Base "+((ArrayRef) lo).getBase());
      Value leftOp = ((ArrayRef) lo).getBase();
      handleRightOperand(leftOp,ro);
      System.out.println(leftOp.toString() + " " + labelManager.getLabel(leftOp.toString(), className, mthdName));
    }

    return true;
  }
  public boolean handleRightOperand(Value lo, Value ro)
  {
    if (ro instanceof Local)
    {
      System.out.println("Check for subject label and assign sublabel to lo");

      handleLocalStatement(lo, ro);
    } else if (ro instanceof InterfaceInvokeExpr)
    {
      System.out.println("Assignment + Interfaceinvoke");
      handleInterfaceInvokeExpr(lo, ro);
    } else if (ro instanceof InvokeExpr)
    {
      System.out.println("Assignment + InvokeExper");
      handleInvokeExpr(lo, ro);
//        System.out.println("handleInvokeExpr returned");
    }
    else if(ro instanceof ArrayRef){
      Value rightOpt = ((ArrayRef) ro).getBase();
      handleRightOperand(lo,rightOpt);
    }
    else
    {
      System.out.println("Assignment + else");
      Type type = ro.getType();
      System.out.println(type);
      String objId = createObjId(lo.toString(), className, mthdName);

      if (sensitive_class.contains(type.toString()))
      {
        Dictionary privateLabel = makeRWLabel.createPrivateLabel();
        if (!(labelManager.updateLabel(objId, privateLabel)))
        {
          String obj_id = createObjId(lo.toString(), className, mthdName);
          labelManager.saveLabel(obj_id, privateLabel);
        }
      }
      else
      {
        Dictionary publicLabel = makeRWLabel.createPublicLabel();
        if (!(labelManager.updateLabel(objId, subLabel)))
        {
//            String obj_id = createObjId(lo.toString(), className, mthdName);
          new RWLabel().createObjLabel(subLabel, lo.toString(), labelManager, className, mthdName);
        }
      }
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
      System.out.println("Inside if");
      Unit unit  = (Unit) itr.next();
//      ps.println(unit);
//      System.out.println(unit);
      if(unit instanceof  ReturnVoidStmt){
        System.out.println("If Return reached "+unit);
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
//      System.out.println(unit);
      if(unit instanceof ReturnVoidStmt){
        System.out.println("Else Return reached "+unit);
        return ret;
      }
      else{
        InformationFlowAnalysis.handleStatement(itr,unit);
        //lub with hand operator
      }
    }
    return ret;
  }

  public void handleReturnVoid(Unit u){
    return;
  }
  public void handleReturn(Unit u){
    return;
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
