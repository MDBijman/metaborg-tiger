package org.spoofax;

import org.apache.commons.lang3.tuple.Pair;
import org.strategoxt.lang.Context;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.terms.io.TAFTermReader;
import org.spoofax.terms.TermFactory;
import java.io.IOException;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.function.Supplier;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoList;
import flock.subject.common.Graph;
import flock.subject.common.Analysis;
import flock.subject.common.Graph.Node;
import flock.subject.common.Analysis.Direction;
import flock.subject.common.CfgNodeId;
import flock.subject.common.Dependency;
import flock.subject.common.Helpers;
import flock.subject.common.FlockLattice;
import flock.subject.common.FlockLattice.MaySet;
import flock.subject.common.FlockLattice.MustSet;
import flock.subject.common.FlockLattice.SimpleMap;
import flock.subject.common.FlockLattice.FlockValueLattice;
import flock.subject.common.FlockLattice.FlockCollectionLattice;
import flock.subject.common.FlockValue;
import flock.subject.common.FlockValue.FlockValueWithDependencies;
import flock.subject.common.MapUtils;
import flock.subject.common.SetUtils;
import flock.subject.common.TransferFunction;
import flock.subject.common.UniversalSet;

public class FlowAnalysisStrategies {
  public static class get_values_0_0 extends Strategy {
    public static get_values_0_0 instance = new get_values_0_0 ( );
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
                                                                                    ITermFactory factory = context. getFactory( );
                                                                                    TermId id = new TermId (((IStrategoInt ) current). intValue( ));
                                                                                    Node node = Flock.instance. getNode(id);
                                                                                    if(node == null) {
                                                                                                       Flock. printDebug("CfgNode is null with id " + id. getId( ));
                                                                                                       return null;
                                                                                                     }
                                                                                    Flock.instance. analysisWithName("values"). performDataAnalysis(Flock.instance.graph, Flock.instance.graph_scss, node);
                                                                                    IStrategoList result = factory. makeList(((Map<IStrategoTerm, IStrategoTerm> ) node. getProperty("values").lattice. value( )). entrySet( ). stream( ). map(n -> factory. makeTuple(Helpers. toTerm(n. getKey( )), Helpers. toTerm(n. getValue( )))). collect(Collectors. toList( )));
                                                                                    return result;
                                                                                  }
  }
  public static class get_values_0_1 extends Strategy {
    public static get_values_0_1 instance = new get_values_0_1 ( );
    @Override public IStrategoTerm invoke(Context context,IStrategoTerm current, IStrategoTerm key) {
                                                                                                      ITermFactory factory = context. getFactory( );
                                                                                                      TermId id = new TermId (((IStrategoInt ) current). intValue( ));
                                                                                                      Node node = Flock.instance. getNode(id);
                                                                                                      if(node == null) {
                                                                                                                         Flock. printDebug("CfgNode is null with id " + id. getId( ));
                                                                                                                         return null;
                                                                                                                       }
                                                                                                      Flock.instance. analysisWithName("values"). performDataAnalysis(Flock.instance.graph, Flock.instance.graph_scss, node);
                                                                                                      Map<IStrategoTerm, Object> values = (Map<IStrategoTerm, Object> ) node. getProperty("values").lattice. value( );
                                                                                                      Object value = values. get(key);
                                                                                                      if(value == null) {
                                                                                                                          return null;
                                                                                                                        }
                                                                                                      IStrategoTerm asTerm = Helpers. toTerm(value);
                                                                                                      return asTerm;
                                                                                                    }
  }
}