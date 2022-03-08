package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoString;

public class TermTree {
	public abstract class ITerm {
		private TermId id;
		private TermTree tree;
		public boolean irregular = false;

		public ITerm(TermTree tree, TermId id) {
			this.id = id;
			this.tree = tree;
		}

		public boolean containsIrregular() {
			if (irregular)
				return true;

			for (ITerm c : this.children()) {
				if (c.containsIrregular()) {
					return true;
				}
			}

			return false;
		}

		public boolean isAppl() {
			return false;
		}

		public boolean isList() {
			return false;
		}

		public boolean isTuple() {
			return false;
		}

		public boolean isString() {
			return false;
		}

		public boolean isInt() {
			return false;
		}

		public TermId getId() {
			return this.id;
		}

		public Collection<ITerm> children() {
			return tree.childrenOf(this);
		}

		public ITerm childAt(int index) {
			return tree.childrenOf(this).get(index);
		}

		public int childrenCount() {
			return tree.childrenOf(this).size();
		}

		public IStrategoTerm toTerm() {
			return tree.nodeToStrategoTerm(this);
		}

		public IStrategoTerm toTermWithoutAnnotations() {
			return tree.nodeToStrategoTermWithoutAnnotations(this);
		}

		@Override
		public String toString() {
			return this.toString(-1);
		}

		public String toString(int depth) {
			return this.tree.nodeToString(this, depth);
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(id).toHashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			ITerm rhs = (ITerm) other;
			return id == rhs.id;
		}
	}

	public class ApplTerm extends ITerm {
		String constructor;

		public ApplTerm(TermTree tree, TermId id, String constructor) {
			super(tree, id);
			this.constructor = constructor;
		}

		public String getConstructor() {
			return this.constructor;
		}

		@Override
		public boolean isAppl() {
			return true;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(constructor).toHashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			ApplTerm rhs = (ApplTerm) other;
			return new EqualsBuilder().appendSuper(super.equals(other)).append(constructor, rhs.constructor).isEquals();
		}
	}

	public class StringTerm extends ITerm {
		String stringValue;

		public StringTerm(TermTree tree, TermId id, String stringValue) {
			super(tree, id);
			this.stringValue = stringValue;
		}

		@Override
		public boolean isString() {
			return true;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(stringValue).toHashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			StringTerm rhs = (StringTerm) other;
			return new EqualsBuilder().appendSuper(super.equals(other)).append(stringValue, rhs.stringValue).isEquals();
		}
	}

	public class IntTerm extends ITerm {
		int intValue;

		public IntTerm(TermTree tree, TermId id, int intValue) {
			super(tree, id);
			this.intValue = intValue;
		}

		@Override
		public boolean isInt() {
			return true;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(intValue).toHashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			IntTerm rhs = (IntTerm) other;
			return new EqualsBuilder().appendSuper(super.equals(other)).append(intValue, rhs.intValue).isEquals();
		}
	}

	public class ListTerm extends ITerm {
		public ListTerm(TermTree tree, TermId id) {
			super(tree, id);
		}

		@Override
		public boolean isList() {
			return true;
		}
	}

	/*
	 * Graph Structure fields
	 */

	private HashMap<ITerm, ArrayList<ITerm>> children = new HashMap<>();
	private HashMap<ITerm, ITerm> parents = new HashMap<>();
	private HashMap<TermId, ITerm> nodes = new HashMap<>();
	private ITerm root;
	private static final boolean DEBUG = Flock.DEBUG;

	public TermTree(IStrategoTerm term) {
		root = this.createTermInTree(term);
	}

	public boolean replace(TermId toReplace, IStrategoTerm replaceWith) {
		ITerm toReplaceNode = this.nodes.get(toReplace);
		ITerm newNode = this.createTermInTree(replaceWith);
		boolean result = toReplaceNode.containsIrregular();
		if (toReplaceNode == root) {
			this.remove(toReplaceNode);
			this.root = newNode;
		} else {
			ITerm parent = this.parents.get(toReplaceNode);
			int index = this.children.get(parent).indexOf(toReplaceNode);
			this.children.get(parent).set(index, newNode);
			this.parents.put(newNode, parent);
			this.remove(toReplaceNode);
		}
		return result;
	}

