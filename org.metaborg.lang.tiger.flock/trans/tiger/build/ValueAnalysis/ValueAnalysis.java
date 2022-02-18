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
import flock.subject.common.GraphBuilder;
import flock.subject.common.TermTree;
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
    super("values", Direction.FORWARD);
  }
  @Override public void initNodeValue(Node node) {
                                                   node. addProperty("values", SimpleMap. bottom( ));
                                                 }
  @Override public void initNodeTransferFunction(Node node) {
                                                              {
                                                                if(matchPattern0(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction0;
                                                                                        }
                                                                if(matchPattern1(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction1;
                                                                                        }
                                                                if(matchPattern2(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction2;
                                                                                        }
                                                                if(matchPattern3(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction3;
                                                                                        }
                                                                if(matchPattern4(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction4;
                                                                                        }
                                                                if(matchPattern5(node)) {
                                                                                          node. getProperty("values").transfer = TransferFunctions.TransferFunction5;
                                                                                        }
                                                                if(matchPattern6(node)) {
                                                                                          node. getProperty("values").init = TransferFunctions.TransferFunction6;
                                                                                        }
                                                              }
                                                            }
  private boolean matchPattern0(Node node) {
                                             ITerm term = node.virtualTerm;
                                             return true;
                                           }
  private boolean matchPattern1(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("LoopBinding") && term. childrenCount( ) == 3)) {
                                                                                                                                                                     return false;
                                                                                                                                                                   }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             ITerm term_2 = term. childAt(2);
                                             if(!(term_0. isAppl( ) && ((ApplTerm ) term_0). getConstructor( ). equals("Var") && term_0. childrenCount( ) == 1)) {
                                                                                                                                                                   return false;
                                                                                                                                                                 }
                                             ITerm term_0_0 = term_0. childAt(0);
                                             return true;
                                           }
  private boolean matchPattern2(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("Assign") && term. childrenCount( ) == 2)) {
                                                                                                                                                                return false;
                                                                                                                                                              }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             if(!(term_0. isAppl( ) && ((ApplTerm ) term_0). getConstructor( ). equals("LValue") && term_0. childrenCount( ) == 1)) {
                                                                                                                                                                      return false;
                                                                                                                                                                    }
                                             ITerm term_0_0 = term_0. childAt(0);
                                             if(!(term_0_0. isAppl( ) && ((ApplTerm ) term_0_0). getConstructor( ). equals("Var") && term_0_0. childrenCount( ) == 1)) {
                                                                                                                                                                         return false;
                                                                                                                                                                       }
                                             ITerm term_0_0_0 = term_0_0. childAt(0);
                                             return true;
                                           }
  private boolean matchPattern3(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("Assign") && term. childrenCount( ) == 2)) {
                                                                                                                                                                return false;
                                                                                                                                                              }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             if(!(term_0. isAppl( ) && ((ApplTerm ) term_0). getConstructor( ). equals("LValue") && term_0. childrenCount( ) == 1)) {
                                                                                                                                                                      return false;
                                                                                                                                                                    }
                                             ITerm term_0_0 = term_0. childAt(0);
                                             if(!(term_0_0. isAppl( ) && ((ApplTerm ) term_0_0). getConstructor( ). equals("Var") && term_0_0. childrenCount( ) == 1)) {
                                                                                                                                                                         return false;
                                                                                                                                                                       }
                                             ITerm term_0_0_0 = term_0_0. childAt(0);
                                             if(!(term_1. isAppl( ) && ((ApplTerm ) term_1). getConstructor( ). equals("Int") && term_1. childrenCount( ) == 1)) {
                                                                                                                                                                   return false;
                                                                                                                                                                 }
                                             ITerm term_1_0 = term_1. childAt(0);
                                             return true;
                                           }
  private boolean matchPattern4(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("VarDec") && term. childrenCount( ) == 3)) {
                                                                                                                                                                return false;
                                                                                                                                                              }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             ITerm term_2 = term. childAt(2);
                                             return true;
                                           }
  private boolean matchPattern5(Node node) {
                                             ITerm term = node.virtualTerm;
                                             if(!(term. isAppl( ) && ((ApplTerm ) term). getConstructor( ). equals("VarDec") && term. childrenCount( ) == 3)) {
                                                                                                                                                                return false;
                                                                                                                                                              }
                                             ITerm term_0 = term. childAt(0);
                                             ITerm term_1 = term. childAt(1);
                                             ITerm term_2 = term. childAt(2);
                                             if(!(term_2. isAppl( ) && ((ApplTerm ) term_2). getConstructor( ). equals("Int") && term_2. childrenCount( ) == 1)) {
                                                                                                                                                                   return false;
                                                                                                                                                                 }
                                             ITerm term_2_0 = term_2. childAt(0);
                                             return true;
                                           }
  private boolean matchPattern6(Node node) {
                                             ITerm term = node.virtualTerm;
                                             return true;
                                           }
}
class Value implements FlockValueLattice {
  ConstProp value;
  public Value(Value o) {
    this.value = o.value;
  }
  public Value(ConstProp v) {
    this.value = v;
  }
  @Override public FlockValue value( ) {
                                         return this.value;
                                       }
  @Override public boolean equals(Object other) {
                                                  if(other == null) return false;
                                                  if(other == this) return true;
                                                  if(other. getClass( ) != this. getClass( )) return false;
                                                  Value
                                                  rhs = (Value ) other;
                                                  return this.value. equals(rhs.value);
                                                }
  @Override public String toString( ) {
                                        return value. toString( );
                                      }
  public static Value bottom( ) {
                                  ITermFactory factory = Flock.instance.factory;
                                  ConstProp tmp90 = (ConstProp ) new ConstProp (factory. makeAppl(factory. makeConstructor("Bottom", 0), new IStrategoTerm[]{ }, null));
                                  return new Value (tmp90);
                                }
  public static Value top( ) {
                               ITermFactory factory = Flock.instance.factory;
                               ConstProp tmp91 = (ConstProp ) new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null));
                               return new Value (tmp91);
                             }
  @Override public boolean lub(Object other) {
                                               ITermFactory factory = Flock.instance.factory;
                                               ConstProp usrl = ((Value ) this).value;
                                               ConstProp usrr = ((Value ) other).value;
                                               IStrategoTerm term4 = Helpers. toTerm(factory. makeTuple(new IStrategoTerm[]{Helpers. toTerm(usrl),Helpers. toTerm(usrr)}, null));
                                               Object result10 = null;
                                               if(TermUtils. isTuple(term4) && (TermUtils. isAppl(Helpers. at(term4, 0)) && (M. appl(Helpers. at(term4, 0)). getName( ). equals("Top") && Helpers. at(term4, 0). getSubtermCount( ) == 0))) {
                                                                                                                                                                                                                                              result10 = new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null));
                                                                                                                                                                                                                                            }
                                               if(TermUtils. isTuple(term4) && (TermUtils. isAppl(Helpers. at(term4, 1)) && (M. appl(Helpers. at(term4, 1)). getName( ). equals("Top") && Helpers. at(term4, 1). getSubtermCount( ) == 0))) {
                                                                                                                                                                                                                                              result10 = new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null));
                                                                                                                                                                                                                                            }
                                               if(TermUtils. isTuple(term4) && (TermUtils. isAppl(Helpers. at(term4, 0)) && (M. appl(Helpers. at(term4, 0)). getName( ). equals("Const") && (Helpers. at(term4, 0). getSubtermCount( ) == 1 && (TermUtils. isAppl(Helpers. at(term4, 1)) && (M. appl(Helpers. at(term4, 1)). getName( ). equals("Const") && Helpers. at(term4, 1). getSubtermCount( ) == 1)))))) {
                                                                                                                                                                                                                                                                                                                                                                                                                   IStrategoTerm usri = Helpers. at(Helpers. at(term4, 0), 0);
                                                                                                                                                                                                                                                                                                                                                                                                                   IStrategoTerm usrj = Helpers. at(Helpers. at(term4, 1), 0);
                                                                                                                                                                                                                                                                                                                                                                                                                   result10 = (boolean) usri. equals(usrj) ? new ConstProp (factory. makeAppl(factory. makeConstructor("Const", 1), new IStrategoTerm[]{Helpers. toTerm(usri)}, null)) : new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null));
                                                                                                                                                                                                                                                                                                                                                                                                                 }
                                               if(TermUtils. isTuple(term4) && (TermUtils. isAppl(Helpers. at(term4, 1)) && (M. appl(Helpers. at(term4, 1)). getName( ). equals("Bottom") && Helpers. at(term4, 1). getSubtermCount( ) == 0))) {
                                                                                                                                                                                                                                                 result10 = usrl;
                                                                                                                                                                                                                                               }
                                               if(TermUtils. isTuple(term4) && (TermUtils. isAppl(Helpers. at(term4, 0)) && (M. appl(Helpers. at(term4, 0)). getName( ). equals("Bottom") && Helpers. at(term4, 0). getSubtermCount( ) == 0))) {
                                                                                                                                                                                                                                                 result10 = usrr;
                                                                                                                                                                                                                                               }
                                               if(result10 == null) {
                                                                      throw new RuntimeException ("Could not match term");
                                                                    }
                                               ConstProp tmp92 = (ConstProp ) result10;
                                               Value
                                               _res_lat = new Value (tmp92);
                                               boolean _is_changed = !this. equals(_res_lat);
                                               this.value = _res_lat.value;
                                               return _is_changed;
                                             }
  @Override public FlockLattice copy( ) {
                                          return new Value (this);
                                        }
  @Override public void setValue(Object value) {
                                                 this.value = (ConstProp ) value;
                                               }
}
class TransferFunctions {
  public static TransferFunction TransferFunction0 = new TransferFunction0 ( );
  public static TransferFunction TransferFunction1 = new TransferFunction1 ( );
  public static TransferFunction TransferFunction2 = new TransferFunction2 ( );
  public static TransferFunction TransferFunction3 = new TransferFunction3 ( );
  public static TransferFunction TransferFunction4 = new TransferFunction4 ( );
  public static TransferFunction TransferFunction5 = new TransferFunction5 ( );
  public static TransferFunction TransferFunction6 = new TransferFunction6 ( );
}
class TransferFunction0 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  SimpleMap tmp74 = (SimpleMap ) UserFunctions. values_f(prev);
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp74);
                                                                                                                                                }
}
class TransferFunction1 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(Helpers. at(term, 0), 0);
                                                                                                                                                  Stream<Map. Entry> result11 = ((Map ) ((FlockLattice ) UserFunctions. values_f(prev)). value( )). entrySet( ). stream( ). filter(o -> {
                                                                                                                                                                                                                                                                                          Entry entry = (Entry ) o;
                                                                                                                                                                                                                                                                                          Object usrk = entry. getKey( );
                                                                                                                                                                                                                                                                                          Object usrv = entry. getValue( );
                                                                                                                                                                                                                                                                                          return !usrk. equals(usrn);
                                                                                                                                                                                                                                                                                        });
                                                                                                                                                  Stream tmp87 = (Stream<Map. Entry> ) result11;
                                                                                                                                                  Stream tmp88 = (Stream<Map. Entry> ) MapUtils. create(usrn, new Value (new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null)))). entrySet( ). stream( );
                                                                                                                                                  Stream tmp89 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp87, tmp88));
                                                                                                                                                  Stream tmp73 = (Stream<Map. Entry> ) tmp89;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp73);
                                                                                                                                                }
}
class TransferFunction2 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(Helpers. at(Helpers. at(term, 0), 0), 0);
                                                                                                                                                  Stream<Map. Entry> result12 = ((Map ) ((FlockLattice ) UserFunctions. values_f(prev)). value( )). entrySet( ). stream( ). filter(o -> {
                                                                                                                                                                                                                                                                                          Entry entry = (Entry ) o;
                                                                                                                                                                                                                                                                                          Object usrk = entry. getKey( );
                                                                                                                                                                                                                                                                                          Object usrv = entry. getValue( );
                                                                                                                                                                                                                                                                                          return !usrk. equals(usrn);
                                                                                                                                                                                                                                                                                        });
                                                                                                                                                  Stream tmp84 = (Stream<Map. Entry> ) result12;
                                                                                                                                                  Stream tmp85 = (Stream<Map. Entry> ) MapUtils. create(usrn, new Value (new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null)))). entrySet( ). stream( );
                                                                                                                                                  Stream tmp86 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp84, tmp85));
                                                                                                                                                  Stream tmp72 = (Stream<Map. Entry> ) tmp86;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp72);
                                                                                                                                                }
}
class TransferFunction3 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(Helpers. at(Helpers. at(term, 0), 0), 0);
                                                                                                                                                  IStrategoTerm usri = Helpers. at(Helpers. at(term, 1), 0);
                                                                                                                                                  Stream<Map. Entry> result13 = ((Map ) ((FlockLattice ) UserFunctions. values_f(prev)). value( )). entrySet( ). stream( ). filter(o -> {
                                                                                                                                                                                                                                                                                          Entry entry = (Entry ) o;
                                                                                                                                                                                                                                                                                          Object usrk = entry. getKey( );
                                                                                                                                                                                                                                                                                          Object usrv = entry. getValue( );
                                                                                                                                                                                                                                                                                          return !usrk. equals(usrn);
                                                                                                                                                                                                                                                                                        });
                                                                                                                                                  Stream tmp81 = (Stream<Map. Entry> ) result13;
                                                                                                                                                  Stream tmp82 = (Stream<Map. Entry> ) MapUtils. create(usrn, new Value (new ConstProp (factory. makeAppl(factory. makeConstructor("Const", 1), new IStrategoTerm[]{Helpers. toTerm(usri)}, null)))). entrySet( ). stream( );
                                                                                                                                                  Stream tmp83 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp81, tmp82));
                                                                                                                                                  Stream tmp69 = (Stream<Map. Entry> ) tmp83;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp69);
                                                                                                                                                }
}
class TransferFunction4 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                                                                                                                  Stream<Map. Entry> result122 = ((Map ) ((FlockLattice ) UserFunctions. values_f(prev)). value( )). entrySet( ). stream( ). filter(o -> {
                                                                                                                                                                                                                                                                                           Entry entry = (Entry ) o;
                                                                                                                                                                                                                                                                                           Object usrk = entry. getKey( );
                                                                                                                                                                                                                                                                                           Object usrv = entry. getValue( );
                                                                                                                                                                                                                                                                                           return !usrk. equals(usrn);
                                                                                                                                                                                                                                                                                         });
                                                                                                                                                  Stream tmp78 = (Stream<Map. Entry> ) result122;
                                                                                                                                                  Stream tmp79 = (Stream<Map. Entry> ) MapUtils. create(usrn, new Value (new ConstProp (factory. makeAppl(factory. makeConstructor("Top", 0), new IStrategoTerm[]{ }, null)))). entrySet( ). stream( );
                                                                                                                                                  Stream tmp80 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp78, tmp79));
                                                                                                                                                  Stream tmp66 = (Stream<Map. Entry> ) tmp80;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp66);
                                                                                                                                                }
}
class TransferFunction5 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  Node prev = node;
                                                                                                                                                  IStrategoTerm usrn = Helpers. at(term, 0);
                                                                                                                                                  IStrategoTerm usri = Helpers. at(Helpers. at(term, 2), 0);
                                                                                                                                                  Stream<Map. Entry> result123 = ((Map ) ((FlockLattice ) UserFunctions. values_f(prev)). value( )). entrySet( ). stream( ). filter(o -> {
                                                                                                                                                                                                                                                                                           Entry entry = (Entry ) o;
                                                                                                                                                                                                                                                                                           Object usrk = entry. getKey( );
                                                                                                                                                                                                                                                                                           Object usrv = entry. getValue( );
                                                                                                                                                                                                                                                                                           return !usrk. equals(usrn);
                                                                                                                                                                                                                                                                                         });
                                                                                                                                                  Stream tmp75 = (Stream<Map. Entry> ) result123;
                                                                                                                                                  Stream tmp76 = (Stream<Map. Entry> ) MapUtils. create(usrn, new Value (new ConstProp (factory. makeAppl(factory. makeConstructor("Const", 1), new IStrategoTerm[]{Helpers. toTerm(usri)}, null)))). entrySet( ). stream( );
                                                                                                                                                  Stream tmp77 = (Stream<Map. Entry> ) MapUtils. create(MapUtils. union(tmp75, tmp76));
                                                                                                                                                  Stream tmp63 = (Stream<Map. Entry> ) tmp77;
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp63);
                                                                                                                                                }
}
class TransferFunction6 extends TransferFunction {
  @SuppressWarnings({"unchecked","rawtypes","unused"}) @Override public boolean eval(Analysis. Direction direction,FlockLattice res, Node node) {
                                                                                                                                                  ITermFactory factory = Flock.instance.factory;
                                                                                                                                                  IStrategoTerm term = node.virtualTerm. toTermWithoutAnnotations( );
                                                                                                                                                  SimpleMap tmp60 = (SimpleMap ) SimpleMap. bottom( );
                                                                                                                                                  return TransferFunction. assignEvalResult(direction, node, res, tmp60);
                                                                                                                                                }
}
class UserFunctions {
  public static FlockLattice values_f(Object o) {
                                                  Node node = (Node ) o;
                                                  return node. getProperty("values").lattice;
                                                }
}