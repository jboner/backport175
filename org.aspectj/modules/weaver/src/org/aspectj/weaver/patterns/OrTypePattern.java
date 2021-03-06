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
 * left || right
 * 
 * <p>any binding to formals is explicitly forbidden for any composite by the language
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class OrTypePattern extends TypePattern {
	private TypePattern left, right;
	
	public OrTypePattern(TypePattern left, TypePattern right) {
		super(false,false);  //??? we override all methods that care about includeSubtypes
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true; // don't dive at the moment...
	}
	
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return left.matchesInstanceof(type).or(right.matchesInstanceof(type));
	}

	protected boolean matchesExactly(ResolvedTypeX type) {
		//??? if these had side-effects, this sort-circuit could be a mistake
		return left.matchesExactly(type) || right.matchesExactly(type);
	}
	
	public boolean matchesStatically(ResolvedTypeX type) {
		return left.matchesStatically(type) || right.matchesStatically(type);
	}

	public FuzzyBoolean matchesInstanceof(Class type) {
		return left.matchesInstanceof(type).or(right.matchesInstanceof(type));
	}

	protected boolean matchesExactly(Class type) {
		//??? if these had side-effects, this sort-circuit could be a mistake
		return left.matchesExactly(type) || right.matchesExactly(type);
	}
	
	public boolean matchesStatically(Class type) {
		return left.matchesStatically(type) || right.matchesStatically(type);
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.OR);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}
	
	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new OrTypePattern(TypePattern.read(s, context), TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public TypePattern resolveBindings(
		IScope scope,
		Bindings bindings,
		boolean allowBinding, boolean requireExactType)
	{
		if (requireExactType) return notExactType(scope);
		left = left.resolveBindings(scope, bindings, false, false);
		right = right.resolveBindings(scope, bindings, false, false);
		return this;
	}
	
	public TypePattern resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
		if (requireExactType) return TypePattern.NO;
		left = left.resolveBindingsFromRTTI(allowBinding,requireExactType);
		right = right.resolveBindingsFromRTTI(allowBinding,requireExactType);
		return this;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append('(');
			buff.append(annotationPattern.toString());
			buff.append(' ');
		}
		buff.append('(');
		buff.append(left.toString());
		buff.append(" || ");
		buff.append(right.toString());
		buff.append(')');
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append(')');
		}
		return buff.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (! (obj instanceof OrTypePattern)) return false;
		OrTypePattern other = (OrTypePattern) obj;
		return left.equals(other.left) && right.equals(other.right);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int ret = 17;
		ret = ret + 37 * left.hashCode();
		ret = ret + 37 * right.hashCode();
		return ret;
	}
}