	public void remove(TermId n) {
		this.remove(this.nodes.get(n));
	}

	private void remove(ITerm n) {
		if (n == null) {
			throw new IllegalArgumentException("No node with this id");
		}

		for (ITerm child : this.children.get(n)) {
			this.removeInner(child.id);
		}

		if (this.root != n) {
			ITerm parent = this.parents.get(n);
			this.children.get(parent).remove(n);
		}

		this.nodes.remove(n.id);
		this.children.remove(n);
		this.parents.remove(n);

		if (this.root == n) {
			this.root = null;
		}
	}

	private void removeInner(TermId toRemove) {
		ITerm n = this.nodes.get(toRemove);

		if (n == null) {
			throw new IllegalArgumentException("No node with this id");
		}

		for (ITerm child : this.children.get(n)) {
			this.removeInner(child.id);
		}

		this.nodes.remove(toRemove);
		this.children.remove(n);
		this.parents.remove(n);
	}

	public void validate() {
		if (!DEBUG)
			return;

		if (this.root == null) {
			throw new RuntimeException("Null root");
		}

		if (!nodes.containsValue(root)) {
			throw new RuntimeException("Root is not in nodes");
		}

		for (ITerm n : nodes.values()) {
			if (n != root) {
				if (this.parents.get(n) == null) {
					throw new RuntimeException("Null parents");
				}
				ITerm parent = this.parents.get(n);
				if (!this.children.get(parent).contains(n)) {
					throw new RuntimeException("Child not in parent list");
				}
			}

			if (this.children.get(n) == null) {
				throw new RuntimeException("Null children");
			}
		}

		Set<ITerm> visited = new HashSet<>();
		Stack<ITerm> toVisit = new Stack<>();
		visited.add(root);
		List<ITerm> rootChildren = this.childrenOf(root);
		toVisit.addAll(rootChildren);

		while (!toVisit.empty()) {
			ITerm next = toVisit.pop();
			visited.add(next);
			toVisit.addAll(this.childrenOf(next));
		}

		if (visited.size() != nodes.size()) {
			throw new RuntimeException("Nodes disjoint from tree");
		}
	}

	private void putNode(ITerm n) {
		this.nodes.put(n.id, n);
		this.children.put(n, new ArrayList<>());
		this.parents.put(n, null);
	}

	private ITerm createTermInTree(IStrategoTerm term) {
		ITerm res = null;
		if (term instanceof IStrategoString) {
			IStrategoString asString = (IStrategoString) term;
			StringTerm sn = new StringTerm(this, Helpers.getTermId(term), asString.stringValue());
			this.putNode(sn);
			res = sn;
		} else if (term instanceof IStrategoInt) {
			IStrategoInt asInt = (IStrategoInt) term;
			IntTerm in = new IntTerm(this, Helpers.getTermId(term), asInt.intValue());
			this.putNode(in);
			res = in;
		} else if (term instanceof IStrategoList) {
			ListTerm ln = new ListTerm(this, Helpers.getTermId(term));
			this.putNode(ln);
			res = ln;
		} else if (term instanceof IStrategoAppl) {
			IStrategoAppl asAppl = (IStrategoAppl) term;
			ApplTerm tn = new ApplTerm(this, Helpers.getTermId(term), asAppl.getName().toString());
			this.putNode(tn);
			res = tn;
		} else {
			throw new RuntimeException("Unsupported term type: " + term.toString());
		}

		for (IStrategoTerm child : term.getAllSubterms()) {
			ITerm sub = this.createTermInTree(child);
			this.children.get(res).add(sub);
			this.parents.put(sub, res);
		}

		return res;
	}

	public String nodeToString(ITerm n) {
		if (!this.nodes.containsKey(n.id)) {
			throw new RuntimeException("Term does not exist in tree");
		}

		return this.nodeToStrategoTerm(n).toString();
	}

	public String nodeToString(ITerm n, int depth) {
		if (!this.nodes.containsKey(n.id)) {
			throw new RuntimeException("Term does not exist in tree");
		}

		return this.nodeToStrategoTerm(n).toString(depth);
	}

