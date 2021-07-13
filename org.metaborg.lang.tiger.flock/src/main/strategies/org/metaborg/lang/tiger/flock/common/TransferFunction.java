package org.metaborg.lang.tiger.flock.common;

import org.metaborg.lang.tiger.flock.common.Graph.Node;

public abstract class TransferFunction {
	public abstract FlockLattice eval(Node node);
}