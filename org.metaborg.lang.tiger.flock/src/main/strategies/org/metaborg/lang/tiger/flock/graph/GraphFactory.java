package org.metaborg.lang.tiger.flock.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.GraphBuilder;
import org.metaborg.lang.tiger.flock.common.GraphBuilder.TermGraphMapping;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.TermId;
import org.metaborg.lang.tiger.flock.common.TermTree;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.stratego_lib.new_0_0;

public class GraphFactory {
	public static class PartialGraph {
		public Graph graph;
		public TermId entry, exit;

		private PartialGraph(Graph g, TermId entry, TermId exit) {
			this.graph = g;
			this.entry = entry;
			this.exit = exit;
		}
	}

	public static Graph createCfgRecursive(TermTree tree, IStrategoTerm term) {
		List<GraphBuilder> r = createCfgRecursive_inner(term);
		if (r.isEmpty()) {
			return null;
		}
		return GraphBuilder.build(r, tree);
	}

	public static PartialGraph createCfgOnce(TermTree tree, IStrategoTerm term) {
		GraphBuilder builder = new GraphBuilder();
		TermGraphMapping tgm = createCfg_inner(builder, term);
		List<GraphBuilder> builderList = new ArrayList<>();
		builderList.add(builder);
		Graph g = GraphBuilder.build(builderList, tree);
		return new PartialGraph(g, tgm.ENTRY, tgm.EXIT);
	}

