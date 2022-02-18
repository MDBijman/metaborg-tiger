package org.metaborg.lang.tiger.flock.graph;

import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.GraphBuilder;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.TermId;
import org.metaborg.lang.tiger.flock.common.TermTree;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class GraphFactory {
	public static Graph createCfgRecursive(TermTree tree, IStrategoTerm term) {
		return createCfgRecursive_inner(term).build(tree);
	}

	public static Graph createCfgOnce(TermTree tree, IStrategoTerm term) {
		return createCfg_inner(term).build(tree);
	}

	public static GraphBuilder createCfgRecursive_inner(IStrategoTerm term) {
		GraphBuilder result_graph = GraphBuilder.empty();
		for (IStrategoTerm subterm : term.getSubterms()) {
			GraphBuilder subgraph = createCfgRecursive_inner(subterm);
			if (subgraph.hasRealNodes()) {
				result_graph.merge(subgraph);
			}
		}
		if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Mod") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Mod = term;
			IStrategoTerm s = Helpers.at(term, 0);
			GraphBuilder s_nr = createCfg_inner(s);
			result_graph.merge(s_nr);
			result_graph.connect(result_graph.START, s_nr.ENTRY);
			result_graph.connect(s_nr.EXIT, result_graph.END);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm body = Helpers.at(term, 2);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(body_nr);
			result_graph.connect(result_graph.START, body_nr.ENTRY);
			result_graph.connect(body_nr.EXIT, result_graph.END);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm rt = Helpers.at(term, 2);
			IStrategoTerm body = Helpers.at(term, 3);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(body_nr);
			result_graph.connect(result_graph.START, body_nr.ENTRY);
			result_graph.connect(body_nr.EXIT, result_graph.END);
		} else {
		}
		return result_graph;
	}

	private static GraphBuilder createCfg_inner(IStrategoTerm term) {
		GraphBuilder result_graph = GraphBuilder.empty();
		if (TermUtils.isList(term)) {
			IStrategoList list = M.list(term);
			if (list.isEmpty()) {
				return GraphBuilder.fallthrough();
			}
			TermId currentTail = result_graph.ENTRY;
			while (!list.isEmpty()) {
				GraphBuilder new_result = createCfg_inner(list.head());
				result_graph.merge(new_result);
				result_graph.connect(currentTail, new_result.ENTRY);
				currentTail = new_result.EXIT;
				list = list.tail();
			}
			result_graph.connect(currentTail, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			result_graph.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			result_graph.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			result_graph.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			IStrategoTerm occ = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			result_graph.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _VarDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			GraphBuilder e_nr = createCfg_inner(e);
			GraphBuilder _VarDec_nb = GraphBuilder.fromSingle(Helpers.getTermId(_VarDec));
			result_graph.merge(e_nr);
			result_graph.merge(_VarDec_nb);
			result_graph.connect(result_graph.ENTRY, e_nr.ENTRY);
			result_graph.connect(e_nr.EXIT, _VarDec_nb.ENTRY);
			result_graph.connect(_VarDec_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("VarDecNoType") && term.getSubtermCount() == 2)) {
			IStrategoTerm _VarDecNoType = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm e = Helpers.at(term, 1);
			GraphBuilder e_nr = createCfg_inner(e);
			GraphBuilder _VarDecNoType_nb = GraphBuilder.fromSingle(Helpers.getTermId(_VarDecNoType));
			result_graph.merge(e_nr);
			result_graph.merge(_VarDecNoType_nb);
			result_graph.connect(result_graph.ENTRY, e_nr.ENTRY);
			result_graph.connect(e_nr.EXIT, _VarDecNoType_nb.ENTRY);
			result_graph.connect(_VarDecNoType_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("UMinus") && term.getSubtermCount() == 1)) {
			IStrategoTerm _UMinus = term;
			IStrategoTerm exp = Helpers.at(term, 0);
			GraphBuilder exp_nr = createCfg_inner(exp);
			GraphBuilder _UMinus_nb = GraphBuilder.fromSingle(Helpers.getTermId(_UMinus));
			result_graph.merge(exp_nr);
			result_graph.merge(_UMinus_nb);
			result_graph.connect(result_graph.ENTRY, exp_nr.ENTRY);
			result_graph.connect(exp_nr.EXIT, _UMinus_nb.ENTRY);
			result_graph.connect(_UMinus_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Minus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Minus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Minus_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Minus));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Minus_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Minus_nb.ENTRY);
			result_graph.connect(_Minus_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Plus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Plus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Plus_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Plus));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Plus_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Plus_nb.ENTRY);
			result_graph.connect(_Plus_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Times") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Times = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Times_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Times));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Times_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Times_nb.ENTRY);
			result_graph.connect(_Times_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Divide") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Divide = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Divide_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Divide));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Divide_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Divide_nb.ENTRY);
			result_graph.connect(_Divide_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Lt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Lt = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Lt_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Lt));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Lt_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Lt_nb.ENTRY);
			result_graph.connect(_Lt_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Gt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Gt = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Gt_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Gt));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Gt_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Gt_nb.ENTRY);
			result_graph.connect(_Gt_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Eq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Eq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Eq_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Eq));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Eq_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Eq_nb.ENTRY);
			result_graph.connect(_Eq_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Geq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Geq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Geq_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Geq));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Geq_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Geq_nb.ENTRY);
			result_graph.connect(_Geq_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Leq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Leq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Leq_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Leq));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Leq_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Leq_nb.ENTRY);
			result_graph.connect(_Leq_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Neq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Neq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Neq_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Neq));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Neq_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Neq_nb.ENTRY);
			result_graph.connect(_Neq_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("And") && term.getSubtermCount() == 2)) {
			IStrategoTerm _And = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _And_nb = GraphBuilder.fromSingle(Helpers.getTermId(_And));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_And_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _And_nb.ENTRY);
			result_graph.connect(_And_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Or") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Or = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			GraphBuilder lhs_nr = createCfg_inner(lhs);
			GraphBuilder rhs_nr = createCfg_inner(rhs);
			GraphBuilder _Or_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Or));
			result_graph.merge(lhs_nr);
			result_graph.merge(rhs_nr);
			result_graph.merge(_Or_nb);
			result_graph.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			result_graph.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			result_graph.connect(rhs_nr.EXIT, _Or_nb.ENTRY);
			result_graph.connect(_Or_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("Subscript") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Subscript = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm idx = Helpers.at(term, 1);
			GraphBuilder idx_nr = createCfg_inner(idx);
			GraphBuilder lval_nr = createCfg_inner(lval);
			GraphBuilder _Subscript_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Subscript));
			result_graph.merge(idx_nr);
			result_graph.merge(lval_nr);
			result_graph.merge(_Subscript_nb);
			result_graph.connect(result_graph.ENTRY, idx_nr.ENTRY);
			result_graph.connect(idx_nr.EXIT, lval_nr.ENTRY);
			result_graph.connect(lval_nr.EXIT, _Subscript_nb.ENTRY);
			result_graph.connect(_Subscript_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Call") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Call = term;
			IStrategoTerm args = Helpers.at(term, 1);
			GraphBuilder args_nr = createCfg_inner(args);
			GraphBuilder _Call_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Call));
			result_graph.merge(args_nr);
			result_graph.merge(_Call_nb);
			result_graph.connect(result_graph.ENTRY, args_nr.ENTRY);
			result_graph.connect(args_nr.EXIT, _Call_nb.ENTRY);
			result_graph.connect(_Call_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("If") && term.getSubtermCount() == 3)) {
			IStrategoTerm _If = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			GraphBuilder c_nr = createCfg_inner(c);
			GraphBuilder t_nr = createCfg_inner(t);
			GraphBuilder e_nr = createCfg_inner(e);
			result_graph.merge(c_nr);
			result_graph.merge(t_nr);
			result_graph.merge(e_nr);
			result_graph.connect(result_graph.ENTRY, c_nr.ENTRY);
			result_graph.connect(c_nr.EXIT, t_nr.ENTRY);
			result_graph.connect(t_nr.EXIT, result_graph.EXIT);
			result_graph.connect(c_nr.EXIT, e_nr.ENTRY);
			result_graph.connect(e_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("LValue") && term.getSubtermCount() == 1)) {
			IStrategoTerm _LValue = term;
			IStrategoTerm inner = Helpers.at(term, 0);
			GraphBuilder inner_nr = createCfg_inner(inner);
			result_graph.merge(inner_nr);
			result_graph.connect(result_graph.ENTRY, inner_nr.ENTRY);
			result_graph.connect(inner_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("IfThen") && term.getSubtermCount() == 2)) {
			IStrategoTerm _IfThen = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			GraphBuilder c_nr = createCfg_inner(c);
			GraphBuilder t_nr = createCfg_inner(t);
			result_graph.merge(c_nr);
			result_graph.merge(t_nr);
			result_graph.connect(result_graph.ENTRY, c_nr.ENTRY);
			result_graph.connect(c_nr.EXIT, t_nr.ENTRY);
			result_graph.connect(t_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Assign") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Assign = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm expr = Helpers.at(term, 1);
			GraphBuilder expr_nr = createCfg_inner(expr);
			GraphBuilder lval_nr = createCfg_inner(lval);
			GraphBuilder _Assign_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Assign));
			result_graph.merge(expr_nr);
			result_graph.merge(lval_nr);
			result_graph.merge(_Assign_nb);
			result_graph.connect(result_graph.ENTRY, expr_nr.ENTRY);
			result_graph.connect(expr_nr.EXIT, lval_nr.ENTRY);
			result_graph.connect(lval_nr.EXIT, _Assign_nb.ENTRY);
			result_graph.connect(_Assign_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Seq") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Seq = term;
			IStrategoTerm stmts = Helpers.at(term, 0);
			GraphBuilder stmts_nr = createCfg_inner(stmts);
			result_graph.merge(stmts_nr);
			result_graph.connect(result_graph.ENTRY, stmts_nr.ENTRY);
			result_graph.connect(stmts_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("For")
				&& (term.getSubtermCount() == 2 && (TermUtils.isAppl(Helpers.at(term, 0))
						&& (M.appl(Helpers.at(term, 0)).getName().equals("LoopBinding")
								&& Helpers.at(term, 0).getSubtermCount() == 3))))) {
			IStrategoTerm _For = term;
			IStrategoTerm binding = Helpers.at(term, 0);
			IStrategoTerm var = Helpers.at(Helpers.at(term, 0), 0);
			IStrategoTerm from = Helpers.at(Helpers.at(term, 0), 1);
			IStrategoTerm to = Helpers.at(Helpers.at(term, 0), 2);
			IStrategoTerm body = Helpers.at(term, 1);
			GraphBuilder from_nr = createCfg_inner(from);
			GraphBuilder to_nr = createCfg_inner(to);
			GraphBuilder binding_nr = createCfg_inner(binding);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(from_nr);
			result_graph.merge(to_nr);
			result_graph.merge(binding_nr);
			result_graph.merge(body_nr);
			result_graph.connect(result_graph.ENTRY, from_nr.ENTRY);
			result_graph.connect(from_nr.EXIT, to_nr.ENTRY);
			result_graph.connect(to_nr.EXIT, binding_nr.ENTRY);
			result_graph.connect(binding_nr.EXIT, body_nr.ENTRY);
			result_graph.connect(body_nr.EXIT, binding_nr.ENTRY);
			result_graph.connect(binding_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("LoopBinding") && term.getSubtermCount() == 3)) {
			IStrategoTerm _LoopBinding = term;
			IStrategoTerm var = Helpers.at(term, 0);
			IStrategoTerm from = Helpers.at(term, 1);
			IStrategoTerm to = Helpers.at(term, 2);
			GraphBuilder _LoopBinding_nb = GraphBuilder.fromSingle(Helpers.getTermId(_LoopBinding));
			result_graph.merge(_LoopBinding_nb);
			result_graph.connect(result_graph.ENTRY, _LoopBinding_nb.ENTRY);
			result_graph.connect(_LoopBinding_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Let") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Let = term;
			IStrategoTerm decs = Helpers.at(term, 0);
			IStrategoTerm exps = Helpers.at(term, 1);
			GraphBuilder decs_nr = createCfg_inner(decs);
			GraphBuilder exps_nr = createCfg_inner(exps);
			result_graph.merge(decs_nr);
			result_graph.merge(exps_nr);
			result_graph.connect(result_graph.ENTRY, decs_nr.ENTRY);
			result_graph.connect(decs_nr.EXIT, exps_nr.ENTRY);
			result_graph.connect(exps_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Array") && term.getSubtermCount() == 3)) {
			IStrategoTerm _Array = term;
			IStrategoTerm len = Helpers.at(term, 1);
			IStrategoTerm init = Helpers.at(term, 2);
			GraphBuilder len_nr = createCfg_inner(len);
			GraphBuilder init_nr = createCfg_inner(init);
			GraphBuilder _Array_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Array));
			result_graph.merge(len_nr);
			result_graph.merge(init_nr);
			result_graph.merge(_Array_nb);
			result_graph.connect(result_graph.ENTRY, len_nr.ENTRY);
			result_graph.connect(len_nr.EXIT, init_nr.ENTRY);
			result_graph.connect(init_nr.EXIT, _Array_nb.ENTRY);
			result_graph.connect(_Array_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Hole") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Hole = term;
			result_graph.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Return") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Return = term;
			result_graph.connect(result_graph.ENTRY, result_graph.END);
			result_graph.markIrregular(Helpers.getTermId(term));
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Var") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Var = term;
			GraphBuilder _Var_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Var));
			result_graph.merge(_Var_nb);
			result_graph.connect(result_graph.ENTRY, _Var_nb.ENTRY);
			result_graph.connect(_Var_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Int") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Int = term;
			GraphBuilder _Int_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Int));
			result_graph.merge(_Int_nb);
			result_graph.connect(result_graph.ENTRY, _Int_nb.ENTRY);
			result_graph.connect(_Int_nb.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("String") && term.getSubtermCount() == 1)) {
			IStrategoTerm _String = term;
			GraphBuilder _String_nb = GraphBuilder.fromSingle(Helpers.getTermId(_String));
			result_graph.merge(_String_nb);
			result_graph.connect(result_graph.ENTRY, _String_nb.ENTRY);
			result_graph.connect(_String_nb.EXIT, result_graph.EXIT);
		} else {
			throw new RuntimeException("Could not create CFG node for term '" + term + "'.");
		}
		return result_graph;
	}
}