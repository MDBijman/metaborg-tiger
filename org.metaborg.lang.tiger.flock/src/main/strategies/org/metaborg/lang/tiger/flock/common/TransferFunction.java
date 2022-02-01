package org.metaborg.lang.tiger.flock.common;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public abstract class TransferFunction {
	public abstract boolean eval(FlockLattice res, Node node);
}