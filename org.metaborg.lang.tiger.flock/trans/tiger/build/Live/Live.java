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
    super("live", Direction.BACKWARD);
  }
  public void initNodeValue(Node node) {
                                         node. addProperty("live", MaySet. bottom( ));
                                       }
  private boolean matchPattern0(Node node) {
                                             IStrategoTerm term = node.term;
                                             return true;
                                           }
  private boolean matchPattern1(Node node) {
                                             IStrategoTerm term = node.term;
                                             if(!(TermUtils. isAppl(term) && M. appl(term). getName( ). equals("Var") && term. getSubtermCount( ) == 1)) {
                                                                                                                                                           return false;
                                                                                                                                                         }
                                             IStrategoTerm term_0 = Helpers. at(term, 0);
                                             addNodePatternParent(node, Helpers. getTermNode(term));
                                             return true;
                                           }
  private boolean matchPattern2(Node node) {
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
  private boolean matchPattern3(Node node) {
                                             IStrategoTerm term = node.term;
                                             return true;
                                           }
  public void initNodeTransferFunction(Node node) {
                                                    {
                                                      if(matchPattern0(node)) {
                                                                                node. getProperty("live").transfer = TransferFunctions.TransferFunction0;
                                                                              }
                                                      if(matchPattern1(node)) {
                                                                                node. getProperty("live").transfer = TransferFunctions.TransferFunction1;
                                                                              }
                                                      if(matchPattern2(node)) {
                                                                                node. getProperty("live").transfer = TransferFunctions.TransferFunction2;
                                                                              }
                                                      if(matchPattern3(node)) {
                                                                                node. getProperty("live").init = TransferFunctions.TransferFunction3;
                                                                              }
                                                    }
                                                  }
  @Override public void performDataAnalysis(Graph g,Collection<Node> roots,Collection<Node> nodeset,Collection<Node> dirty, float intervalBoundary) {
                                                                                                                                                      Queue<Node> worklist = new LinkedBlockingQueue <>( );
                                                                                                                                                      HashSet<Node> inWorklist = new HashSet <>( );
                                                                                                                                                      for( Node node : nodeset) {
                                                                                                                                                                                  if(node.interval < intervalBoundary) continue;
                                                                                                                                                                                  worklist. add(node);
                                                                                                                                                                                  inWorklist. add(node);
                                                                                                                                                                                  initNodeValue(node);
                                                                                                                                                                                  initNodeTransferFunction(node);
                                                                                                                                                                                }
                                                                                                                                                      for( Node node : dirty) {
                                                                                                                                                                                if(node.interval < intervalBoundary) continue;
                                                                                                                                                                                worklist. add(node);
                                                                                                                                                                                inWorklist. add(node);
                                                                                                                                                                                initNodeTransferFunction(node);
                                                                                                                                                                              }
                                                                                                                                                      for( Node root : roots) {
                                                                                                                                                                                if(root.interval < intervalBoundary) continue;
                                                                                                                                                                                root. getProperty("live").lattice = root. getProperty("live").init. eval(root);
                                                                                                                                                                                this.changedNodes. add(root);
                                                                                                                                                                              }
                                                                                                                                                      for( Node node : nodeset) {
                                                                                                                                                                                  if(node.interval < intervalBoundary) continue;
                                                                                                                                                                                  {
                                                                                                                                                                                    FlockLattice init = node. getProperty("live").lattice;
                                                                                                                                                                                    for( Node pred : g. childrenOf(node)) {
                                                                                                                                                                                                                            FlockLattice live_o = pred. getProperty("live").transfer. eval(pred);
                                                                                                                                                                                                                            init = init. lub(live_o);
                                                                                                                                                                                                                          }
                                                                                                                                                                                    node. getProperty("live").lattice = init;
                                                                                                                                                                                    this.changedNodes. add(node);
                                                                                                                                                                                  }
                                                                                                                                                                                }
                                                                                                                                                      while(!worklist. isEmpty( )) {
                                                                                                                                                                                     Node node = worklist. poll( );
                                                                                                                                                                                     inWorklist. remove(node);
                                                                                                                                                                                     if(node.interval < intervalBoundary) continue;
                                                                                                                                                                                     FlockLattice live_n = node. getProperty("live").transfer. eval(node);
                                                                                                                                                                                     for( Node successor : g. childrenOf(node)) {
                                                                                                                                                                                                                                  if(successor.interval < intervalBoundary) continue;
                                                                                                                                                                                                                                  boolean changed = false;
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
                                                                                                                                                                                                                                 if(successor.interval < intervalBoundary) continue;
                                                                                                                                                                                                                                 FlockLattice live_o = successor. getProperty("live").lattice;
                                                                                                                                                                                                                                 if(live_n. nleq(live_o)) {
                                                                                                                                                                                                                                                            successor. getProperty("live").lattice = live_o. lub(live_n);
                                                                                                                                                                                                                                                            changed = true;
                                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                 FlockLattice live_o = successor. getProperty("live").lattice;
                                                                                                                                                                                                                                 if(live_n. nleq(live_o)) {
                                                                                                                                                                                                                                                            successor. getProperty("live").lattice = live_o. lub(live_n);
                                                                                                                                                                                                                                                            changed = true;
                                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                 FlockLattice live_o = successor. getProperty("live").lattice;
                                                                                                                                                                                                                                 if(live_n. nleq(live_o)) {
                                                                                                                                                                                                                                                            successor. getProperty("live").lattice = live_o. lub(live_n);
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
                                                                                                                                                                                   }
                                                                                                                                                    }
}
class TransferFunctions {
  public static TransferFunction TransferFunction0 = new TransferFunction0 ( );
  public static TransferFunction TransferFunction1 = new TransferFunction1 ( );
  public static TransferFunction TransferFunction2 = new TransferFunction2 ( );
  public static TransferFunction TransferFunction3 = new TransferFunction3 ( );
}
class TransferFunction0 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Node next = node;
                                                  MaySet tmp45 = (MaySet ) UserFunctions. live_f(next);
                                                  return tmp45;
                                                }
}
class TransferFunction1 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Node next = node;
                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                  Set tmp47 = (Set ) SetUtils. create(TermUtils. asString(usrn). get( ));
                                                  Set tmp48 = (Set ) SetUtils. union(tmp47, ((FlockLattice ) UserFunctions. live_f(next)). value( ));
                                                  Set tmp44 = (Set ) tmp48;
                                                  return new MaySet (tmp44);
                                                }
}
class TransferFunction2 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Node next = node;
                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                  Set result0 = new HashSet ( );
                                                  for( Object usrm : (Set ) ((FlockLattice ) UserFunctions. live_f(next)). value( )) {
                                                                                                                                       if(!usrm. equals(usrn)) {
                                                                                                                                                                 result0. add(usrm);
                                                                                                                                                               }
                                                                                                                                     }
                                                  Set tmp46 = (Set ) result0;
                                                  Set tmp43 = (Set ) tmp46;
                                                  return new MaySet (tmp43);
                                                }
}
class TransferFunction3 extends TransferFunction {
  @Override public FlockLattice eval(Node node) {
                                                  IStrategoTerm term = node.term;
                                                  Set tmp41 = (Set ) SetUtils. create( );
                                                  return new MaySet (tmp41);
                                                }
}
class UserFunctions {
  public static FlockLattice live_f(Object o) {
                                                Node node = (Node ) o;
                                                return node. getProperty("live").lattice;
                                              }
}