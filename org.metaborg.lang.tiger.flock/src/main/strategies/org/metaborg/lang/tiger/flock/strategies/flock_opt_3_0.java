package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.trans.flock_replace_node_0_1;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_1_1;

public class flock_opt_3_0 extends Strategy {

	public static flock_opt_3_0 instance = new flock_opt_3_0();

	private IStrategoTerm optLet(Context context, IStrategoTerm term, Strategy pre, Strategy fundecOpt, Strategy post) {
		ITermFactory factory = context.getFactory();
		IStrategoAppl asAppl = M.appl(term);

		// Replace constant in let
		IStrategoTerm body = term.getAllSubterms()[1];
		if (TermUtils.isList(body)) {
			IStrategoList list = M.list(body);
			if (list.size() == 1) {
				IStrategoTerm elem = list.getSubterm(0);
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
						IStrategoTerm subterm = factory.makeTuple(left, right);
						IStrategoTerm result = flock_opt_3_0.instance.invoke(context, subterm, pre, fundecOpt, post);
						if (result == null) {
							return null;
						} else {
							return context.getFactory().annotateTerm(factory.makeAppl(asAppl.getConstructor(),
									result.getSubterm(0), result.getSubterm(1)), annos);
						}
					}
				}, scope2);
			}
		}, scope1);

	}

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy pre, Strategy fundecOpt, Strategy post) {
		boolean transformed = false;
		IStrategoTerm preResult = pre.invoke(context, term);
		if (preResult != null) {
			transformed = true;
			term = preResult;
		}

		ITermFactory factory = context.getFactory();
		if (TermUtils.isAppl(term)) {
			IStrategoAppl asAppl = M.appl(term);

			if (asAppl.getConstructor().getName().equals("Let")) {
				IStrategoTerm letResult = this.optLet(context, term, pre, fundecOpt, post);

				if (letResult != null) {
					transformed = true;
					term = letResult;
				}

				// We can try optimize this term
				IStrategoTerm postTerm = post.invoke(context, term);
				if (postTerm == null && transformed) {
					return term;
				} else {
					return postTerm;
				}
			} else if (asAppl.getConstructor().getName().equals("FunDec")) {
				IStrategoTerm fundecResult = fundecOpt.invoke(context, term);

				if (fundecResult == null && transformed) {
					return term;
				} else {
					return fundecResult;
				}
			} else {
				IStrategoTerm[] newSubterms = new IStrategoTerm[asAppl.getSubtermCount()];
				for (int i = 0; i < asAppl.getSubtermCount(); i++) {
					IStrategoTerm t = asAppl.getSubterm(i);
					IStrategoTerm newT = this.invoke(context, t, pre, fundecOpt, post);
					if (newT != null) {
						transformed = true;
						newSubterms[i] = newT;
					} else {
						newSubterms[i] = t;
					}
				}
				term = factory.makeAppl(asAppl.getConstructor(), newSubterms, asAppl.getAnnotations());

				// We can try optimize this term
				IStrategoTerm postTerm = post.invoke(context, term);
				if (postTerm == null && transformed) {
					return term;
				} else {
					return postTerm;
				}
			}
		} else if (TermUtils.isList(term) || TermUtils.isTuple(term)) {
			IStrategoTerm[] newSubterms = new IStrategoTerm[term.getSubtermCount()];
			for (int i = 0; i < term.getSubtermCount(); i++) {
				IStrategoTerm t = term.getSubterm(i);
				IStrategoTerm newT = this.invoke(context, t, pre, fundecOpt, post);
				if (newT != null) {
					transformed = true;
					newSubterms[i] = newT;
				} else {
					newSubterms[i] = t;
				}
			}

			term = factory.makeList(newSubterms, term.getAnnotations());

			// We can try optimize this term
			IStrategoTerm postTerm = post.invoke(context, term);
			if (postTerm == null && transformed) {
				return term;
			} else {
				return postTerm;
			}
		} else {
			return null;
		}
	}
}