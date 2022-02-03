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
    super("busyExpressions", Direction.BACKWARD);
  }
  @Override public void initNodeValue(Node node) {
                                                   node. addProperty("busyExpressions", SimpleMap. bottom( ));
                                                 }
  @Override public void initNodeTransferFunction(Node node) {
                                                              {
                                                                if(matchPattern0(node)) {
                                                                                          node. getProperty("busyExpressions").transfer = TransferFunctions.TransferFunction0;
                                                                                        }
                                                                if(matchPattern1(node)) {
                                                                                          node. getProperty("busyExpressions").transfer = TransferFunctions.TransferFunction1;
                                                                                        }
                                                                if(matchPattern2(node)) {
                                                                                          node. getProperty("busyExpressions").init = TransferFunctions.TransferFunction2;
                                                                                        }
                                                              }
                                                            }
  private boolean matchPattern0(Node node) {
                                             ITerm term = node.virtualTerm;
                                             return true;
                                           }
  private boolean matchPattern1(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("VarDec") && term. childrenCount( ) == 3)) {
                                                                                                                                                                return false;
                                                                                                                                                              }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             ITerm term_2 = term. childAt(2);
                                             return true;
                                           }
  private boolean matchPattern2(Node node) {
                                             ITerm term = node.virtualTerm;
                                             return true;
                                           }
}
class TransferFunctions {
  public static TransferFunction TransferFunction0 = new TransferFunction0 ( );
  public static TransferFunction TransferFunction1 = new TransferFunction1 ( );
  public static TransferFunction TransferFunction2 = new TransferFunction2 ( );
}
class TransferFunction0 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node next = node;
                                                                                                                                                  SimpleMap tmp93 = (SimpleMap ) UserFunctions. busyExpressions_f(next);
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp17);
                                                                                                                                                }
}
class TransferFunction1 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node next = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                                                                                                                  IStrategoTerm usre = Helpers. at(term, 2);
                                                                                                                                                  Set tmp94 = (Set ) SetUtils. create(usrn);
                                                                                                                                                  Stream<Map. Entry> result217 = ((Map ) ((FlockLattice ) UserFunctions. busyExpressions_f(next)). value( )). entrySet( ). stream( );
                                                                                                                                                  Stream tmp95 = (Stream<Map. Entry> ) result217;
                                                                                                                                                  Set tmp96 = (Set ) SetUtils. create(usrn);
                                                                                                                                                  Stream tmp97 = (Stream<Map. Entry> ) MapUtils. create(Helpers. toTerm(usre), new MustSet (tmp96)). entrySet( ). stream( );
                                                                                                                                                  Stream tmp98 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp95, tmp97));
                                                                                                                                                  Stream tmp92 = (Stream<Map. Entry> ) tmp98;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp17);
                                                                                                                                                }
}
class TransferFunction2 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Stream tmp91 = (Stream<Map. Entry> ) MapUtils. create( ). entrySet( ). stream( );
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp17);
                                                                                                                                                }
}
class UserFunctions {
  public static FlockLattice busyExpressions_f(Object o) {
                                                           Node node = (Node ) o;
                                                           return node. getProperty("busyExpressions").lattice;
                                                         }
}