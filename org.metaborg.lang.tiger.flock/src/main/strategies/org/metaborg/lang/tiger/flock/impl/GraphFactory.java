package org.metaborg.lang.tiger.flock.impl;

import java.util.HashSet;

import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class GraphFactory {
	public static Graph createCfgRecursive(IStrategoTerm term) {
		Graph result_graph = new Graph();
		for (IStrategoTerm subterm : term.getSubterms()) {
			Graph subgraph = createCfgRecursive(subterm);
			if (subgraph.size() != 0) {
				result_graph.mergeGraph(subgraph);
			}
		}
		if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Mod") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Mod = term;
			IStrategoTerm s = Helpers.at(term, 0);
			Graph s_nr = createCfg_inner(s);
			result_graph.mergeGraph(s_nr);
			result_graph.leaves.addAll(s_nr.leaves);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm body = Helpers.at(term, 2);
			Graph body_nr = createCfg_inner(body);
			result_graph.mergeGraph(body_nr);
			result_graph.leaves.addAll(body_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm rt = Helpers.at(term, 2);
			IStrategoTerm body = Helpers.at(term, 3);
			Graph body_nr = createCfg_inner(body);
			result_graph.mergeGraph(body_nr);
			result_graph.leaves.addAll(body_nr.leaves);
		} else {
		}
		return result_graph;
	}

	public static Graph createCfgOnce(IStrategoTerm term) {
		return createCfg_inner(term);
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
			IStrategoTerm _FunDecs = term;
			result_graph.leaves = new HashSet<>();
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDecs") && term.getSubtermCount() == 1)) {
			IStrategoTerm _TypeDecs = term;
			IStrategoTerm typeDecs = Helpers.at(term, 0);
			result_graph.leaves = new HashSet<>();
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			IStrategoTerm occ = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			result_graph.leaves = new HashSet<>();
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _VarDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			Graph e_nr = createCfg_inner(e);
			Graph _VarDec_nb = new Graph(Helpers.getTermNode(_VarDec), _VarDec);
			result_graph.mergeGraph(e_nr);
			result_graph.attachChildGraph(e_nr.leaves, _VarDec_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_VarDec_nb.leaves);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("VarDecNoType") && term.getSubtermCount() == 2)) {
			IStrategoTerm _VarDecNoType = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm e = Helpers.at(term, 1);
			Graph e_nr = createCfg_inner(e);
			Graph _VarDecNoType_nb = new Graph(Helpers.getTermNode(_VarDecNoType), _VarDecNoType);
			result_graph.mergeGraph(e_nr);
			result_graph.attachChildGraph(e_nr.leaves, _VarDecNoType_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_VarDecNoType_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("UMinus") && term.getSubtermCount() == 1)) {
			IStrategoTerm _UMinus = term;
			IStrategoTerm exp = Helpers.at(term, 0);
			Graph exp_nr = createCfg_inner(exp);
			Graph _UMinus_nb = new Graph(Helpers.getTermNode(_UMinus), _UMinus);
			result_graph.mergeGraph(exp_nr);
			result_graph.attachChildGraph(exp_nr.leaves, _UMinus_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_UMinus_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Minus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Minus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Minus_nb = new Graph(Helpers.getTermNode(_Minus), _Minus);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Minus_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Minus_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Plus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Plus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Plus_nb = new Graph(Helpers.getTermNode(_Plus), _Plus);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Plus_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Plus_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Times") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Times = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Times_nb = new Graph(Helpers.getTermNode(_Times), _Times);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Times_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Times_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Divide") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Divide = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Divide_nb = new Graph(Helpers.getTermNode(_Divide), _Divide);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Divide_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Divide_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Lt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Lt = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Lt_nb = new Graph(Helpers.getTermNode(_Lt), _Lt);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Lt_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Lt_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Eq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Eq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Eq_nb = new Graph(Helpers.getTermNode(_Eq), _Eq);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Eq_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Eq_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Geq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Geq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Geq_nb = new Graph(Helpers.getTermNode(_Geq), _Geq);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Geq_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Geq_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Leq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Leq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Leq_nb = new Graph(Helpers.getTermNode(_Leq), _Leq);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Leq_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Leq_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Neq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Neq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Neq_nb = new Graph(Helpers.getTermNode(_Neq), _Neq);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Neq_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Neq_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("And") && term.getSubtermCount() == 2)) {
			IStrategoTerm _And = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _And_nb = new Graph(Helpers.getTermNode(_And), _And);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _And_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_And_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Or") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Or = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			Graph lhs_nr = createCfg_inner(lhs);
			Graph rhs_nr = createCfg_inner(rhs);
			Graph _Or_nb = new Graph(Helpers.getTermNode(_Or), _Or);
			result_graph.mergeGraph(lhs_nr);
			result_graph.attachChildGraph(lhs_nr.leaves, rhs_nr);
			result_graph.attachChildGraph(rhs_nr.leaves, _Or_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Or_nb.leaves);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("Subscript") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Subscript = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm idx = Helpers.at(term, 1);
			Graph idx_nr = createCfg_inner(idx);
			Graph lval_nr = createCfg_inner(lval);
			Graph _Subscript_nb = new Graph(Helpers.getTermNode(_Subscript), _Subscript);
			result_graph.mergeGraph(idx_nr);
			result_graph.attachChildGraph(idx_nr.leaves, lval_nr);
			result_graph.attachChildGraph(lval_nr.leaves, _Subscript_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Subscript_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Call") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Call = term;
			IStrategoTerm args = Helpers.at(term, 1);
			Graph args_nr = createCfg_inner(args);
			Graph _Call_nb = new Graph(Helpers.getTermNode(_Call), _Call);
			result_graph.mergeGraph(args_nr);
			result_graph.attachChildGraph(args_nr.leaves, _Call_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Call_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("If") && term.getSubtermCount() == 3)) {
			IStrategoTerm _If = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			Graph c_nr = createCfg_inner(c);
			Graph t_nr = createCfg_inner(t);
			Graph e_nr = createCfg_inner(e);
			result_graph.mergeGraph(c_nr);
			result_graph.attachChildGraph(c_nr.leaves, t_nr);
			result_graph.attachChildGraph(c_nr.leaves, e_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(t_nr.leaves);
			result_graph.leaves.addAll(e_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("IfThen") && term.getSubtermCount() == 2)) {
			IStrategoTerm _IfThen = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			Graph c_nr = createCfg_inner(c);
			Graph t_nr = createCfg_inner(t);
			result_graph.mergeGraph(c_nr);
			result_graph.attachChildGraph(c_nr.leaves, t_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(t_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Assign") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Assign = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm expr = Helpers.at(term, 1);
			Graph expr_nr = createCfg_inner(expr);
			Graph _Assign_nb = new Graph(Helpers.getTermNode(_Assign), _Assign);
			result_graph.mergeGraph(expr_nr);
			result_graph.attachChildGraph(expr_nr.leaves, _Assign_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Assign_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Seq") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Seq = term;
			IStrategoTerm stmts = Helpers.at(term, 0);
			Graph stmts_nr = createCfg_inner(stmts);
			result_graph.mergeGraph(stmts_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(stmts_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("For") && term.getSubtermCount() == 2)) {
			IStrategoTerm _For = term;
			IStrategoTerm binding = Helpers.at(term, 0);
			IStrategoTerm body = Helpers.at(term, 1);
			Graph binding_nr = createCfg_inner(binding);
			Graph body_nr = createCfg_inner(body);
			result_graph.mergeGraph(binding_nr);
			result_graph.attachChildGraph(binding_nr.leaves, body_nr);
			result_graph.attachChildGraph(body_nr.leaves, binding_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(body_nr.leaves);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("LoopBinding") && term.getSubtermCount() == 3)) {
			IStrategoTerm _LoopBinding = term;
			IStrategoTerm var = Helpers.at(term, 0);
			IStrategoTerm from = Helpers.at(term, 1);
			IStrategoTerm to = Helpers.at(term, 2);
			Graph _LoopBinding_nb = new Graph(Helpers.getTermNode(_LoopBinding), _LoopBinding);
			result_graph.mergeGraph(_LoopBinding_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_LoopBinding_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Let") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Let = term;
			IStrategoTerm decs = Helpers.at(term, 0);
			IStrategoTerm exps = Helpers.at(term, 1);
			Graph decs_nr = createCfg_inner(decs);
			Graph exps_nr = createCfg_inner(exps);
			result_graph.mergeGraph(decs_nr);
			result_graph.attachChildGraph(decs_nr.leaves, exps_nr);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(exps_nr.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Array") && term.getSubtermCount() == 3)) {
			IStrategoTerm _Array = term;
			IStrategoTerm len = Helpers.at(term, 1);
			IStrategoTerm init = Helpers.at(term, 2);
			Graph len_nr = createCfg_inner(len);
			Graph init_nr = createCfg_inner(init);
			Graph _Array_nb = new Graph(Helpers.getTermNode(_Array), _Array);
			result_graph.mergeGraph(len_nr);
			result_graph.attachChildGraph(len_nr.leaves, init_nr);
			result_graph.attachChildGraph(init_nr.leaves, _Array_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Array_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Var") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Var = term;
			Graph _Var_nb = new Graph(Helpers.getTermNode(_Var), _Var);
			result_graph.mergeGraph(_Var_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Var_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Int") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Int = term;
			Graph _Int_nb = new Graph(Helpers.getTermNode(_Int), _Int);
			result_graph.mergeGraph(_Int_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_Int_nb.leaves);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("String") && term.getSubtermCount() == 1)) {
			IStrategoTerm _String = term;
			Graph _String_nb = new Graph(Helpers.getTermNode(_String), _String);
			result_graph.mergeGraph(_String_nb);
			result_graph.leaves = new HashSet<>();
			result_graph.leaves.addAll(_String_nb.leaves);
		} else {
			throw new RuntimeException("Could not create CFG node for term '" + term + "'.");
		}
		return result_graph;
	}
}