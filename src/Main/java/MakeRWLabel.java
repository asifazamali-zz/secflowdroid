package Main.java;

import ifc.LabelManager;
import ifc.RWLabel;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by asif on 5/26/17.
 */
public class MakeRWLabel
{
  public RWLabel rwLabel = new RWLabel();
  public LabelManager labelManager = new LabelManager();
  public boolean makeRWLabel(Dictionary subLabel,String obj_id,boolean pub){
    Set<String> readerSet = (Set) subLabel.get("readers");
    Set<String> writerSet = (Set) subLabel.get("writers");
    Dictionary ret = new Hashtable();
    Dictionary temp2 = rwLabel.createObjLabel(subLabel,obj_id);
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
//    }
    
    }
    else{
      ret.put("boolean",true);
      ret.put("type","private");
    }
  }
  public Dictionary makeSubLabel(String owner,String readers){
    Dictionary ret = new Hashtable();
    ret.put("owner",owner);
    ret.put("readers",readers);
    ret.put("writers",owner);
    return ret;
  }
  
}
