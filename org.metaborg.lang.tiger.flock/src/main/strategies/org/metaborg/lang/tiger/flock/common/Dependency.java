package org.metaborg.lang.tiger.flock.common;

public class Dependency {
	public TermId id;

	public Dependency(TermId id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Dep(" + id.toString() + ")";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Dependency))
			return false;
		return this.id.equals(((Dependency) other).id);
	}
}
