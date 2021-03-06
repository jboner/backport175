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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * @annotation(@Foo) or @annotation(foo)
 * 
 * Matches any join point where the subject of the join point has an
 * annotation matching the annotationTypePattern:
 * 
 * Join Point Kind          Subject
 * ================================
 * method call              the target method
 * method execution         the method
 * constructor call         the constructor
 * constructor execution    the constructor
 * get                      the target field
 * set                      the target field
 * adviceexecution          the advice
 * initialization           the constructor
 * preinitialization        the constructor
 * staticinitialization     the type being initialized
 * handler                  the declared type of the handled exception
 */
public class AnnotationPointcut extends NameBindingPointcut {

	private ExactAnnotationTypePattern annotationTypePattern;
    private ShadowMunger munger = null; // only set after concretization
	
	public AnnotationPointcut(ExactAnnotationTypePattern type) {
		super();
		this.annotationTypePattern =  type;
		this.pointcutKind = Pointcut.ANNOTATION;
	}

	public AnnotationPointcut(ExactAnnotationTypePattern type, ShadowMunger munger) {
		this(type);
		this.munger = munger;
	}

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (info.getKind() == Shadow.StaticInitialization) {
			return annotationTypePattern.fastMatches(info.getType());
		} else {
			return FuzzyBoolean.MAYBE;
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		AnnotatedElement toMatchAgainst = null;
		Member member = shadow.getSignature();
		ResolvedMember rMember = member.resolve(shadow.getIWorld());

		if (rMember == null) {
		    if (member.getName().startsWith(NameMangler.PREFIX)) {
		    	return FuzzyBoolean.NO;
			}
			shadow.getIWorld().getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return FuzzyBoolean.NO;
		}

		Shadow.Kind kind = shadow.getKind();
		if (kind == Shadow.StaticInitialization) {
			toMatchAgainst = rMember.getDeclaringType().resolve(shadow.getIWorld());
		} else if ( (kind == Shadow.ExceptionHandler)) {
			toMatchAgainst = rMember.getParameterTypes()[0].resolve(shadow.getIWorld());
		} else {
			toMatchAgainst = rMember;
		}
		
		annotationTypePattern.resolve(shadow.getIWorld());
		return annotationTypePattern.matches(toMatchAgainst);
	}
	

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		annotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern.resolveBindings(scope,bindings,true);
		// must be either a Var, or an annotation type pattern
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindingsFromRTTI()
	 */
	protected void resolveBindingsFromRTTI() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedTypeX, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		ExactAnnotationTypePattern newType = (ExactAnnotationTypePattern) annotationTypePattern.remapAdviceFormals(bindings);		
		Pointcut ret = new AnnotationPointcut(newType, bindings.getEnclosingAdvice());
        ret.copyLocationFrom(this);
        return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {

		if (shadow.getKind()!=Shadow.MethodCall &&
			shadow.getKind()!=Shadow.ConstructorCall &&
			shadow.getKind()!=Shadow.ConstructorExecution &&
			shadow.getKind()!=Shadow.MethodExecution &&
			shadow.getKind()!=Shadow.FieldSet &&
			shadow.getKind()!=Shadow.FieldGet &&
			shadow.getKind()!=Shadow.StaticInitialization &&
			shadow.getKind()!=Shadow.PreInitialization &&
			shadow.getKind()!=Shadow.AdviceExecution &&
			shadow.getKind()!=Shadow.Initialization &&
			shadow.getKind()!=Shadow.ExceptionHandler
			) {
			IMessage lim = MessageUtil.error("Binding not supported in @pcds (1.5.0 M2 limitation) for "+shadow.getKind()+" join points, see: " +
						getSourceLocation());
			shadow.getIWorld().getMessageHandler().handleMessage(lim);
			throw new BCException("Binding not supported in @pcds (1.5.0 M2 limitation) for "+shadow.getKind()+" join points, see: " +
			getSourceLocation());
		}

		
		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern)annotationTypePattern;
			TypeX annotationType = btp.annotationType;
			Var var = shadow.getKindedAnnotationVar(annotationType);
			
			// This should not happen, we shouldn't have gotten this far 
			// if we weren't going to find the annotation
			if (var == null) throw new BCException("Impossible! annotation=["+annotationType+
					                               "]  shadow=["+shadow+" at "+shadow.getSourceLocation()+
												   "]    pointcut is at ["+getSourceLocation()+"]");//return Literal.FALSE;
			// Check if we have already bound something to this formal
			if ((state.get(btp.getFormalIndex())!=null) &&(lastMatchedShadowId == shadow.shadowId)) {
//				ISourceLocation pcdSloc = getSourceLocation(); 
//				ISourceLocation shadowSloc = shadow.getSourceLocation();
//				Message errorMessage = new Message(
//					"Cannot use @pointcut to match at this location and bind a formal to type '"+var.getType()+
//					"' - the formal is already bound to type '"+state.get(btp.getFormalIndex()).getType()+"'"+
//					".  The secondary source location points to the problematic binding.",
//					shadowSloc,true,new ISourceLocation[]{pcdSloc}); 
//				shadow.getIWorld().getMessageHandler().handleMessage(errorMessage);
				state.setErroneousVar(btp.getFormalIndex());
			}
			state.set(btp.getFormalIndex(),var);
		} 
		return Literal.TRUE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	public List getBindingAnnotationTypePatterns() {
		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			List l = new ArrayList();
			l.add(annotationTypePattern);
			return l;
		} else return Collections.EMPTY_LIST;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingTypePatterns()
	 */
	public List getBindingTypePatterns() {
		return Collections.EMPTY_LIST;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ANNOTATION);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		AnnotationPointcut ret = new AnnotationPointcut((ExactAnnotationTypePattern)type);
		ret.readLocation(context, s);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof AnnotationPointcut)) return false;
		AnnotationPointcut o = (AnnotationPointcut)other;
		return o.annotationTypePattern.equals(this.annotationTypePattern);
	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + annotationTypePattern.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("@annotation(");
		String annPatt = annotationTypePattern.toString();
		buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
		buf.append(")");
		return buf.toString();
	}

}
