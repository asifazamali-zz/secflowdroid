package ifc;

import Main.java.Util;

import javax.print.attribute.standard.DialogTypeSelection;
import java.awt.*;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

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
  public boolean saveLabel(String obj_id,Dictionary label,String className,String methodName){
//    obj_id = className+"."+methodName+"."+obj_id;
    d.put(obj_id,label);
    
   System.out.println("saveLabel: "+obj_id+"label "+label);
    return true;
  }
  public boolean updateLabel(String obj_id,Dictionary objLabel,String className,String methodName){
    obj_id = className+"."+methodName+"."+obj_id;
//    System.out.println(obj_id);
//    if(obj_id.contains("com.example.asif.gpstracking.GPSTracker.getLongitude.$r0"))
//      System.out.println(d);
    Dictionary label = (Dictionary) d.get(obj_id);
    if(label != null){
      Dictionary newLabel = new Hashtable();
//      newLabel.put("owner",((Dictionary)(d.get(obj_id).get("owner")));
//      newLabel.put("readers",d.get(obj_id).
      d.put(obj_id,objLabel);
      return true;
    }
    Util.ps.println(obj_id+ "not updated");
    return false;
  }
  public boolean deleteLabel(String obj_id){
    if(d.remove(obj_id) != null)
      return true;
    return false;
    
  }
}
