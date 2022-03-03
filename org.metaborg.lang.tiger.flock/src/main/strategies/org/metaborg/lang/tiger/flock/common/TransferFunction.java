package org.metaborg.lang.tiger.flock.common;

import java.util.stream.Stream;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public abstract class TransferFunction {
	/*
	 * We want to have access to the analysis direction within the transfer function
	 * for optimization purposes. It allows us to check if the current node has a
	 * single predecessor, in which case we can copy a reference to the result of
	 * evaluation instead of performing a lub.
	 */
	public abstract boolean eval(SingleAnalysis.Direction dir, FlockLattice res, Node node);

	/*
	 * Efficiently assigns result to lattice.
	 * Return true if lattice changed.
	 */
	public static boolean assignEvalResult(SingleAnalysis.Direction dir, Node node, FlockLattice lattice, Object result) {
		// Here we can assign reference if result is not a stream
		if (node.predecessors(dir).size() == 1) {
			// Its a stream so we must do lub since the lattice is aware of how to lub
			// streams
			if (result instanceof Stream) {
				return lattice.lub(result);
			}
			// If its a lattice we replace the inner value of res with the results inner value
			else if (result instanceof FlockLattice) {
				Object newLatticeValue = ((FlockLattice) result).value();
				boolean changed = !newLatticeValue.equals(lattice.value());
				lattice.setValue(newLatticeValue);
				return changed;
			}
			// Last possibility is that we are given the inner value of the lattice directly
			else {
				boolean changed = !result.equals(lattice.value());
				lattice.setValue(result);
				return changed;				
			}
		}
		// Here we must use lub since we need to combine results of multiple
		// predecessors
		else {
			return lattice.lub(result);
		}
	}

}