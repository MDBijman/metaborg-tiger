package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.trans.flock_replace_node_0_1;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_1_1;

public class flock_opt_2_0 extends Strategy {

	public static flock_opt_2_0 instance = new flock_opt_2_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy fundecOpt, Strategy optOne) {
		// Flock.printDebug(term.toString());
		if (TermUtils.isAppl(term)) {
			IStrategoAppl asAppl = M.appl(term);

			if (asAppl.getConstructor().getName().equals("Let")) {
				// Replace constant in let
				IStrategoTerm body = term.getAllSubterms()[1];
				if (TermUtils.isList(body)) {
					IStrategoList list = M.list(body);
					if (list.size() == 1) {
						IStrategoTerm elem = list.getAllSubterms()[0];
						if (TermUtils.isAppl(elem) && M.appl(elem).getConstructor().getName().equals("Int")) {
							return flock_replace_node_0_1.instance.invoke(context, elem, term);
						}
					}
				}

				// Recurse
				IStrategoString scope1 = context.getFactory().makeString("InlineFn");
				IStrategoString scope2 = context.getFactory().makeString("InlineCount");

				return dr_scope_1_1.instance.invoke(context, term, new Strategy() {
					@Override
					public IStrategoTerm invoke(Context context, IStrategoTerm term) {
						return dr_scope_1_1.instance.invoke(context, term, new Strategy() {
							@Override
							public IStrategoTerm invoke(Context context, IStrategoTerm term) {
								IStrategoTerm left = term.getSubterm(0);
								IStrategoTerm right = term.getSubterm(1);
								IStrategoList annos = term.getAnnotations();
								IStrategoTerm subterm = context.getFactory().makeTuple(left, right);
								IStrategoTerm result = flock_opt_2_0.instance.invoke(context, subterm, fundecOpt,
										optOne);
								if (result == null) {
									return null;
								} else {
									return context.getFactory()
											.annotateTerm(context.getFactory().makeAppl(asAppl.getConstructor(),
													result.getSubterm(0), result.getSubterm(1)), annos);
								}
							}
						}, scope2);
					}
				}, scope1);

			} else if (asAppl.getConstructor().getName().equals("FunDec")) {
				Flock.increment("fundecOpt");
				return fundecOpt.invoke(context, term);
			} else {
				boolean hasSucceeded = false;
				IStrategoTerm[] newSubterms = new IStrategoTerm[asAppl.getSubtermCount()];
				for (int i = 0; i < asAppl.getSubtermCount(); i++) {
					IStrategoTerm t = asAppl.getSubterm(i);
					IStrategoTerm newT = this.invoke(context, t, fundecOpt, optOne);
					if (newT != null) {
						hasSucceeded = true;
						newSubterms[i] = newT;
					} else {
						newSubterms[i] = t;
					}
				}

				IStrategoTerm newTerm = context.getFactory().makeAppl(asAppl.getConstructor(), newSubterms,
						asAppl.getAnnotations());

				// We can try optimize this term
				if (hasSucceeded) {
					IStrategoTerm t = optOne.invoke(context, newTerm);
					if (t == null) {
						return newTerm;
					} else {
						return t;
					}
				} else {
					IStrategoTerm t = optOne.invoke(context, newTerm);
					return t;
				}
			}
		} else if (TermUtils.isList(term)) {
			IStrategoList asList = M.list(term);

			boolean hasSucceeded = false;
			IStrategoTerm[] newSubterms = new IStrategoTerm[asList.getSubtermCount()];
			for (int i = 0; i < asList.getSubtermCount(); i++) {
				IStrategoTerm t = asList.getSubterm(i);
				IStrategoTerm newT = this.invoke(context, t, fundecOpt, optOne);
				if (newT != null) {
					hasSucceeded = true;
					newSubterms[i] = newT;
				} else {
					newSubterms[i] = t;
				}
			}

			IStrategoTerm newTerm = context.getFactory().makeList(newSubterms, asList.getAnnotations());

			// We can try optimize this term
			if (hasSucceeded) {
				IStrategoTerm t = optOne.invoke(context, newTerm);
				if (t == null) {
					return newTerm;
				} else {
					return t;
				}
			} else {
				IStrategoTerm t = optOne.invoke(context, newTerm);
				return t;
			}
		} else if (TermUtils.isTuple(term)) {
			IStrategoTuple asTuple = M.tuple(term);

			boolean hasSucceeded = false;
			IStrategoTerm[] newSubterms = new IStrategoTerm[asTuple.getSubtermCount()];
			for (int i = 0; i < asTuple.getSubtermCount(); i++) {
				IStrategoTerm t = asTuple.getSubterm(i);
				IStrategoTerm newT = this.invoke(context, t, fundecOpt, optOne);
				if (newT != null) {
					hasSucceeded = true;
					newSubterms[i] = newT;
				} else {
					newSubterms[i] = t;
				}
			}

			IStrategoTerm newTerm = context.getFactory().makeTuple(newSubterms, asTuple.getAnnotations());

			// We can try optimize this term
			if (hasSucceeded) {
				IStrategoTerm t = optOne.invoke(context, newTerm);
				if (t == null) {
					return newTerm;
				} else {
					return t;
				}
			} else {
				IStrategoTerm t = optOne.invoke(context, newTerm);
				return t;
			}
		} else {
			return null;
		}
	}
}