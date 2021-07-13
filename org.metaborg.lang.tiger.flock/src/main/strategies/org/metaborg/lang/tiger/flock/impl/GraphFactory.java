package org.metaborg.lang.tiger.flock.impl;

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
import org.metaborg.lang.tiger.flock.common.*;
import org.metaborg.lang.tiger.flock.common.Graph.Node;

public class GraphFactory {
	public static Graph createCfg(IStrategoTerm term) {
		Graph result_graph = new Graph();
		for (IStrategoTerm subterm : term.getSubterms()) {
			Graph subgraph = createCfg(subterm);
			if (subgraph.size() != 0) {
				result_graph.mergeGraph(subgraph);
			}
		}
		if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Mod") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Mod_t = term;
			IStrategoTerm s_t = Helpers.at(term, 0);
			Graph s_nr = createCfg_inner(s_t);
			result_graph.mergeGraph(s_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(s_nr.leaves);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec_t = term;
			IStrategoTerm n_t = Helpers.at(term, 0);
			IStrategoTerm args_t = Helpers.at(term, 1);
			IStrategoTerm body_t = Helpers.at(term, 2);
			Graph body_nr = createCfg_inner(body_t);
			result_graph.mergeGraph(body_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(body_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec_t = term;
			IStrategoTerm n_t = Helpers.at(term, 0);
			IStrategoTerm args_t = Helpers.at(term, 1);
			IStrategoTerm rt_t = Helpers.at(term, 2);
			IStrategoTerm body_t = Helpers.at(term, 3);
			Graph body_nr = createCfg_inner(body_t);
			result_graph.mergeGraph(body_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(body_nr.leaves);
		} else {
		}
		return result_graph;
	}

	private static Graph createCfg_inner(IStrategoTerm term) {
		Graph result_graph = new Graph();
		if (TermUtils.isList(term)) {
			IStrategoList list = M.list(term);
			if (list.isEmpty()) {
				return new Graph();
			}
			result_graph.mergeGraph(createCfg_inner(list.head()));
			list = list.tail();
			while (!list.isEmpty()) {
				Graph new_result = createCfg_inner(list.head());
				result_graph.attachChildGraph(result_graph.leaves, new_result);
				result_graph.leaves = new_result.leaves;
				list = list.tail();
			}
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("FunDecs") && term.getSubtermCount() == 1)) {
			IStrategoTerm _FunDecs_t = term;
			result_graph.leaves = new HashSet<>();
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _VarDec_t = term;
			IStrategoTerm n_t = Helpers.at(term, 0);
			IStrategoTerm t_t = Helpers.at(term, 1);
			IStrategoTerm e_t = Helpers.at(term, 2);
			Graph e_nr = createCfg_inner(e_t);
			Graph _VarDec_nb = new Graph(getTermNode(_VarDec_t), _VarDec_t);
			result_graph.mergeGraph(e_nr);
			result_graph.attachChildGraph(e_nr.leaves, _VarDec_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_VarDec_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Let") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Let_t = term;
			IStrategoTerm decs_t = Helpers.at(term, 0);
			IStrategoTerm exps_t = Helpers.at(term, 1);
			Graph decs_nr = createCfg_inner(decs_t);
			Graph exps_nr = createCfg_inner(exps_t);
			result_graph.mergeGraph(decs_nr);
			result_graph.attachChildGraph(decs_nr.leaves, exps_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(exps_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Call") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Call_t = term;
			IStrategoTerm args_t = Helpers.at(term, 1);
			Graph args_nr = createCfg_inner(args_t);
			Graph _Call_nb = new Graph(getTermNode(_Call_t), _Call_t);
			result_graph.mergeGraph(args_nr);
			result_graph.attachChildGraph(args_nr.leaves, _Call_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Call_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Minus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Minus_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Minus_nb = new Graph(getTermNode(_Minus_t), _Minus_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Minus_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Minus_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Plus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Plus_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Plus_nb = new Graph(getTermNode(_Plus_t), _Plus_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Plus_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Plus_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Lt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Lt_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Lt_nb = new Graph(getTermNode(_Lt_t), _Lt_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Lt_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Lt_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Eq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Eq_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Eq_nb = new Graph(getTermNode(_Eq_t), _Eq_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Eq_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Eq_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Times") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Times_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Times_nb = new Graph(getTermNode(_Times_t), _Times_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Times_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Times_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Divide") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Divide_t = term;
			IStrategoTerm lhs_t = Helpers.at(term, 0);
			IStrategoTerm rhs_t = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs_t);
			Graph rhs_nr = createCfg_inner(rhs_t);
			Graph _Divide_nb = new Graph(getTermNode(_Divide_t), _Divide_t);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Divide_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Divide_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("If") && term.getSubtermCount() == 3)) {
			IStrategoTerm _If_t = term;
			IStrategoTerm c_t = Helpers.at(term, 0);
			IStrategoTerm t_t = Helpers.at(term, 1);
			IStrategoTerm e_t = Helpers.at(term, 2);
			Graph c_nr = createCfg_inner(c_t);
			Graph t_nr = createCfg_inner(t_t);
			Graph e_nr = createCfg_inner(e_t);
			result_graph.mergeGraph(c_nr);
			result_graph.attachChildGraph(c_nr.leaves, t_nr);
			result_graph.attachChildGraph(c_nr.leaves, e_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(t_nr.leaves);
			result_graph.leaves.addAll(e_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Seq") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Seq_t = term;
			IStrategoTerm stmts_t = Helpers.at(term, 0);
			Graph stmts_nr = createCfg_inner(stmts_t);
			result_graph.mergeGraph(stmts_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(stmts_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Var") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Var_t = term;
			Graph _Var_nb = new Graph(getTermNode(_Var_t), _Var_t);
			result_graph.mergeGraph(_Var_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Var_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Int") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Int_t = term;
			Graph _Int_nb = new Graph(getTermNode(_Int_t), _Int_t);
			result_graph.mergeGraph(_Int_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Int_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("String") && term.getSubtermCount() == 1)) {
			IStrategoTerm _String_t = term;
			Graph _String_nb = new Graph(getTermNode(_String_t), _String_t);
			result_graph.mergeGraph(_String_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_String_nb.leaves);
		} else {
			throw new RuntimeException("Could not create CFG node for term '" + term + "'.");
		}
		return result_graph;
	}

	public static Node getTermNode(IStrategoTerm n) {
		if (n.getAnnotations().size() == 0)
			return null;
		assert TermUtils.isAppl(n.getAnnotations().getSubterm(0), "FlockNodeId", 1);
		IStrategoInt id = (IStrategoInt) n.getAnnotations().getSubterm(0).getSubterm(0);
		return new Node(new CfgNodeId(id.intValue()));
	}
}