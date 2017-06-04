package callgraph;

import Main.java.MakeRWLabel;
import Main.java.Util;
import fj.data.State;
import ifc.LabelManager;
import ifc.RWLabel;
import polyglot.ast.Field;
import polyglot.ast.If;
import polyglot.ast.Special;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static Main.java.Util.*;
import static callgraph.InformationFlowAnalysis.*;
import static callgraph.InformationFlowAnalysis.labelManager;
import static callgraph.InformationFlowAnalysis.makeRWLabel;

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

  public Dictionary handleLocalStatement(Value ro)
  {
//    checkAndDef(lo.toString(), className, mthdName);
    checkAndDef(ro.toString(), className, mthdName);
    // lo <- ro checking and changing labels of subjects

//    subLabel = new RWLabel().checkRead(subLabel, labelManager.getLabel(ro.toString(), className, mthdName), labelManager);
//    // update label of lo 
//    String obj_id = createObjId(lo.toString(),className,mthdName);
//    labelManager.updateLabel(obj_id, labelManager.getLabel(ro.toString(), className, mthdName));
    return labelManager.getLabel(ro.toString(),className,mthdName);
  }
  public Dictionary handleOnlyInvokeStmt(InvokeExpr invokeExpr){
    Value base = null;
    Dictionary ret = null;
    if (invokeExpr instanceof InterfaceInvokeExpr)
    {
      System.out.println("Interfaceinvoke");
      base = ((InterfaceInvokeExpr) invokeExpr).getBase();
    }
    else if(invokeExpr instanceof SpecialInvokeExpr){
      System.out.println("special invoke");
      base = ((SpecialInvokeExpr) invokeExpr).getBase();
    }
    else if(invokeExpr instanceof VirtualInvokeExpr){
      System.out.println("virtual invoke");
      base = ((VirtualInvokeExpr) invokeExpr).getBase();
    }
    else if(invokeExpr instanceof StaticInvokeExpr){
      System.out.println("static invoke");
      base = null;
    }
    if(base != null && refLocals.contains(base)){
      checkAndDef(base .toString(),className,mthdName);
      printLabel(base.toString(),className,mthdName);
    }
   
//      String obj_id = createObjId(base.toString(),className,mthdName);
    
//    if(ro instanceof VirtualInvokeExpr){
//
////      VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) ro;
//      rightOp = ((VirtualInvokeExpr) ro).getBase();
//
//    }
    SootMethod sootMethod = invokeExpr.getMethod();
    
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
    }
    System.out.println("arguments");
    List<Value> args = invokeExpr.getArgs();
    Dictionary publicLabel1 = null;
    Dictionary publicLabel2  = createPublicLabel("dummyPublicLabel2");;
    if(invokeExpr.getArgCount() > 0)
    {
      publicLabel1 = createPublicLabel("dummyPublicLabel1");


    }
    for (Value v : args)
    {
      System.out.println(v.toString());
      if(refLocals.contains(v.toString())) {
        if(labelManager.getLabel(v.toString(),className,mthdName) == null)
          new RWLabel().createObjLabel(publicLabel1,v.toString(),labelManager,className,mthdName);
        else
          System.out.println(labelManager.getLabel(v.toString(),className,mthdName));
        publicLabel2 = new RWLabel().checkRead(publicLabel2,labelManager.getLabel(v.toString(),className,mthdName),labelManager);
      }

    }
    if(base!=null)
    {
      checkAndDef(base.toString(),className,mthdName);
      publicLabel2 = new RWLabel().checkRead(publicLabel2,labelManager.getLabel(base.toString(),className,mthdName),labelManager);
      String obj_id = createObjId(base.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,publicLabel2);
    }
