/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.VersionedDataInputStream;

/**
 * !TypePattern
 * 
 * <p>any binding to formals is explicitly forbidden for any composite, ! is
 * just the most obviously wrong case.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class NotTypePattern extends TypePattern {
	private TypePattern pattern;
	
	public NotTypePattern(TypePattern pattern) {
		super(false,false);  //??? we override all methods that care about includeSubtypes
		this.pattern = pattern;
		setLocation(pattern.getSourceContext(), pattern.getStart(), pattern.getEnd());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}
	
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return pattern.matchesInstanceof(type).not();
	}

	protected boolean matchesExactly(ResolvedTypeX type) {
		return !pattern.matchesExactly(type);
	}
	
	public boolean matchesStatically(Class type) {
		return !pattern.matchesStatically(type);
	}

	public FuzzyBoolean matchesInstanceof(Class type) {
		return pattern.matchesInstanceof(type).not();
	}

	protected boolean matchesExactly(Class type) {
		return !pattern.matchesExactly(type);
	}
	
	public boolean matchesStatically(ResolvedTypeX type) {
		return !pattern.matchesStatically(type);
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.NOT);
		pattern.write(s);
		writeLocation(s);
	}
	
	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new NotTypePattern(TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public TypePattern resolveBindings(
		IScope scope,
		Bindings bindings,
		boolean allowBinding, boolean requireExactType)
	{
		if (requireExactType) return notExactType(scope);
		pattern = pattern.resolveBindings(scope, bindings, false, false);
		return this;
	}
	
	public TypePattern resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
		if (requireExactType) return TypePattern.NO;
		pattern = pattern.resolveBindingsFromRTTI(allowBinding,requireExactType);
		return this;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append('(');
			buff.append(annotationPattern.toString());
			buff.append(' ');
		}
		buff.append('!');
		buff.append(pattern);
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append(')');
		}
		return buff.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (! (obj instanceof NotTypePattern)) return false;
		return (pattern.equals(((NotTypePattern)obj).pattern));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 + 37 * pattern.hashCode();
	}
}
