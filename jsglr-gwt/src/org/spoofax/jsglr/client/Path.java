/*
 * Created on 04.des.2005
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 * 
 * Licensed under the GNU Lesser General Public License, v2.1
 */
package org.spoofax.jsglr.client;

import java.util.ArrayList;
import java.util.List;


public class Path {

	private Path parent;
	private IParseNode label;
	private Frame frame;
	private int length;
	private int parentCount;
	private Link link;

	public int getRecoverCount()
	{
		int result = 0;
		if(link != null) {
			result += link.recoverCount;           
		}
		if(parent != null) {
			// TODO find out relation linktoparent/parent
			result += parent.getRecoverCount();
		}
		return result;        
	}

	public int getRecoverCount(int maxCharLength)
	{
		if(parent == null || this.length <= maxCharLength)
			return getRecoverCount();
		return parent.getRecoverCount(maxCharLength);
	}

	public static boolean logNewInstanceCreation = false;

	public Path reuse(Path parent, Link link, Frame frame, int length, int parentCount) {
		this.parent = parent;
		this.link = link;
		this.frame = frame;
		this.length = length;
		this.parentCount = parentCount;
		if(link != null){
			this.label = link.label;
        } else {
            this.label = null;
        }
		assert length >= 0;
		assert parentCount >= 0;
		return this;
	}

	public Frame getEnd() {
		return frame;
	}

	public final List<IParseNode> getParseNodes() {
		List<IParseNode> ret = new ArrayList<IParseNode>();

		int pos = 0;
		for (Path n = parent; n != null; n = n.parent) {
			ret.add(n.label);
		}

		return ret;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("<");
		for (Path p = this; p != null; p = p.parent) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(p.frame.state.stateNumber);
			first = false;
		}
		sb.append(">");
		return sb.toString();
	}

	public int getLength() {
		return length;
	}

	public Path getParent() {
		return parent;
	}

	public IParseNode getLabel() {
		return label;
	}

	public Link getLink() {
		return link;
	}

	public static Path valueOf(Path parent, Link link, Frame frame, int length/*, int parentCount*/) {
		Path r = new Path();
		r.parent = parent;
		r.link = link;
		if(link != null){
            r.label = link.label;
        } else {
            r.label = null;
        }
		r.frame = frame;
		r.length = length;
		r.parentCount = -1; //parentCount;
		return r;
	}
}


