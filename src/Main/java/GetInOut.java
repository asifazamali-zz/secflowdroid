
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.util.*;
@SuppressWarnings("unchecked")
public class GetInOut extends ForwardFlowAnalysis{
  public Map<Unit, List> flowMapBefore;
  public Map<Unit, List> flowMapAfter;

  public GetInOut(DirectedGraph graph){
    super(graph);
    doAnalysis();

    flowMapBefore = new HashMap<Unit, List>();
    flowMapAfter = new HashMap<Unit, List>();

    Iterator unitItr = graph.iterator();
    while(unitItr.hasNext()){
      Unit s = (Unit) unitItr.next();
      FlowSet beforeSet = (FlowSet) getFlowBefore(s);
      FlowSet afterSet = (FlowSet) getFlowAfter(s);

      flowMapBefore.put(s, Collections.unmodifiableList(beforeSet.toList()));
      flowMapAfter.put(s, Collections.unmodifiableList(afterSet.toList()));
    }
  }

  @Override
  protected void flowThrough(Object srcValue, Object u, Object destValue){
    FlowSet src = (FlowSet) srcValue,
      dest = (FlowSet) destValue;
    Unit unit = (Unit) u;

    src.copy(dest);

    for(ValueBox defBox: unit.getDefBoxes()){
      Value value = defBox.getValue();
      if(value instanceof Local){
        dest.add(value);
      }
    }
  }

  @Override
  protected Object newInitialFlow(){
    return new ArraySparseSet();
  }

  @Override
  protected Object entryInitialFlow(){
    return new ArraySparseSet();
  }

  @Override
  protected void merge(Object in1, Object in2, Object out){
    FlowSet src1 = (FlowSet) in1,
      src2 = (FlowSet) in2,
      dest = (FlowSet) out;
    src1.union(src2,dest);
  }
  @Override
  protected void copy(Object src,Object dest){
    FlowSet sourceSet = (FlowSet) src,
      destSet = (FlowSet) dest;
    sourceSet.copy(destSet);
  }

  public List getInOutBefore(Unit u){
    return flowMapBefore.get(u);
  }
  public List getInOutAfter(Unit u){
    return flowMapAfter.get(u);
  }
}