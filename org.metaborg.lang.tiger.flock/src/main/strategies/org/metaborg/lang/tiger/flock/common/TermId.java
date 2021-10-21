package org.metaborg.lang.tiger.flock.common;

public class TermId {
	private long id;

	public TermId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public TermId successor() {
		return new TermId(id + 1);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TermId)) {
			return false;
		}
		TermId other = (TermId) obj;
		return other.id == this.id;
	}

	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}

	@Override
	public String toString() {
		return id + "";
	}
}