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

import java.util.Arrays;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Var;

public class ExposedState {
	public Var[] vars;
	private boolean[] erroneousVars;
	private Expr aspectInstance;

	public ExposedState(int size) {
		super();
		vars = new Var[size];
		erroneousVars = new boolean[size];
	}

	public ExposedState(Member signature) {
		// XXX there maybe something about target for non-static sigs
		this(signature.getParameterTypes().length);
	}

	public void set(int i, Var var) {
		//XXX add sanity checks
		// Some checks added in ArgsPointcut and ThisOrTargetPointcut
//		if (vars[i]!=null) {
//			if (!var.getType().equals(vars[i].getType())) 
//			  throw new RuntimeException("Shouldn't allow a slot to change type! Currently="+var.getType()+"   New="+vars[i].getType());
//		}
		vars[i] = var;
	}
    public Var get(int i) {
        return vars[i];
    }
    public int size() {
        return vars.length;
    }

    public Expr getAspectInstance() {
        return aspectInstance;
    }

    public void setAspectInstance(Expr aspectInstance) {
        this.aspectInstance = aspectInstance;
    }

	public String toString() {
		return "ExposedState(" + Arrays.asList(vars) + ", " + aspectInstance + ")";
	}

	// Set to true if we have reported an error message against it,
	// prevents us blowing up in later code gen.
	public void setErroneousVar(int formalIndex) {
		erroneousVars[formalIndex]=true;
	}
	
	public boolean isErroneousVar(int formalIndex) {
		return erroneousVars[formalIndex];
	}
}
