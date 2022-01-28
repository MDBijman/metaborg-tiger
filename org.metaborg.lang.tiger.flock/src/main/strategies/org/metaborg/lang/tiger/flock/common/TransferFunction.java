package org.metaborg.lang.tiger.flock.common;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public abstract class TransferFunction {
	public abstract FlockLattice eval(Node node);
	public boolean evalInplace(FlockLattice res, Node node) {
		throw new NotImplementedException();
	}
	public boolean supportsInplace() {
		return false;
	}
}