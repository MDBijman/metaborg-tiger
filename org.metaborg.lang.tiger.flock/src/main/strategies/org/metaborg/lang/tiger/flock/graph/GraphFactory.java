package org.metaborg.lang.tiger.flock.graph;

import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.GraphBuilder;
import org.metaborg.lang.tiger.flock.common.Helpers;
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
			if (subgraph.size() != 0) {
				result_graph.merge(subgraph);
			}
		}
		if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Mod") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Mod = term;
			IStrategoTerm s = Helpers.at(term, 0);
			GraphBuilder s_nr = createCfg_inner(s);
			result_graph.merge(s_nr);
			result_graph.fromStart(s_nr.getEntry());
			result_graph.toEnd(s_nr.getExit());
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm body = Helpers.at(term, 2);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(body_nr);
			result_graph.fromStart(body_nr.getEntry());
			result_graph.toEnd(body_nr.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm rt = Helpers.at(term, 2);
			IStrategoTerm body = Helpers.at(term, 3);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(body_nr);
			result_graph.fromStart(body_nr.getEntry());
			result_graph.toEnd(body_nr.getExit());
		} else {
		}
		return result_graph;
	}

	private static GraphBuilder createCfg_inner(IStrategoTerm term) {
		GraphBuilder result_graph = GraphBuilder.empty();
		if (TermUtils.isList(term)) {
			IStrategoList list = M.list(term);
			if (list.isEmpty()) {
				return GraphBuilder.placeholder();
			}
			GraphBuilder r = GraphBuilder.empty();
			while (r.size() == 0 && !list.isEmpty()) {
				r = createCfg_inner(list.head());
				list = list.tail();
			}
			result_graph.merge(r);
			result_graph.fromEntry(r.getEntry());
			result_graph.setExit(r.getExit());
			while (!list.isEmpty()) {
				GraphBuilder new_result = createCfg_inner(list.head());
				result_graph.merge(new_result);
				if (new_result.size() > 0) {
					result_graph.connect(result_graph.getExit(), new_result.getEntry());
					result_graph.setExit(new_result.getExit());
				}
				list = list.tail();
			}
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			IStrategoTerm occ = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _VarDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			GraphBuilder e_nr = createCfg_inner(e);
			GraphBuilder _VarDec_nb = GraphBuilder.fromSingle(Helpers.getTermId(_VarDec));
			result_graph.merge(e_nr);
			result_graph.merge(_VarDec_nb);
			result_graph.fromEntry(e_nr.getEntry());
			result_graph.connect(e_nr.getExit(), _VarDec_nb.getEntry());
			result_graph.toExit(_VarDec_nb.getExit());
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("VarDecNoType") && term.getSubtermCount() == 2)) {
			IStrategoTerm _VarDecNoType = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm e = Helpers.at(term, 1);
			GraphBuilder e_nr = createCfg_inner(e);
			GraphBuilder _VarDecNoType_nb = GraphBuilder.fromSingle(Helpers.getTermId(_VarDecNoType));
			result_graph.merge(e_nr);
			result_graph.merge(_VarDecNoType_nb);
			result_graph.fromEntry(e_nr.getEntry());
			result_graph.connect(e_nr.getExit(), _VarDecNoType_nb.getEntry());
			result_graph.toExit(_VarDecNoType_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("UMinus") && term.getSubtermCount() == 1)) {
			IStrategoTerm _UMinus = term;
			IStrategoTerm exp = Helpers.at(term, 0);
			GraphBuilder exp_nr = createCfg_inner(exp);
			GraphBuilder _UMinus_nb = GraphBuilder.fromSingle(Helpers.getTermId(_UMinus));
			result_graph.merge(exp_nr);
			result_graph.merge(_UMinus_nb);
			result_graph.fromEntry(exp_nr.getEntry());
			result_graph.connect(exp_nr.getExit(), _UMinus_nb.getEntry());
			result_graph.toExit(_UMinus_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Minus_nb.getEntry());
			result_graph.toExit(_Minus_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Plus_nb.getEntry());
			result_graph.toExit(_Plus_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Times_nb.getEntry());
			result_graph.toExit(_Times_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Divide_nb.getEntry());
			result_graph.toExit(_Divide_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Lt_nb.getEntry());
			result_graph.toExit(_Lt_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Gt_nb.getEntry());
			result_graph.toExit(_Gt_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Eq_nb.getEntry());
			result_graph.toExit(_Eq_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Geq_nb.getEntry());
			result_graph.toExit(_Geq_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Leq_nb.getEntry());
			result_graph.toExit(_Leq_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Neq_nb.getEntry());
			result_graph.toExit(_Neq_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _And_nb.getEntry());
			result_graph.toExit(_And_nb.getExit());
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
			result_graph.fromEntry(lhs_nr.getEntry());
			result_graph.connect(lhs_nr.getExit(), rhs_nr.getEntry());
			result_graph.connect(rhs_nr.getExit(), _Or_nb.getEntry());
			result_graph.toExit(_Or_nb.getExit());
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
			result_graph.fromEntry(idx_nr.getEntry());
			result_graph.connect(idx_nr.getExit(), lval_nr.getEntry());
			result_graph.connect(lval_nr.getExit(), _Subscript_nb.getEntry());
			result_graph.toExit(_Subscript_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Call") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Call = term;
			IStrategoTerm args = Helpers.at(term, 1);
			GraphBuilder args_nr = createCfg_inner(args);
			GraphBuilder _Call_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Call));
			result_graph.merge(args_nr);
			result_graph.merge(_Call_nb);
			result_graph.fromEntry(args_nr.getEntry());
			result_graph.connect(args_nr.getExit(), _Call_nb.getEntry());
			result_graph.toExit(_Call_nb.getExit());
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
			result_graph.fromEntry(c_nr.getEntry());
			result_graph.connect(c_nr.getExit(), t_nr.getEntry());
			result_graph.toExit(t_nr.getExit());
			result_graph.connect(c_nr.getExit(), e_nr.getEntry());
			result_graph.toExit(e_nr.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("LValue") && term.getSubtermCount() == 1)) {
			IStrategoTerm _LValue = term;
			IStrategoTerm inner = Helpers.at(term, 0);
			GraphBuilder inner_nr = createCfg_inner(inner);
			result_graph.merge(inner_nr);
			result_graph.fromEntry(inner_nr.getEntry());
			result_graph.toExit(inner_nr.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("IfThen") && term.getSubtermCount() == 2)) {
			IStrategoTerm _IfThen = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			GraphBuilder c_nr = createCfg_inner(c);
			GraphBuilder t_nr = createCfg_inner(t);
			result_graph.merge(c_nr);
			result_graph.merge(t_nr);
			result_graph.fromEntry(c_nr.getEntry());
			result_graph.connect(c_nr.getExit(), t_nr.getEntry());
			result_graph.toExit(t_nr.getExit());
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
			result_graph.fromEntry(expr_nr.getEntry());
			result_graph.connect(expr_nr.getExit(), lval_nr.getEntry());
			result_graph.connect(lval_nr.getExit(), _Assign_nb.getEntry());
			result_graph.toExit(_Assign_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Seq") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Seq = term;
			IStrategoTerm stmts = Helpers.at(term, 0);
			GraphBuilder stmts_nr = createCfg_inner(stmts);
			result_graph.merge(stmts_nr);
			result_graph.fromEntry(stmts_nr.getEntry());
			result_graph.toExit(stmts_nr.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("For") && term.getSubtermCount() == 2)) {
			IStrategoTerm _For = term;
			IStrategoTerm binding = Helpers.at(term, 0);
			IStrategoTerm body = Helpers.at(term, 1);
			GraphBuilder binding_nr = createCfg_inner(binding);
			GraphBuilder body_nr = createCfg_inner(body);
			result_graph.merge(binding_nr);
			result_graph.merge(body_nr);
			result_graph.fromEntry(binding_nr.getEntry());
			result_graph.connect(binding_nr.getExit(), body_nr.getEntry());
			result_graph.connect(body_nr.getExit(), binding_nr.getEntry());
			result_graph.toExit(binding_nr.getExit());
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("LoopBinding") && term.getSubtermCount() == 3)) {
			IStrategoTerm _LoopBinding = term;
			IStrategoTerm var = Helpers.at(term, 0);
			IStrategoTerm from = Helpers.at(term, 1);
			IStrategoTerm to = Helpers.at(term, 2);
			GraphBuilder from_nr = createCfg_inner(from);
			GraphBuilder to_nr = createCfg_inner(to);
			GraphBuilder _LoopBinding_nb = GraphBuilder.fromSingle(Helpers.getTermId(_LoopBinding));
			result_graph.merge(from_nr);
			result_graph.merge(to_nr);
			result_graph.merge(_LoopBinding_nb);
			result_graph.fromEntry(from_nr.getEntry());
			result_graph.connect(from_nr.getExit(), to_nr.getEntry());
			result_graph.connect(to_nr.getExit(), _LoopBinding_nb.getEntry());
			result_graph.toExit(_LoopBinding_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Let") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Let = term;
			IStrategoTerm decs = Helpers.at(term, 0);
			IStrategoTerm exps = Helpers.at(term, 1);
			GraphBuilder decs_nr = createCfg_inner(decs);
			GraphBuilder exps_nr = createCfg_inner(exps);
			result_graph.merge(decs_nr);
			result_graph.merge(exps_nr);
			result_graph.fromEntry(decs_nr.getEntry());
			result_graph.connect(decs_nr.getExit(), exps_nr.getEntry());
			result_graph.toExit(exps_nr.getExit());
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
			result_graph.fromEntry(len_nr.getEntry());
			result_graph.connect(len_nr.getExit(), init_nr.getEntry());
			result_graph.connect(init_nr.getExit(), _Array_nb.getEntry());
			result_graph.toExit(_Array_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Hole") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Hole = term;
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Return") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Return = term;
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Var") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Var = term;
			GraphBuilder _Var_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Var));
			result_graph.merge(_Var_nb);
			result_graph.fromEntry(_Var_nb.getEntry());
			result_graph.toExit(_Var_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Int") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Int = term;
			GraphBuilder _Int_nb = GraphBuilder.fromSingle(Helpers.getTermId(_Int));
			result_graph.merge(_Int_nb);
			result_graph.fromEntry(_Int_nb.getEntry());
			result_graph.toExit(_Int_nb.getExit());
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("String") && term.getSubtermCount() == 1)) {
			IStrategoTerm _String = term;
			GraphBuilder _String_nb = GraphBuilder.fromSingle(Helpers.getTermId(_String));
			result_graph.merge(_String_nb);
			result_graph.fromEntry(_String_nb.getEntry());
			result_graph.toExit(_String_nb.getExit());
		} else {
			throw new RuntimeException("Could not create CFG node for term '" + term + "'.");
		}
		return result_graph;
	}
}