	public IStrategoTerm nodeToStrategoTerm(ITerm n) {
		ITermFactory tf = Flock.instance.factory;

		if (!this.nodes.containsKey(n.id)) {
			throw new RuntimeException("Term does not exist in tree");
		}

		if (n instanceof StringTerm) {
			return tf.annotateTerm(tf.makeString(((StringTerm) n).stringValue), idAsList(n));
		} else if (n instanceof IntTerm) {
			return tf.annotateTerm(tf.makeInt(((IntTerm) n).intValue), idAsList(n));
		} else if (n instanceof ListTerm) {
			List<ITerm> children = this.childrenOf(n);
			IStrategoTerm head = null;
			IStrategoList tail = null;
			IStrategoList anno = idAsList(n);

			// Counts down because we append to the front not the back
			for (int i = children.size() - 1; i >= 0; i--) {
				IStrategoTerm newHead = this.nodeToStrategoTerm(children.get(i));
				tail = tf.makeListCons(head, tail);
				head = newHead;
			}
			return new StrategoList(head, tail, anno);
		} else if (n instanceof ApplTerm) {
			List<ITerm> children = this.childrenOf(n);
			IStrategoConstructor ctor = tf.makeConstructor(((ApplTerm) n).constructor, children.size());
			IStrategoTerm[] termChildren = children.stream().map(c -> this.nodeToStrategoTerm(c))
					.toArray(IStrategoTerm[]::new);
			return tf.annotateTerm(tf.makeAppl(ctor, termChildren), idAsList(n));
		} else {
			throw new RuntimeException("Unsupported node type: " + n.toString());
		}
	}

	public IStrategoTerm nodeToStrategoTermWithoutAnnotations(ITerm n) {
		ITermFactory tf = Flock.instance.factory;

		if (!this.nodes.containsKey(n.id)) {
			throw new RuntimeException("Term does not exist in tree");
		}

		if (n instanceof StringTerm) {
			return new StrategoString(((StringTerm) n).stringValue, null);
		} else if (n instanceof IntTerm) {
			return new StrategoInt(((IntTerm) n).intValue, null);
		} else if (n instanceof ListTerm) {
			List<ITerm> children = this.childrenOf(n);
			IStrategoTerm head = null;
			IStrategoList tail = null;

			// Counts down because we append to the front not the back
			for (int i = children.size() - 1; i >= 0; i--) {
				IStrategoTerm newHead = this.nodeToStrategoTermWithoutAnnotations(children.get(i));
				tail = tf.makeListCons(head, tail);
				head = newHead;
			}
			return tf.makeListCons(head, tail);
		} else if (n instanceof ApplTerm) {
			List<ITerm> children = this.childrenOf(n);
			IStrategoConstructor ctor = tf.makeConstructor(((ApplTerm) n).constructor, children.size());
			IStrategoTerm[] termChildren = children.stream().map(c -> this.nodeToStrategoTermWithoutAnnotations(c))
					.toArray(IStrategoTerm[]::new);
			IStrategoAppl res = tf.makeAppl(ctor, termChildren);
			return res;
		} else {
			throw new RuntimeException("Unsupported node type: " + n.toString());
		}
	}

	private IStrategoList idAsList(ITerm n) {
		if (n.id == null) {
			throw new RuntimeException("Node does not have an id");
		}
		ITermFactory tf = Flock.instance.factory;

		IStrategoTerm[] kids = { tf.makeInt((int) n.id.getId()) };
		return tf.makeList(tf.makeAppl(tf.makeConstructor("FlockNodeId", 1), kids));
	}

	/*
	 * Getters
	 */

	public ITerm nodeById(TermId id) {
		return this.nodes.get(id);
	}

	public ITerm root() {
		return this.root;
	}

	public List<ITerm> childrenOf(ITerm n) {
		return this.children.get(n);
	}

	public ITerm parentOf(ITerm n) {
		return this.parents.get(n);
	}

	public Collection<ITerm> nodes() {
		return this.nodes.values();
	}

	public long size() {
		return this.nodes.size();
	}

	@Override
	public String toString() {
		return this.nodeToString(this.root);
	}

	public void markIrregular(TermId t) {
		this.nodes.get(t).irregular = true;
	}

	public boolean isIrregular(TermId t) {
		return this.nodes.get(t).irregular;
	}
}
