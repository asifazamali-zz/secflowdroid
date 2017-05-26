package ifc;

import javax.print.attribute.standard.DialogTypeSelection;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by asif on 5/26/17.
 */
public class LabelManager
{
  public Dictionary d = new Hashtable();
  public Dictionary getLabel(Dictionary d,String str){
    if(d.get(str) != null){
      return (Dictionary) d.get(str);
    }
    return null;
  }
  public boolean saveLabel(String obj_id,Dictionary label){
    if(d.put(obj_id,label) != null)
      return true;
    return false;
  }
  public boolean updateLabel(String obj_id,Dictionary objLabel){
    Dictionary label = (Dictionary) d.get(obj_id);
    if(label != null){
      d.put(obj_id,objLabel);
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
