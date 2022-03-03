package org.metaborg.lang.tiger.flock.strategies;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_opt_3_0 extends Strategy {

	public static flock_opt_3_0 instance = new flock_opt_3_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy letOpt, Strategy fundecOpt,
			Strategy optOne) {
		//Flock.printDebug(term.toString());
		if (TermUtils.isAppl(term)) {
			IStrategoAppl asAppl = M.appl(term);

			if (asAppl.getConstructor().getName().equals("Let")) {
				return letOpt.invoke(context, term);
			} else if (asAppl.getConstructor().getName().equals("FunDec")) {
				return fundecOpt.invoke(context, term);
			} else {
				boolean hasSucceeded = false;
				IStrategoTerm[] newSubterms = new IStrategoTerm[asAppl.getSubtermCount()];
				for (int i = 0; i < asAppl.getSubtermCount(); i++) {
					IStrategoTerm t = asAppl.getSubterm(i);
					IStrategoTerm newT = this.invoke(context, t, letOpt, fundecOpt, optOne);
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
				IStrategoTerm newT = this.invoke(context, t, letOpt, fundecOpt, optOne);
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
		} else {
			return null;
		}
	}
}