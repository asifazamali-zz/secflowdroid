package ifc;

import Main.java.Util;

import javax.print.attribute.standard.DialogTypeSelection;
import java.awt.*;
import java.util.*;

import static Main.java.Util.checkPrivateField;
import static Main.java.Util.fieldsLocals;
import static Main.java.Util.privateFields;

/**
 * Created by asif on 5/26/17.
 */
public class LabelManager
{
  public static Dictionary d = new Hashtable();
  public LabelManager(){
  } 
  public Dictionary getLabel(String str,String className,String methodName){
    String obj_id = className+"."+methodName+"."+str;
//    System.out.println("query :"+obj_id);
//    if(obj_id == "com.example.asif.gpstracking.GPSTracker..getLongitude.$r0")
//      System.out.println(d);
    if(d.get(obj_id) != null){
      return (Dictionary) d.get(obj_id);
    }
    return null;
  }
  public boolean saveLabel(String obj_id,Dictionary label){
//    obj_id = className+"."+methodName+"."+obj_id;
    d.put(obj_id,label);
    
   System.out.println("saveLabel: "+obj_id+"label "+label);
    return true;
  }
  public static boolean updateLabel(String obj_id,Dictionary objLabel){
//    obj_id = className+"."+methodName+"."+obj_id;
//    System.out.println(obj_id);
//    if(obj_id.contains("com.example.asif.gpstracking.GPSTracker.getLongitude.$r0"))
//      System.out.println(d);
    Dictionary label = (Dictionary) d.get(obj_id);
    if(label != null){
      Dictionary newLabel = new Hashtable();
//      newLabel.put("owner",((Dictionary)(d.get(obj_id).get("owner")));
//      newLabel.put("readers",d.get(obj_id).
      d.put(obj_id,objLabel);
      if(checkPrivate(objLabel) && fieldsLocals.containsKey(obj_id)){
        System.out.println("updating fields"+fieldsLocals);
        updateFields(obj_id);
      }
      return true;
    }
    Util.ps.println(obj_id+ "not updated");
    System.out.println(obj_id+" not updated");
    return false;
  }
  public static boolean updateFields(String obj_id){
    HashSet fields  = fieldsLocals.get(obj_id);
    Iterator itr = fields.iterator();
    while(itr.hasNext()){
      String field = (String) itr.next();
      
      if(! privateFields.contains(field))
      {
        System.out.println("=======================================================");
        System.out.println("Adding "+field);
        System.out.println("======================================================="); 
        
        privateFields.add(field);
      }
    }
    return false;
  }
  public static boolean checkPrivate(Dictionary label){
    if(((Set)label.get("writers")).size()>1)
    {
//      System.out.println("checking for private"+label);
//      System.out.println(fieldsLocals);

      return true;
    }
    return false;
  }
  public boolean deleteLabel(String obj_id){
    if(d.remove(obj_id) != null)
      return true;
    return false;
    
  }
}