//    Make all the args share the LUB{base,arg1,arg2,.....}
    for(Value v:args){
      if(refLocals.contains(v.toString()))
      {
        String obj_id = createObjId(v.toString(),className,mthdName);
        labelManager.updateLabel(obj_id,publicLabel2);
      }
    }
    if (Util.global_output_api_method.containsKey(cName) && Util.global_output_api_method.get(cName).contains(method))
    {
      System.out.println("Inside global_output_apis_method");
      Dictionary publicLabel = createPublicLabel("dummyLabel."+className+"."+mthdName);
      System.out.println("check write with "+publicLabel2);
      new RWLabel().checkWrite(publicLabel2, publicLabel);
    }
    else if(sensitive_api_method.containsKey(cName) && sensitive_api_method.get(cName).contains(method)){
      System.out.println("-----------------------------------------------");
      System.out.println("Sensitive api :"+cName+" "+method);
      
      System.out.println("-----------------------------------------------");

      Dictionary privateLabel =  Util.makeRWLabel.createPrivateLabel();
      subLabel = new RWLabel().checkRead(subLabel,privateLabel,labelManager);
      System.out.println("changed subLabel "+subLabel);
      if(base != null && refLocals.contains(base.toString()))
      {
        checkAndUpdate(base.toString(),className,mthdName);
        System.out.println("updated obj_label "+base.toString()+" "+labelManager.getLabel(base.toString(),className,mthdName));
      }
    }
    ret = publicLabel2;
    
    System.out.println("returning from assignment "+ret);
    return ret;
  }


  public Dictionary handleInvokeExpr(Value ro)
  {
   
//    System.out.println("handleInvokeExpr"+ro);
    InvokeExpr invokeExpr = (InvokeExpr) ro;
    Dictionary ret = handleOnlyInvokeStmt(invokeExpr);
    System.out.println("returned form invokeOnly "+ret);
    if(ret != null)
    {
      
     return ret;
    }
    else{
      ret = createPublicLabel("dummyLabel."+className+mthdName);
    }
    return ret;
  }

  public boolean handleIdentityStmt(Unit s)
  {
    Value lo = ((DefinitionStmt) s).getLeftOp();
    Value ro = ((DefinitionStmt) s).getRightOp();
    if(lo instanceof Local)
    {
      if (ro.toString().contains("@this")){
        Dictionary publicLabel = createPublicLabel("dummyLabel");
        new RWLabel().createObjLabel(publicLabel,lo.toString(),labelManager,className,mthdName);
      }

      else if (ro.toString().contains("@parameter")){

        Dictionary publicLabel = createPublicLabel("dummyLabel");
        new RWLabel().createObjLabel(publicLabel,lo.toString(),labelManager,className,mthdName);

      }
      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    return true;
  }

  public Dictionary handleAssignmentStmt(Unit s)
  {
    Value lo = ((DefinitionStmt) s).getLeftOp();
    Value ro = ((DefinitionStmt) s).getRightOp();
    Dictionary ret = null;
//    Assuming local can be only Local or ArrayRef type or FieldRef
    if (lo instanceof Local)
    {
      ret = handleRightOperand(lo, ro);
      String obj_id = createObjId(lo.toString(),className,mthdName);
      checkAndDef(lo.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,ret);
//      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    else if(lo instanceof ArrayRef){
//      System.out.println("Array Ref Base "+((ArrayRef) lo).getBase());
      Value leftOp = ((ArrayRef) lo).getBase();
      ret = handleRightOperand(lo,ro);
      String obj_id = createObjId(leftOp.toString(),className,mthdName);
      checkAndDef(leftOp.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,ret);
//      System.out.println(leftOp.toString() + " " + labelManager.getLabel(leftOp.toString(), className, mthdName));
    }
    else if(lo instanceof AbstractInstanceFieldRef){
//      System.out.println(((AbstractInstanceFieldRef) lo).getBase());
//      System.out.println(((AbstractInstanceFieldRef) lo).getField().getSignature());
      ret = handleRightOperand(lo,ro);
      String leftOp = ((AbstractInstanceFieldRef) lo).getBase().toString();
      String signature = ((AbstractInstanceFieldRef) lo).getField().getSignature().toString();
      
      String obj_id = createObjId(leftOp,className,mthdName);
      System.out.println("obj_id"+obj_id);
      checkAndDef(leftOp.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,ret);
      addLocalToField(obj_id,className+"."+signature);
//      privateFieldsLocals.put(className+"."+signature,leftOp);
      if(checkPrivateField(leftOp,className,mthdName)){
        
        privateFields.add(className+"."+signature);
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Sensitive Field Stored :"+ ro);
        System.out.println("---------------------------------------------------------------------------");
        ps.println("---------------------------------------------------------------------------");
        ps.println("Sensitive Field Stored :"+ ro);
        ps.println("---------------------------------------------------------------------------");
      }
    }
    
    return ret;
  }
  public Dictionary handleRightOperand(Value lo,Value ro)
  {
    Dictionary ret = null;
    if (ro instanceof Local)
    {
      System.out.println("Check for subject label and assign sublabel to lo");

      ret = handleLocalStatement(ro);
    }
    else if(ro instanceof ArrayRef){
      Value rightOpt = ((ArrayRef) ro).getBase();
      ret  = handleRightOperand(lo,rightOpt);
    }
    
    else if (ro instanceof InvokeExpr)
    {
      System.out.println("Assignment + InvokeExper");
      ret = handleInvokeExpr(ro);
//        System.out.println("handleInvokeExpr returned");
    }
    else if(ro instanceof NewExpr)
    {
      
      Type type = ro.getType();
      System.out.println("newExpr" + ro.getType());
      if (sensitive_class.contains(type.toString()))
      {
        Dictionary privateLabel = makeRWLabel.createPrivateLabel();
        ret = privateLabel;

      }
      else{
          checkAndDef(ro.toString(),className,mthdName);
      }
      ret = labelManager.getLabel(ro.toString(),className,mthdName);
    }
    else if(ro instanceof CastExpr){
      Value val =((CastExpr) ro).getOp();
      Type tpe = ro.getType();
      if(sensitive_class.contains(tpe.toString())){
        Dictionary privateLabel = makeRWLabel.createPrivateLabel();
        new RWLabel().checkRead(subLabel,privateLabel,labelManager);
        checkAndUpdate(val.toString(),className,mthdName);
        
      }
      else{
        checkAndDef(val.toString(),className,mthdName);
      }
      ret = labelManager.getLabel(val.toString(),className,mthdName);
//        String obj_id = createObjId(lo.toString(),className,mthdName);
//        labelManager.updateLabel(obj_id,labelManager.getLabel(ro.toString(),className,mthdName));
    }
    else if(ro instanceof AbstractInstanceFieldRef){
//      System.out.println(((AbstractInstanceFieldRef) ro).getBase());
//      System.out.println(((AbstractInstanceFieldRef) ro).getField().getSignature());
      Value base = ((AbstractInstanceFieldRef) ro).getBase();
      String signature = ((AbstractInstanceFieldRef) ro).getField().getDeclaration().toString();
      String obj_id = createObjId(base.toString(),className,mthdName);
      addLocalToField(obj_id,className+"."+signature);//// Add addLocalToField(obj_id(lo),signature);
      obj_id = createObjId(lo.toString(),className,mthdName);
      addLocalToField(obj_id,className+"."+signature);
      if(privateFields.contains(className+"."+signature)){
        Dictionary privateLabel = makeRWLabel.createPrivateLabel();
        new RWLabel().checkRead(subLabel,privateLabel,labelManager);
        checkAndDef(base.toString(),className,mthdName);
        ret = privateLabel;
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Sensitive Field Access :"+ ro);
        System.out.println("---------------------------------------------------------------------------");
        ps.println("---------------------------------------------------------------------------");
        ps.println("Sensitive Field Access :"+ ro);
        ps.println("---------------------------------------------------------------------------");

      }
      else{
        Dictionary publicLabel = createPublicLabel("dummyPublicLabel."+className+"."+mthdName);
        ret = publicLabel;
      }
    }
    else
    {
      System.out.println("Assignment + else");
      Type type = ro.getType();
      System.out.println(type);
      

      if (sensitive_class.contains(type.toString()))
      {
        Dictionary privateLabel = makeRWLabel.createPrivateLabel();
        ret = privateLabel;
      }
      else
      {
        Dictionary publicLabel = createPublicLabel("dummyLabel."+className+"."+mthdName);
        ret = publicLabel;
      }
    }
    
    return ret;
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
