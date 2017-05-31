package callgraph;

import Main.java.Util;
import heros.flowfunc.Identity;
import ifc.LabelManager;
import ifc.RWLabel;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MemoryEfficientGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import static Main.java.Util.makeRWLabel;
import static Main.java.Util.sensitive_api_method;
import static Main.java.Util.sensitive_class;
import static callgraph.InformationFlowAnalysis.subLabel;

/**
 * Created by asif on 5/28/17.
 */
public class ProcessLocalMethod
{
  public RWLabel rwLabel;
  public Dictionary processLocalMethod(SootMethod sootMethod,String className)
  {
    Body b = sootMethod.retrieveActiveBody();
    Chain units = b.getUnits();
    String methodName = sootMethod.getName();
    LabelManager labelManager = Util.labelManager;
    Dictionary ret = null;
    rwLabel = new RWLabel();
    Iterator localIt = b.getLocals().iterator();
//    System.out.println("locals");
    
    while(localIt.hasNext())
    {
      Local l = (Local) localIt.next();
      System.out.println(l.getName());
      String obj_id = l.getName();
//      if ((labelManager.getLabel(obj_id, className, methodName)) == null)
//      {
//        Dictionary objLabel = makeRWLabel.makeRWLabel(subLabel, obj_id, true, labelManager, className, methodName);
//      }
    }
    
    for (Object u : units)
    {
      Unit un = (Unit) u;
      Util.ps.println(u.toString());
//      if (u instanceof AssignStmt)
//        System.out.println("Assignment Stmt: "+u.toString());
//      if (u instanceof DefinitionStmt)
//        System.out.println("DefinitionStmt: "+u.toString());
//      if (u instanceof IdentityStmt)
//        System.out.println("IdentityStmt: "+u.toString());
//      if (u instanceof IfStmt)
//        System.out.println("IfStmt: "+u.toString());
//      if (u instanceof InvokeStmt)
//        System.out.println("InvokeStmt: "+u.toString());
//      if (u instanceof LookupSwitchStmt)
//        System.out.println("LookupSwitchStmt: "+u.toString());
//      if (u instanceof ReturnStmt)
//        System.out.println("ReturnStmt: "+u.toString());
//      if (u instanceof RetStmt)
//        System.out.println("RetStmt: "+u.toString());
//      if (u instanceof ReturnVoidStmt)
//        System.out.println("ReturnVoidStmt: "+u.toString());
      
      if (u instanceof DefinitionStmt)
      {
//        System.out.println("Def statement "+u.toString());
        DefinitionStmt stmt = (DefinitionStmt) u;
        Value lo = stmt.getLeftOp();
        Value ro = stmt.getRightOp();
        if (lo instanceof Local)
        {
          if (ro instanceof Local)
          {
            if(InformationFlowAnalysis.checkAndDef(lo.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,lo.toString())+" created");
            if(InformationFlowAnalysis.checkAndDef(ro.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,ro.toString())+" created");

            // lo <- ro checking and changing labels of subjects

            subLabel = new RWLabel().checkRead(subLabel, labelManager.getLabel(ro.toString(),className,methodName),labelManager);
            // update label of lo 
            labelManager.updateLabel(lo.toString(),labelManager.getLabel(ro.toString(),className,methodName),className,methodName);
          } 
          else if (ro instanceof InterfaceInvokeExpr){
            Util.ps.println("interfaceInvoke ");// handle it later
            Util.ps.println(ro.getUseBoxes().get(0).getValue());
          }
          else if (ro instanceof InvokeExpr)
          {
            if(InformationFlowAnalysis.checkAndDef(lo.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,lo.toString())+" created");
            if(InformationFlowAnalysis.checkAndDef(ro.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,ro.toString())+" created");

            InvokeExpr invokeExpr = (InvokeExpr) ro;
            sootMethod = invokeExpr.getMethod();
            List<Value> args = invokeExpr.getArgs();
            String methdName = sootMethod.getName();

            String cName = sootMethod.getDeclaringClass().getName();
            Util.ps.println("Class :" + cName + " method :" + methdName);
            Util.ps.println("arguments");
            if (className.contains(Util.appPackageName))
            {
//              System.out.println("Process Parse Method: " + methdName);
              
              ////////////// change to recurse /////////
//              Dictionary ret = (Dictionary) new ProcessLocalMethod().processLocalMethod(sootMethod);
//            labelManager.updateLabel(lo.toString(),labelManager.getLabel(ro.toString()));
            }
            if(sensitive_api_method.containsKey(cName) && sensitive_api_method.get(cName).contains(methdName)){
//              System.out.println("inside sensitive method");
              Dictionary privateLabel =  makeRWLabel.createPrivateLabel();
              subLabel = new RWLabel().checkRead(subLabel,privateLabel,labelManager);
              labelManager.updateLabel(ro.getUseBoxes().get(0).getValue().toString(),subLabel,className,methodName);
              Util.ps.println(ro.getUseBoxes().get(0).getValue());
            }
              
            for (Value v : args)
            {
//              System.out.println(v);
              if(labelManager.getLabel(v.toString(),className,methodName) !=null){
                rwLabel.checkRead(subLabel,labelManager.getLabel(v.toString(),className,methodName),labelManager);
              }
            }
//            System.out.println("");
//            System.out.println("After local fnction call "+subLabel);
            labelManager.updateLabel(lo.toString(),subLabel,className,methodName);
          }
          else if(ro instanceof FieldRef){
            if(InformationFlowAnalysis.checkAndDef(lo.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,lo.toString())+" created");
            if(InformationFlowAnalysis.checkAndDef(ro.toString(),className,methodName))
              Util.ps.println(createObjId(className,methodName,ro.toString())+" created");

            SootField sootField = ((FieldRef)ro).getField();
            String subSignatureType = sootField.getSubSignature().split(" ")[0];
            if(Util.sensitive_class.contains(subSignatureType)){
              Util.ps.println("-------------------------------------------");
              String var =ro.getUseBoxes().get(0).getValue().toString();
              String obj_id = createObjId(className,methodName,var);
              Util.ps.println(className+" "+methodName+" "+var);
              Util.ps.println("Changing lable "+obj_id+" label :"+Util.labelManager.getLabel(var,className,methodName));
              Dictionary privateLabel =  makeRWLabel.createPrivateLabel();
              Util.ps.println(privateLabel);
              subLabel = rwLabel.checkRead(subLabel,privateLabel,labelManager);
              if(Util.labelManager.updateLabel(var,privateLabel,className,methodName)){
                Util.ps.println("Changed Label "+Util.labelManager.getLabel(var,className,methodName));
                labelManager.updateLabel(lo.toString(),subLabel,className,methodName);
              }
              else{
                Util.ps.println("Label not changed");
              }
              Util.ps.println("-------------------------------------------");
            }
            

          }
          Util.ps.println(lo.toString()+" "+labelManager.getLabel(lo.toString(),className,methodName));

        }
          
        //        if(ro instanceof FieldRef)
//          System.out.println(ro.getUseBoxes().get(0).getValue().toString()+" "+labelManager.getLabel(ro.getUseBoxes().get(0).getValue().toString(),className,methodName));
//        else
//          System.out.println(ro.toString()+" "+labelManager.getLabel(ro.toString(),className,methodName));

      }
      if(u instanceof ReturnStmt){
//        System.out.println("ReturnStmt");
        Util.ps.println(((ReturnStmt) u).getOp());
        Util.ps.println(labelManager.getLabel(((ReturnStmt) u).getOp().toString(),className,methodName));
        ret = labelManager.getLabel(((ReturnStmt) u).getOp().toString(),className,methodName);
      }
      else if(u instanceof ReturnVoidStmt){
        ret = null;
      }
    }
    return ret;
  }
  public static String createObjId(String className,String methodName,String obj_id){
    return className+"."+methodName+"."+obj_id;
  }
}
