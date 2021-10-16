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

public class FlowAnalysis extends Analysis {
  public FlowAnalysis( ) {
    super("expressions", Direction.FORWARD);
  }
  public void initNodeValue(Node node) {
                                         node. addProperty("expressions", SimpleMap. bottom( ));
                                       }
  private boolean matchPattern0(Node node) {
                                             IStrategoTerm term = node.term;
                                             return true;
                                           }
  private boolean matchPattern1(Node node) {
                                             IStrategoTerm term = node.term;
                                             if(!(TermUtils. isAppl(term) && M. appl(term). getName( ). equals("VarDec") && term. getSubtermCount( ) == 3)) {
                                                                                                                                                              return false;
                                                                                                                                                            }
                                             IStrategoTerm term_0 = Helpers. at(term, 0);
                                             IStrategoTerm term_1 = Helpers. at(term, 1);
                                             IStrategoTerm term_2 = Helpers. at(term, 2);
                                             addNodePatternParent(node, Helpers. getTermNode(term));
                                             return true;
                                           }
  private boolean matchPattern2(Node node) {
                                             IStrategoTerm term = node.term;
                                             return true;
                                           }
  public void initNodeTransferFunction(Node node) {
                                                    {
                                                      if(matchPattern0(node)) {
                                                                                node. getProperty("expressions").transfer = TransferFunctions.TransferFunction0;
                                                                              }
                                                      if(matchPattern1(node)) {
                                                                                node. getProperty("expressions").transfer = TransferFunctions.TransferFunction1;
                                                                              }
                                                      if(matchPattern2(node)) {
                                                                                node. getProperty("expressions").init = TransferFunctions.TransferFunction2;
                                                                              }
                                                    }
                                                  }
  @Override public void performDataAnalysis(Graph g,Collection<Node> roots,Collection<Node> nodeset,Collection<Node> dirty, float intervalBoundary) {
                                                                                                                                                      Queue<Node> worklist = new LinkedBlockingQueue <>( );
                                                                                                                                                      HashSet<Node> inWorklist = new HashSet <>( );
                                                                                                                                                      for( Node node : nodeset) {
                                                                                                                                                                                  if(node.interval > intervalBoundary) continue;
                                                                                                                                                                                  worklist. add(node);
                                                                                                                                                                                  inWorklist. add(node);
                                                                                                                                                                                  initNodeValue(node);
                                                                                                                                                                                  initNodeTransferFunction(node);
                                                                                                                                                                                }
                                                                                                                                                      for( Node node : dirty) {
                                                                                                                                                                                if(node.interval > intervalBoundary) continue;
                                                                                                                                                                                worklist. add(node);
                                                                                                                                                                                inWorklist. add(node);
                                                                                                                                                                                initNodeTransferFunction(node);
                                                                                                                                                                              }
                                                                                                                                                      for( Node root : roots) {
                                                                                                                                                                                if(root.interval > intervalBoundary) continue;
                                                                                                                                                                                root. getProperty("expressions").lattice = root. getProperty("expressions").init. eval(root);
                                                                                                                                                                                this.changedNodes. add(root);
                                                                                                                                                                              }
                                                                                                                                                      for( Node node : nodeset) {
                                                                                                                                                                                  if(node.interval > intervalBoundary) continue;
                                                                                                                                                                                  {
                                                                                                                                                                                    FlockLattice init = node. getProperty("expressions").lattice;
                                                                                                                                                                                    for( Node pred : g. parentsOf(node)) {
                                                                                                                                                                                                                           FlockLattice live_o = pred. getProperty("expressions").transfer. eval(pred);
                                                                                                                                                                                                                           init = init. lub(live_o);
                                                                                                                                                                                                                         }
                                                                                                                                                                                    node. getProperty("expressions").lattice = init;
                                                                                                                                                                                    this.changedNodes. add(node);
                                                                                                                                                                                  }
                                                                                                                                                                                }
                                                                                                                                                      while(!worklist. isEmpty( )) {
                                                                                                                                                                                     Node node = worklist. poll( );
                                                                                                                                                                                     inWorklist. remove(node);
                                                                                                                                                                                     if(node.interval > intervalBoundary) continue;
                                                                                                                                                                                     FlockLattice expressions_n = node. getProperty("expressions").transfer. eval(node);
                                                                                                                                                                                     for( Node successor : g. childrenOf(node)) {
                                                                                                                                                                                                                                  if(successor.interval > intervalBoundary) continue;
                                                                                                                                                                                                                                  boolean changed = false;
                                                                                                                                                                                                                                  FlockLattice expressions_o = successor. getProperty("expressions").lattice;
                                                                                                                                                                                                                                  if(expressions_n. nleq(expressions_o)) {
                                                                                                                                                                                                                                                                           successor. getProperty("expressions").lattice = expressions_o. lub(expressions_n);
                                                                                                                                                                                                                                                                           changed = true;
                                                                                                                                                                                                                                                                         }
                                                                                                                                                                                                                                  if(changed && !inWorklist. contains(successor)) {
                                                                                                                                                                                                                                                                                    worklist. add(successor);
                                                                                                                                                                                                                                                                                    inWorklist. add(successor);
                                                                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                  if(changed) {
                                                                                                                                                                                                                                                this.changedNodes. add(successor);
                                                                                                                                                                                                                                              }
                                                                                                                                                                                                                                }
                                                                                                                                                                                     for( Node successor : g. parentsOf(node)) {
                                                                                                                                                                                                                                 boolean changed = false;
                                                                                                                                                                                                                                 if(successor.interval > intervalBoundary) continue;
                                                                                                                                                                                                                                 if(changed && !inWorklist. contains(successor)) {
                                                                                                                                                                                                                                                                                   worklist. add(successor);
                                                                                                                                                                                                                                                                                   inWorklist. add(successor);
                                                                                                                                                                                                                                                                                 }
                                                                                                                                                                                                                                 if(changed) {
                                                                                                                                                                                                                                               this.changedNodes. add(successor);
                                                                                                                                                                                                                                             }
                                                                                                                                                                                                                               }
                                                                                                                                                                                   }
                                                                                                                                                    }
}
class TransferFunctions {
  public static TransferFunction TransferFunction0 = new TransferFunction0 ( );
  public static TransferFunction TransferFunction1 = new TransferFunction1 ( );
  public static TransferFunction TransferFunction2 = new TransferFunction2 ( );
}
class TransferFunction0 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Node prev = node;
                                                  SimpleMap tmp26 = (SimpleMap ) UserFunctions. expressions_f(prev);
                                                  return tmp26;
                                                }
}
class TransferFunction1 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Node prev = node;
                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                  IStrategoTerm usre = Helpers. at(term, 2);
                                                  Set tmp27 = (Set ) SetUtils. create(usrn);
                                                  Map result0 = new HashMap ( );
                                                  for( Object o : ((Map ) ((FlockLattice ) UserFunctions. expressions_f(prev)). value( )). entrySet( )) {
                                                                                                                                                          Entry entry = (Entry ) o;
                                                                                                                                                          Object usrk = entry. getKey( );
                                                                                                                                                          Object usrv = entry. getValue( );
                                                                                                                                                          result0. put(usrk, new MustSet (SetUtils. difference(((FlockLattice ) usrv). value( ), tmp27)));
                                                                                                                                                        }
                                                  Map tmp28 = (Map ) result0;
                                                  Set tmp29 = (Set ) SetUtils. create(usrn);
                                                  Map tmp30 = (Map ) MapUtils. create(Helpers. toTerm(usre), new MustSet (tmp29));
                                                  Map tmp31 = (Map ) new SimpleMap (MapUtils. union(tmp28, tmp30));
                                                  Map tmp25 = (Map ) tmp31;
                                                  return new SimpleMap (tmp25);
                                                }
}
class TransferFunction2 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Map tmp24 = (Map ) MapUtils. create( );
                                                  return new SimpleMap (tmp24);
                                                }
}
class UserFunctions {
  public static FlockLattice expressions_f(Object o) {
                                                       Node node = (Node ) o;
                                                       return node. getProperty("expressions").lattice;
                                                     }
}