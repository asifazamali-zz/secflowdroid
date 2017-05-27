package Main.java;

import ifc.LabelManager;
import ifc.RWLabel;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by asif on 5/26/17.
 */
public class MakeRWLabel
{
  public RWLabel rwLabel = new RWLabel();
  public LabelManager labelManager;
  public MakeRWLabel(){
     rwLabel = new RWLabel();
//     labelManager = new LabelManager();
  }
  public Dictionary makeRWLabel(Dictionary subLabel,String obj_id,boolean pub,LabelManager labelManager){
    Set<String> readerSet = (Set) subLabel.get("readers");
    Set<String> writerSet = (Set) subLabel.get("writers");
    Dictionary ret = new Hashtable();
    Dictionary temp2 = rwLabel.createObjLabel(subLabel,obj_id,labelManager);
    if(pub)
    {
//      Set<String> readers = getAllReaders();
//      Dictionary temp3 = new Hashtable();
//      Dictionary objLbl = new Hashtable();
//      objLbl.put("owner", subLabel.get("owner"));
//      objLbl.put("readers", readers);
//      objLbl.put("writers", writerSet);
//    if(rwLabel.checkDowngrade(subLabel,temp2,temp3){
//           labelManager.updateLabel(obj_id,temp3);
//           ret.put("bool",true);
//             ret.put("type","public");
//     }
//      else{
      ret.put("boolean", false);
      ret.put("type", "public");
      ret.put("label",temp2);
//    }
    
    }
    else{
      ret.put("boolean",true);
      ret.put("type","private");
      ret.put("label",temp2);
    }
    System.out.println("Label created "+obj_id+" readers "+readerSet+" writers "+writerSet);
    return  ret;
  }
  public Dictionary makeSubLabel(ArrayList<String> owner, ArrayList<String> readers){
    Dictionary ret = new Hashtable();
    ret.put("owner",owner);
    ret.put("readers",readers);
    ret.put("writers",owner);
    System.out.println("label:"+owner+","+readers+","+owner+" created");
    return ret;
  }
  
}
