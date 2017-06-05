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

import java.util.*;

import static Main.java.Util.*;
import static callgraph.InformationFlowAnalysis.*;
import static callgraph.InformationFlowAnalysis.labelManager;
import static callgraph.InformationFlowAnalysis.makeRWLabel;


/**
 * Created by asif on 5/31/17.
 */
public class StatementHanding
{
  
  public String className,mthdName;
  public HashSet refLocals  = new HashSet();
  public HashSet processedClassNames;
  public StatementHanding(String className,String mthdName, HashSet refLocals){
    this.className = className;
    this.mthdName = mthdName;
    this.refLocals = refLocals;
    processedClassNames = new HashSet();
  }

  public  ArrayList getParameters(InvokeExpr invokeExpr,String className, String methodName){
    ArrayList<Dictionary> paraLabels = new ArrayList<>();
    if(invokeExpr.getArgCount() > 0){
      List<Value> values = invokeExpr.getArgs();
      System.out.println("method Arguments");
      for(Value value: values){
        if(refLocals.contains(value.toString())){
          checkAndDef(value.toString(),className,mthdName);
          paraLabels.add(labelManager.getLabel(value.toString(),className,mthdName));
        }
      }
    }
    return paraLabels;
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

    if (cName.contains(Util.appPackageName) && !method.contains("<init>"))
    {
      processedClassNames.add(className+mthdName+cName+method);
      System.out.println("Parse Method: " + method+"inside class "+className+"."+mthdName+"."+cName+"."+method);
      System.out.println(processedClassNames);
      ArrayList<String> paraLabels = getParameters(invokeExpr,className,mthdName);
      ProcessLocalMethod processLocalMethod = new ProcessLocalMethod(cName,sootMethod,paraLabels);
      ret = processLocalMethod.processLocalMethod();
      System.out.println("Returning from processLocalMethod");
      System.out.println("----------------------------------------------");
      System.out.println(LabelManager.d);
      System.out.println("----------------------------------------------");
      System.out.println(Util.global_output_api_method);
//            labelManager.updateLabel(lo.toString(),labelManager.getLabel(ro.toString()));
    }
    else
    {
      System.out.println("arguments");
      List<Value> args = invokeExpr.getArgs();
      Dictionary publicLabel1 = null;
      Dictionary publicLabel2 = createPublicLabel("dummyPublicLabel2");
      ;
      if (invokeExpr.getArgCount() > 0)
      {
        publicLabel1 = createPublicLabel("dummyPublicLabel1");


      }
      for (Value v : args)
      {
        System.out.println(v.toString());
        if (refLocals.contains(v.toString()))
        {
          if (labelManager.getLabel(v.toString(), className, mthdName) == null)
            new RWLabel().createObjLabel(publicLabel1, v.toString(), labelManager, className, mthdName);
          else
            System.out.println(labelManager.getLabel(v.toString(), className, mthdName));
          publicLabel2 = new RWLabel().checkRead(publicLabel2, labelManager.getLabel(v.toString(), className, mthdName), labelManager);
        }
      }
//      ro   = LUB{publicLabel2, base}
//      base = ro
      if (base != null && refLocals.contains(base.toString()))
      {
        checkAndDef(base.toString(), className, mthdName);
        publicLabel2 = new RWLabel().checkRead(publicLabel2, labelManager.getLabel(base.toString(), className, mthdName), labelManager);
        String obj_id = createObjId(base.toString(), className, mthdName);
        System.out.println(obj_id+" "+publicLabel2);
        labelManager.updateLabel(obj_id, publicLabel2); 
      }
      //    Make all the args share the LUB{base,arg1,arg2,.....}
      for (Value v : args)
      {
        if (refLocals.contains(v.toString()))
        {
          String obj_id = createObjId(v.toString(), className, mthdName);
          labelManager.updateLabel(obj_id, publicLabel2);
        }
      }
      if (Util.global_output_api_method.containsKey(cName) && Util.global_output_api_method.get(cName).contains(method))
      {
        System.out.println("Inside global_output_apis_method");
        Dictionary publicLabel = createPublicLabel("dummyLabel." + className + "." + mthdName);
        System.out.println("check write with " + publicLabel2);
        new RWLabel().checkWrite(publicLabel2, publicLabel);
      } else if (sensitive_api_method.containsKey(cName) && sensitive_api_method.get(cName).contains(method))
      {
        System.out.println("-----------------------------------------------");
        System.out.println("Sensitive api :" + cName + " " + method);

        System.out.println("-----------------------------------------------");

        Dictionary privateLabel = Util.makeRWLabel.createPrivateLabel();
        subLabel = new RWLabel().checkRead(subLabel, privateLabel, labelManager);
        System.out.println("changed subLabel " + subLabel);
        if (base != null && refLocals.contains(base.toString()))
        {
          checkAndUpdate(base.toString(), className, mthdName);
          System.out.println("updated obj_label " + base.toString() + " " + labelManager.getLabel(base.toString(), className, mthdName));
        }
        publicLabel2 = subLabel;
      }
      ret = publicLabel2;
    }
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

  public Dictionary handleIdentityStmt(InformationFlowAnalysis info, Unit s)
  {
    Value lo = ((DefinitionStmt) s).getLeftOp();
    Value ro = ((DefinitionStmt) s).getRightOp();
    Dictionary ret;
    if(lo instanceof Local)
    {
      if (ro.toString().contains("@this")){
        Dictionary publicLabel = createPublicLabel("dummyLabel");
        new RWLabel().createObjLabel(publicLabel,lo.toString(),labelManager,className,mthdName);
      }

      else if (ro.toString().contains("@parameter")){
        if(info.paraLabels.size() > 0 && (++(info.paraIndex)) < (info.paraLabels).size())
        {
          System.out.println("Identity :assigning Predefined labels " +  info.paraIndex+" "+ info.paraLabels +" "+info.paraLabels.size());
          checkAndDef(lo.toString(),className,mthdName);
          String obj_id = createObjId(lo.toString(),className,mthdName);
          labelManager.updateLabel(obj_id,info.paraLabels.get(info.paraIndex));
        }
        else{
          Dictionary publicLabel = createPublicLabel("dummyLabel");
          new RWLabel().createObjLabel(publicLabel, lo.toString(), labelManager, className, mthdName);
          ret = publicLabel;
        }
        
      }
      else{
        Dictionary publicLabel = createPublicLabel("dummyLabel");
        new RWLabel().createObjLabel(publicLabel, lo.toString(), labelManager, className, mthdName);
      }
      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    ret = labelManager.getLabel(lo.toString(),className,mthdName);
    
    return ret;
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
      System.out.println("returned from handle right Handle Operand"+ret);
      String obj_id = createObjId(lo.toString(),className,mthdName);
      checkAndDef(lo.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,ret);
      System.out.println(lo.toString() + " " + labelManager.getLabel(lo.toString(), className, mthdName));
    }
    else if(lo instanceof ArrayRef){
      System.out.println("Array Ref Base "+((ArrayRef) lo).getBase());
      Value leftOp = ((ArrayRef) lo).getBase();
      ret = handleRightOperand(lo,ro);
      String obj_id = createObjId(leftOp.toString(),className,mthdName);
      checkAndDef(leftOp.toString(),className,mthdName);
      labelManager.updateLabel(obj_id,ret);
//      System.out.println(leftOp.toString() + " " + labelManager.getLabel(leftOp.toString(), className, mthdName));
    }
    else if(lo instanceof AbstractInstanceFieldRef)
    {
//      System.out.println(((AbstractInstanceFieldRef) lo).getBase());
//      System.out.println(((AbstractInstanceFieldRef) lo).getField().getSignature());
      ret = handleRightOperand(lo, ro);
      String leftOp = ((AbstractInstanceFieldRef) lo).getBase().toString();
      String signature = ((AbstractInstanceFieldRef) lo).getField().getSignature().toString();

      String obj_id = createObjId(leftOp, className, mthdName);
      System.out.println("obj_id" + obj_id);
      checkAndDef(leftOp.toString(), className, mthdName);
      labelManager.updateLabel(obj_id, ret);

      addLocalToField(obj_id, className + "." + signature);
//      privateFieldsLocals.put(className+"."+signature,leftOp);
      if (checkPrivateField(leftOp, className, mthdName))
      {

        privateFields.add(className + "." + signature);
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Sensitive Field Stored :" + ro);
        System.out.println("---------------------------------------------------------------------------");
        ps.println("---------------------------------------------------------------------------");
        ps.println("Sensitive Field Stored :" + ro);
        ps.println("---------------------------------------------------------------------------");
      }
    }
    else{
      System.out.println("Assignment + lo +else "+lo.toString());
      String obj_id = createObjId(lo.toString(),className,mthdName);
      ret = createPublicLabel(obj_id);
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
        System.out.println("Inside Assignment + ro + else "+ ro.toString());
          checkAndDef(ro.toString(),className,mthdName);
      }
      ret = labelManager.getLabel(ro.toString(),className,mthdName);
    }
    else if(ro instanceof CastExpr)
    {
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
    else if(ro instanceof AbstractInstanceFieldRef)
    {
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
        if(base != null){
          publicLabel = new RWLabel().checkRead(publicLabel,labelManager.getLabel(base.toString(),className,mthdName),labelManager);
        }
        ret = publicLabel;
      }
    }
    else
    {
      System.out.println("Assignment +ro + else");
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
      System.out.println("returning from ro else");
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
  public Dictionary handleIfStatement(InformationFlowAnalysis info, Iterator itr, Unit s){
    Value v = ((IfStmt) s).getCondition();
    System.out.println("inside if statements");
    Dictionary ret = null;
    Dictionary publicLabel = createPublicLabel("dummyLabel"+className+mthdName);
    if(v instanceof JInstanceOfExpr){
      JInstanceOfExpr expr = (JInstanceOfExpr) v;
      Value val = handleInstanceExper(expr);
      checkAndDef(val.toString(),className,mthdName);
      publicLabel = new RWLabel().checkRead(publicLabel,labelManager.getLabel(v.toString(),className,mthdName),labelManager);
//      new RWLabel().checkRead(subLabel,labelManager.getLabel(val.toString(),className,mthdName),labelManager);
    }
    else if(v instanceof JEqExpr || v instanceof JNeExpr){
      System.out.println(v);
      AbstractBinopExpr abstractBinopExpr = (AbstractBinopExpr) v;
      Value lo = abstractBinopExpr.getOp1();
      Value ro = abstractBinopExpr.getOp2();
      checkAndDef(lo.toString(),className,mthdName);
      checkAndDef(ro.toString(),className,mthdName);
      publicLabel = new RWLabel().checkRead(publicLabel,labelManager.getLabel(lo.toString(),className,mthdName),labelManager);
      publicLabel = new RWLabel().checkRead(publicLabel,labelManager.getLabel(ro.toString(),className,mthdName),labelManager);
    }
    while(itr.hasNext()){
      Unit unit = (Unit) itr.next();
      if(unit instanceof ReturnVoidStmt)
      {
        System.out.println("returning from if null");
        return null;
      }
      else if(unit instanceof ReturnStmt)
      {
        ret = handleReturn(unit);
        return ret;
      }
      else
        ret =  info.handleStatement(itr,unit);
      if(ret != null){
        publicLabel = new RWLabel().checkRead(publicLabel,ret,labelManager);
      }
    }
    System.out.println("returning from if"+publicLabel);

    return publicLabel;
  }
  public Dictionary handleIfElseStatement(InformationFlowAnalysis info, Iterator itr,Unit s){
    Dictionary ret = null;
    System.out.println("Inside If/else");
    ret = handleIfStatement(info,itr,s);
    System.out.println("handle IfElse + ret from handleIfStatement "+ret);
    Dictionary publicLabel = createPublicLabel("dummyLabel"+className+mthdName);
    
    if(ret != null)
    {
      publicLabel = new RWLabel().checkRead(publicLabel,ret,labelManager);
    }
    while(itr.hasNext()){
      Unit unit = (Unit) itr.next();
      if(unit instanceof ReturnVoidStmt)
      {
        System.out.println("returning from if null");
        return null;
      }
      else if(unit instanceof ReturnStmt)
      {
        ret = handleReturn(unit);
        return ret;
      }
      else
        ret =  info.handleStatement(itr,unit);
      if(ret != null){
        publicLabel = new RWLabel().checkRead(publicLabel,ret,labelManager);
      }      
    }
    System.out.println("returning from else "+publicLabel);
    return publicLabel;
  }

  public Dictionary handleReturnVoid(Unit u){
    return null;
  }
  public Dictionary handleReturn(Unit u){
    if(refLocals.contains(u.toString())){
      return labelManager.getLabel(u.toString(),className,mthdName);
    }
    return null;
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
