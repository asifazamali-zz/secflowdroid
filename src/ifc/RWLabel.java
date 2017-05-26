package ifc;



import java.util.*;

/**
 * Created by asif on 5/26/17.
 */
public class RWLabel
{
  public Dictionary createObjLabel(Dictionary sublabel,String obj_id){
    
    Dictionary objectLabel = new Hashtable();
    Set<String> hashSet = new HashSet<>();
    hashSet.add(obj_id);
    objectLabel.put("obj_id",hashSet);
    objectLabel.put("owner",sublabel.get("owner"));
    objectLabel.put("readers",sublabel.get("readers"));
    objectLabel.put("writers",sublabel.get("writers"));
    LabelManager labelManager = new LabelManager();
    if(labelManager.saveLabel(obj_id,objectLabel))
      return objectLabel;
    return null;
  }
  
  
  // returning dictionary first element : bool secondelement: dictionary
  public Dictionary checkRead(Dictionary subLabel,Dictionary objLabel){
    Dictionary ret = new Hashtable();
    if(subLabel.get("owner").subsetOf(objLabel.get("owner"))){
      Dictionary newsubLabel = changeLabelRead(subLabel,objLabel);
      ret.put("bool",true);
      ret.put("subLabel",newsubLabel);
      
    }
    else{
      ret.put("bool",false);
      ret.put("subLabel",subLabel);
    }
    return ret;
  }
  
  private Dictionary changeLabelRead(Dictionary subLabel,Dictionary objLabel){
    Set<String> newR,newW;
    newR = subLabel.get("readers").intersection(objLabel.get("readers"));
    newW = subLabel.get("writers").intersection(objLabel.get("writers"));
    Dictionary newSubLabel = new Hashtable();
    newSubLabel.put("owner",subLabel.get("owner"));
    newSubLabel.put("readers",newR);
    newSubLabel.put("writers",newW);
    return newSubLabel;
  }
  
  public boolean checkWrite(Dictionary subLabel,Dictionary objLabel){
    boolean temp = subLabel.get("owner").subsetOf(objLabel.get("writers")) && subLabel.get("readers").supersetOf(objLabel.get("readers")) && subLabel.get("writers").subsetOf(objLabel.get("writers"));
    if(temp)
      return true;
    return false;
  }
  
  
//  public checkUpgrade(Dictionary subLabel,Dictionary objTemp2, Dictionary objTemp3){
//    if(subLabel.get("owner").subsetOf(objtemp2.get("readers")) && subLabel.get("owner").equals(objTemp2.get("owner")) && subLabel.get("owner").equals(objTemp3.get("owner")) and 
//  }
}


