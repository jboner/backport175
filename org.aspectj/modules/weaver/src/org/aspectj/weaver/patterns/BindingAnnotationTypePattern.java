/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.TypeX;

public class BindingAnnotationTypePattern extends ExactAnnotationTypePattern implements BindingPattern {

	private int formalIndex;
	
	/**
	 * @param annotationType
	 */
	public BindingAnnotationTypePattern(TypeX annotationType, int index) {
		super(annotationType);
		this.formalIndex = index;
	}
		
	public BindingAnnotationTypePattern(FormalBinding binding) {
		this(binding.getType(),binding.getIndex());
	}
	
	public int getFormalIndex() {
		return formalIndex;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof BindingAnnotationTypePattern)) return false;
		BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern) obj;
		return (super.equals(btp) && (btp.formalIndex == formalIndex));
	}
	
	public int hashCode() {
		return super.hashCode()*37 + formalIndex;
	}
	
	public AnnotationTypePattern remapAdviceFormals(IntMap bindings) {			
		if (!bindings.hasKey(formalIndex)) {
			return new ExactAnnotationTypePattern(annotationType);
		} else {
			int newFormalIndex = bindings.get(formalIndex);
			return new BindingAnnotationTypePattern(annotationType, newFormalIndex);
		}
	}
	private static final byte VERSION = 1; // rev if serialised form changed
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.ExactAnnotationTypePattern#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.BINDING);
		s.writeByte(VERSION);
		annotationType.write(s);
		s.writeShort((short)formalIndex);
		writeLocation(s);
	}	
	
	public static AnnotationTypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("BindingAnnotationTypePattern was written by a more recent version of AspectJ");
		}
		AnnotationTypePattern ret = new BindingAnnotationTypePattern(TypeX.read(s),s.readShort());
		ret.readLocation(context,s);
		return ret;
	}
}
