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
  @Override public void initNodeValue(Node node) {
                                                   node. addProperty("expressions", SimpleMap. bottom( ));
                                                 }
  @Override public void initNodeTransferFunction(Node node) {
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
                                                                                                                                                  Node prev = node;
                                                                                                                                                  SimpleMap tmp105 = (SimpleMap ) UserFunctions. expressions_f(prev);
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp105);
                                                                                                                                                }
}
class TransferFunction1 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                                                                                                                  IStrategoTerm usre = Helpers. at(term, 2);
                                                                                                                                                  Set tmp108 = (Set ) SetUtils. create(usrn);
                                                                                                                                                  Stream<Map. Entry> result12 = ((Map ) ((FlockLattice ) UserFunctions. expressions_f(prev)). value( )). entrySet( ). stream( );
                                                                                                                                                  Stream tmp111 = (Stream<Map. Entry> ) result12;
                                                                                                                                                  Set tmp112 = (Set ) SetUtils. create(usrn);
                                                                                                                                                  Stream tmp113 = (Stream<Map. Entry> ) MapUtils. create(Helpers. toTerm(usre), new MustSet (tmp112)). entrySet( ). stream( );
                                                                                                                                                  Stream tmp114 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp111, tmp113));
                                                                                                                                                  Stream tmp102 = (Stream<Map. Entry> ) tmp114;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp102);
                                                                                                                                                }
}
class TransferFunction2 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Stream tmp99 = (Stream<Map. Entry> ) MapUtils. create( ). entrySet( ). stream( );
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp99);
                                                                                                                                                }
}
class UserFunctions {
  public static FlockLattice expressions_f(Object o) {
                                                       Node node = (Node ) o;
                                                       return node. getProperty("expressions").lattice;
                                                     }
}