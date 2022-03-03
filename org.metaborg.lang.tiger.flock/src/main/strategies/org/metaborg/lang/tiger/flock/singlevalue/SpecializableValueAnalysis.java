package org.metaborg.lang.tiger.flock.singlevalue;

import org.metaborg.lang.tiger.flock.common.SingleAnalysis;
import org.metaborg.lang.tiger.flock.common.SpecializableAnalysis;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpecializableValueAnalysis extends SpecializableAnalysis {
	public SpecializableValueAnalysis() {
		super("values", Direction.FORWARD);
	}

	@Override
	public SingleAnalysis makeSpecialization(IStrategoTerm sub) {
		return new SingleValueAnalysis(sub);
	}
}
