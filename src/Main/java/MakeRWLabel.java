package Main.java;

import fj.Hash;
import ifc.LabelManager;
import ifc.RWLabel;

import java.util.*;

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
  public Dictionary makeRWLabel(Dictionary subLabel,String obj_id,boolean pub,LabelManager labelManager,String className,String methodName){
    Set<String> readerSet = (Set) subLabel.get("readers");
    Set<String> writerSet = (Set) subLabel.get("writers");
    Dictionary ret = new Hashtable();
//    System.out.println("lableManager :"+labelManager);
    Dictionary temp2 = rwLabel.createObjLabel(subLabel,obj_id,labelManager,className,methodName);
    if(pub)
    {

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
//    System.out.println("Label created "+className+"."+methodName+"."+obj_id+" readers "+readerSet+" writers "+writerSet);
    return  ret;
  }
  public Dictionary makeSubLabel(HashSet<String> owner, HashSet<String> readers){
    Dictionary ret = new Hashtable();
    ret.put("owner",owner);
    ret.put("readers",readers);
    ret.put("writers",owner);
//    System.out.println("label:"+owner+","+readers+","+owner+" created");
    return ret;
  }
  public Dictionary createPrivateLabel(){
    Dictionary ret = new Hashtable();
    HashSet<String> hashSet = new HashSet<>();
    hashSet.add(Util.appPackageName);
    ret.put("owner", hashSet);
    ret.put("readers",hashSet);
    HashSet<String> set = new HashSet<>();
    set.add(Util.appPackageName);
    set.add("public");
    ret.put("writers",set);
    return ret;
  }
  public static Dictionary createPublicLabel(){
    Dictionary ret = new Hashtable();
    HashSet<String> hashSet = new HashSet<>();
    hashSet.add(Util.appPackageName);
    ret.put("writers",hashSet);
    hashSet.add("public");
    ret.put("readers",hashSet);
    hashSet = new HashSet<>();
    hashSet.add("public");
    ret.put("owner",hashSet);
    return ret;
  }
}
