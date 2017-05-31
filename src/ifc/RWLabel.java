package ifc;



import Main.java.Util;
import fj.Hash;

import java.util.*;

/**
 * Created by asif on 5/26/17.
 */
public class RWLabel
{


  public Dictionary createObjLabel(Dictionary sublabel, String obj_id, LabelManager labelManager, String className, String methodName)
  {

    Dictionary objectLabel = new Hashtable();
    obj_id = className + "." + methodName + "." + obj_id;
//    Set<String> hashSet = new HashSet<>();
//    hashSet.add(obj_id);
//    objectLabel.put(className+"."+methodName+"."+obj_id,hashSet);
    objectLabel.put("owner", sublabel.get("owner"));
    objectLabel.put("readers", sublabel.get("readers"));
    objectLabel.put("writers", sublabel.get("writers"));
    if (labelManager.saveLabel(obj_id, objectLabel, className, methodName))
      return objectLabel;
    return null;
  }


  // returning dictionary first element : bool secondelement: dictionary
  public Dictionary checkRead(Dictionary subLabel, Dictionary objLabel, LabelManager labelManager)
  {
    Dictionary ret = new Hashtable();
    if (((Set) objLabel.get("owner")).containsAll((Set) subLabel.get("owner")))
    {
      Dictionary newsubLabel = changeLabelRead(subLabel, objLabel);
      ret = newsubLabel;
    } else
    {
      Util.ps.println("***************** misuse *************");
      Util.ps.println("subLabel "+subLabel+" tries to read "+objLabel);
      Util.ps.println("***************** misuse *************");
      ret = subLabel;
    }
    return ret;
  }

  public Dictionary changeLabelRead(Dictionary subLabel, Dictionary objLabel)
  {
    Set<String> newR, newW;
    newR = (Set) subLabel.get("readers");
    (newR).retainAll((Set) (objLabel.get("readers")));
    newW = (Set) subLabel.get("writers");
    newW = unionSet(newW, (Set) (objLabel.get("writers")));
    Dictionary newSubLabel = new Hashtable();
    newSubLabel.put("owner", subLabel.get("owner"));
    newSubLabel.put("readers", newR);
    newSubLabel.put("writers", newW);
    return newSubLabel;
  }

  public boolean checkWrite(Dictionary subLabel, Dictionary objLabel)
  {
    boolean temp = ((Set) objLabel.get("writers")).containsAll((Set) subLabel.get("owner")) && ((Set) subLabel.get("readers")).containsAll((Set) objLabel.get("readers")) && (((Set) objLabel.get("writers")).containsAll((Set) subLabel.get("writers")));
    if (temp)
      return true;
    return false;
  }

  public Set unionSet(Set s1, Set s2)
  {
    if (s1.size() < s2.size())
    {
      for (Object str : s1)
        s2.add(str);
      return s2;
    } else
    {
      for (Object str : s2)
        s1.add(str);
      return s1;
    }
  }

}  
  
  
//  public checkUpgrade(Dictionary subLabel,Dictionary objTemp2, Dictionary objTemp3){
//    if(subLabel.get("owner").subsetOf(objtemp2.get("readers")) && subLabel.get("owner").equals(objTemp2.get("owner")) && subLabel.get("owner").equals(objTemp3.get("owner")) and 
//  }