	public static List<GraphBuilder> createCfgRecursive_inner(IStrategoTerm term) {
		ArrayList<GraphBuilder> graphs = new ArrayList<>();
		for (IStrategoTerm subterm : term.getSubterms()) {
			List<GraphBuilder> subgraph = createCfgRecursive_inner(subterm);
			graphs.addAll(subgraph);
		}
		if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Mod") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Mod = term;
			IStrategoTerm s = Helpers.at(term, 0);
			GraphBuilder builder = new GraphBuilder();
			TermGraphMapping s_nr = createCfg_inner(builder, s);
			builder.connect(builder.START, s_nr.ENTRY);
			builder.connect(s_nr.EXIT, builder.END);
			graphs.add(builder);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm body = Helpers.at(term, 2);
			GraphBuilder builder = new GraphBuilder();
			TermGraphMapping body_nr = createCfg_inner(builder, body);
			builder.connect(builder.START, body_nr.ENTRY);
			builder.connect(body_nr.EXIT, builder.END);
			graphs.add(builder);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm args = Helpers.at(term, 1);
			IStrategoTerm rt = Helpers.at(term, 2);
			IStrategoTerm body = Helpers.at(term, 3);
			GraphBuilder builder = new GraphBuilder();
			TermGraphMapping body_nr = createCfg_inner(builder, body);
			builder.connect(builder.START, body_nr.ENTRY);
			builder.connect(body_nr.EXIT, builder.END);
			graphs.add(builder);
		}
		return graphs;
	}

	private static TermGraphMapping createCfg_inner(GraphBuilder builder, IStrategoTerm term) {
		TermGraphMapping result_graph = GraphBuilder.newMapping(Helpers.getTermId(term));
		builder.materializeEntryExit(result_graph);
		if (TermUtils.isList(term)) {
			IStrategoList list = M.list(term);
			if (list.isEmpty()) {
				builder.connect(result_graph.ENTRY, result_graph.EXIT);
				return result_graph;
			}

			TermId currentTail = result_graph.ENTRY;
			while (!list.isEmpty()) {
				TermGraphMapping new_result = createCfg_inner(builder, list.head());
				builder.connect(currentTail, new_result.ENTRY);
				currentTail = new_result.EXIT;
				list = list.tail();
			}
			builder.connect(currentTail, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("ProcDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _ProcDec = term;
			builder.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("FunDec") && term.getSubtermCount() == 4)) {
			IStrategoTerm _FunDec = term;
			builder.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			builder.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("TypeDec") && term.getSubtermCount() == 2)) {
			IStrategoTerm _TypeDec = term;
			IStrategoTerm occ = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			builder.connect(result_graph.ENTRY, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			IStrategoTerm _VarDec = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			TermGraphMapping e_nr = createCfg_inner(builder, e);
			TermGraphMapping _VarDec_nb = result_graph;
			builder.materializeNode(_VarDec_nb);
			builder.connect(result_graph.ENTRY, e_nr.ENTRY);
			builder.connect(e_nr.EXIT, _VarDec_nb.term);
			builder.connect(_VarDec_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("VarDecNoType") && term.getSubtermCount() == 2)) {
			IStrategoTerm _VarDecNoType = term;
			IStrategoTerm n = Helpers.at(term, 0);
			IStrategoTerm e = Helpers.at(term, 1);
			TermGraphMapping e_nr = createCfg_inner(builder, e);
			TermGraphMapping _VarDecNoType_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, e_nr.ENTRY);
			builder.connect(e_nr.EXIT, _VarDecNoType_nb.term);
			builder.connect(_VarDecNoType_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("UMinus") && term.getSubtermCount() == 1)) {
			IStrategoTerm _UMinus = term;
			IStrategoTerm exp = Helpers.at(term, 0);
			TermGraphMapping exp_nr = createCfg_inner(builder, exp);
			TermGraphMapping _UMinus_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, exp_nr.ENTRY);
			builder.connect(exp_nr.EXIT, _UMinus_nb.term);
			builder.connect(_UMinus_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Minus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Minus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Minus_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Minus_nb.term);
			builder.connect(_Minus_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Plus") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Plus = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Plus_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Plus_nb.term);
			builder.connect(_Plus_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Times") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Times = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Times_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Times_nb.term);
			builder.connect(_Times_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Divide") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Divide = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Divide_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Divide_nb.term);
			builder.connect(_Divide_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Lt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Lt = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Lt_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Lt_nb.term);
			builder.connect(_Lt_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Gt") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Gt = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Gt_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Gt_nb.term);
			builder.connect(_Gt_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Eq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Eq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Eq_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Eq_nb.term);
			builder.connect(_Eq_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Geq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Geq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Geq_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Geq_nb.term);
			builder.connect(_Geq_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Leq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Leq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Leq_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Leq_nb.term);
			builder.connect(_Leq_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Neq") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Neq = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Neq_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Neq_nb.term);
			builder.connect(_Neq_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("And") && term.getSubtermCount() == 2)) {
			IStrategoTerm _And = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _And_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _And_nb.term);
			builder.connect(_And_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Or") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Or = term;
			IStrategoTerm lhs = Helpers.at(term, 0);
			IStrategoTerm rhs = Helpers.at(term, 1);
			TermGraphMapping lhs_nr = createCfg_inner(builder, lhs);
			TermGraphMapping rhs_nr = createCfg_inner(builder, rhs);
			TermGraphMapping _Or_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, lhs_nr.ENTRY);
			builder.connect(lhs_nr.EXIT, rhs_nr.ENTRY);
			builder.connect(rhs_nr.EXIT, _Or_nb.term);
			builder.connect(_Or_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("Subscript") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Subscript = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm idx = Helpers.at(term, 1);
			TermGraphMapping idx_nr = createCfg_inner(builder, idx);
			TermGraphMapping lval_nr = createCfg_inner(builder, lval);
			TermGraphMapping _Subscript_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, idx_nr.ENTRY);
			builder.connect(idx_nr.EXIT, lval_nr.ENTRY);
			builder.connect(lval_nr.EXIT, _Subscript_nb.term);
			builder.connect(_Subscript_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Call") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Call = term;
			IStrategoTerm args = Helpers.at(term, 1);
			TermGraphMapping args_nr = createCfg_inner(builder, args);
			TermGraphMapping _Call_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, args_nr.ENTRY);
			builder.connect(args_nr.EXIT, _Call_nb.term);
			builder.connect(_Call_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("If") && term.getSubtermCount() == 3)) {
			IStrategoTerm _If = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			IStrategoTerm e = Helpers.at(term, 2);
			TermGraphMapping c_nr = createCfg_inner(builder, c);
			TermGraphMapping t_nr = createCfg_inner(builder, t);
			TermGraphMapping e_nr = createCfg_inner(builder, e);
			builder.connect(result_graph.ENTRY, c_nr.ENTRY);
			builder.connect(c_nr.EXIT, t_nr.ENTRY);
			builder.connect(t_nr.EXIT, result_graph.EXIT);
			builder.connect(c_nr.EXIT, e_nr.ENTRY);
			builder.connect(e_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("LValue") && term.getSubtermCount() == 1)) {
			IStrategoTerm _LValue = term;
			IStrategoTerm inner = Helpers.at(term, 0);
			TermGraphMapping inner_nr = createCfg_inner(builder, inner);
			builder.connect(result_graph.ENTRY, inner_nr.ENTRY);
			builder.connect(inner_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("IfThen") && term.getSubtermCount() == 2)) {
			IStrategoTerm _IfThen = term;
			IStrategoTerm c = Helpers.at(term, 0);
			IStrategoTerm t = Helpers.at(term, 1);
			TermGraphMapping c_nr = createCfg_inner(builder, c);
			TermGraphMapping t_nr = createCfg_inner(builder, t);
			builder.connect(result_graph.ENTRY, c_nr.ENTRY);
			builder.connect(c_nr.EXIT, t_nr.ENTRY);
			builder.connect(t_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Assign") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Assign = term;
			IStrategoTerm lval = Helpers.at(term, 0);
			IStrategoTerm expr = Helpers.at(term, 1);
			TermGraphMapping expr_nr = createCfg_inner(builder, expr);
			TermGraphMapping lval_nr = createCfg_inner(builder, lval);
			TermGraphMapping _Assign_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, expr_nr.ENTRY);
			builder.connect(expr_nr.EXIT, lval_nr.ENTRY);
			builder.connect(lval_nr.EXIT, _Assign_nb.term);
			builder.connect(_Assign_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Seq") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Seq = term;
			IStrategoTerm stmts = Helpers.at(term, 0);
			TermGraphMapping stmts_nr = createCfg_inner(builder, stmts);
			builder.connect(result_graph.ENTRY, stmts_nr.ENTRY);
			builder.connect(stmts_nr.EXIT, result_graph.EXIT);
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
			TermGraphMapping from_nr = createCfg_inner(builder, from);
			TermGraphMapping to_nr = createCfg_inner(builder, to);
			TermGraphMapping binding_nr = createCfg_inner(builder, binding);
			TermGraphMapping body_nr = createCfg_inner(builder, body);
			builder.connect(result_graph.ENTRY, from_nr.ENTRY);
			builder.connect(from_nr.EXIT, to_nr.ENTRY);
			builder.connect(to_nr.EXIT, binding_nr.ENTRY);
			builder.connect(binding_nr.EXIT, body_nr.ENTRY);
			builder.connect(body_nr.EXIT, binding_nr.ENTRY);
			builder.connect(binding_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term)
				&& (M.appl(term).getName().equals("LoopBinding") && term.getSubtermCount() == 3)) {
			IStrategoTerm _LoopBinding = term;
			IStrategoTerm var = Helpers.at(term, 0);
			IStrategoTerm from = Helpers.at(term, 1);
			IStrategoTerm to = Helpers.at(term, 2);
			TermGraphMapping _LoopBinding_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, _LoopBinding_nb.term);
			builder.connect(_LoopBinding_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Let") && term.getSubtermCount() == 2)) {
			IStrategoTerm _Let = term;
			IStrategoTerm decs = Helpers.at(term, 0);
			IStrategoTerm exps = Helpers.at(term, 1);
			TermGraphMapping decs_nr = createCfg_inner(builder, decs);
			TermGraphMapping exps_nr = createCfg_inner(builder, exps);
			builder.connect(result_graph.ENTRY, decs_nr.ENTRY);
			builder.connect(decs_nr.EXIT, exps_nr.ENTRY);
			builder.connect(exps_nr.EXIT, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Array") && term.getSubtermCount() == 3)) {
			IStrategoTerm _Array = term;
			IStrategoTerm len = Helpers.at(term, 1);
			IStrategoTerm init = Helpers.at(term, 2);
			TermGraphMapping len_nr = createCfg_inner(builder, len);
			TermGraphMapping init_nr = createCfg_inner(builder, init);
			TermGraphMapping _Array_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, len_nr.ENTRY);
			builder.connect(len_nr.EXIT, init_nr.ENTRY);
			builder.connect(init_nr.EXIT, _Array_nb.term);
			builder.connect(_Array_nb.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Hole") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Hole = term;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, result_graph.term);
			builder.connect(result_graph.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Return") && term.getSubtermCount() == 0)) {
			IStrategoTerm _Return = term;
			builder.connect(result_graph.ENTRY, builder.END);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Var") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Var = term;
			TermGraphMapping _Var_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, result_graph.term);
			builder.connect(result_graph.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("Int") && term.getSubtermCount() == 1)) {
			IStrategoTerm _Int = term;
			TermGraphMapping _Int_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, result_graph.term);
			builder.connect(result_graph.term, result_graph.EXIT);
		} else if (TermUtils.isAppl(term) && (M.appl(term).getName().equals("String") && term.getSubtermCount() == 1)) {
			IStrategoTerm _String = term;
			TermGraphMapping _String_nb = result_graph;
			builder.materializeNode(result_graph);
			builder.connect(result_graph.ENTRY, result_graph.term);
			builder.connect(result_graph.term, result_graph.EXIT);
		} else {
			throw new RuntimeException("Could not create CFG node for term '" + term + "'.");
		}
		return result_graph;
	}
}