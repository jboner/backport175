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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.VersionedDataInputStream;

public class TypePatternList extends PatternNode {
	private TypePattern[] typePatterns;
	int ellipsisCount = 0;
	
	public static final TypePatternList EMPTY =
		new TypePatternList(new TypePattern[] {});
	
	public static final TypePatternList ANY =
	    new TypePatternList(new TypePattern[] {TypePattern.ELLIPSIS});
	
	public TypePatternList() {
		typePatterns = new TypePattern[0];
		ellipsisCount = 0;
	}

	public TypePatternList(TypePattern[] arguments) {
		this.typePatterns = arguments;
		for (int i=0; i<arguments.length; i++) {
			if (arguments[i] == TypePattern.ELLIPSIS) ellipsisCount++;
		}
	}
	
	public TypePatternList(List l) {
		this((TypePattern[]) l.toArray(new TypePattern[l.size()]));
	}
	
	public int size() { return typePatterns.length; }
	
	public TypePattern get(int index) {
		return typePatterns[index];
	}

    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("(");
    	for (int i=0, len=typePatterns.length; i < len; i++) {
    		TypePattern type = typePatterns[i];
    		if (i > 0) buf.append(", ");
    		if (type == TypePattern.ELLIPSIS) {
    			buf.append("..");
    		} else {
    			buf.append(type.toString());
    		}
    	}
    	buf.append(")");
    	return buf.toString();
    }
    
    
    /**
     * Used by reflection-based matching for args pcds.
     * Returns YES if types will always be matched by the pattern,
     *         NO if types do not match the pattern,
     *         MAYBE if types may match the pattern dependent on a runtime test
     */
    public FuzzyBoolean matchesArgsPatternSubset(Class[] types) {
    	int argsLength = types.length;
    	int patternLength = typePatterns.length;
    	int argsIndex = 0;
    	
    	if ((argsLength < patternLength) && (ellipsisCount == 0)) return FuzzyBoolean.NO;
    	if (argsLength < (patternLength -1)) return FuzzyBoolean.NO;
    	
    	int ellipsisMatchCount = argsLength - (patternLength - ellipsisCount);
    	
    	FuzzyBoolean ret = FuzzyBoolean.YES;
    	
    	for (int i = 0; i < typePatterns.length; i++) {
			if (typePatterns[i] == TypePattern.ELLIPSIS) {
				// match ellipsisMatchCount args
				argsIndex += ellipsisMatchCount;
			} else if (typePatterns[i] == TypePattern.ANY) {
				argsIndex++;
			} else {
				// match the argument type at argsIndex with the ExactTypePattern
				// we it is exact because nothing else is allowed in args
				ExactTypePattern tp = (ExactTypePattern)typePatterns[i];
				FuzzyBoolean matches = tp.willMatchDynamically(types[argsIndex]);
				if (matches == FuzzyBoolean.NO) {
					return FuzzyBoolean.NO;
				} else {
					argsIndex++;
					ret = ret.and(matches);
				}
			}
		}
    	
    	return ret;
    }
    
    //XXX shares much code with WildTypePattern and with NamePattern
    /**
     * When called with TypePattern.STATIC this will always return either
     * FuzzyBoolean.YES or FuzzyBoolean.NO.
     * 
     * When called with TypePattern.DYNAMIC this could return MAYBE if
     * at runtime it would be possible for arguments of the given static
     * types to dynamically match this, but it is not known for certain.
     * 
     * This method will never return FuzzyBoolean.NEVER
     */ 
    public FuzzyBoolean matches(ResolvedTypeX[] types, TypePattern.MatchKind kind) {
    	int nameLength = types.length;
		int patternLength = typePatterns.length;
		
		int nameIndex = 0;
		int patternIndex = 0;
		
		if (ellipsisCount == 0) {
			if (nameLength != patternLength) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				FuzzyBoolean ret = typePatterns[patternIndex++].matches(types[nameIndex++], kind);
				if (ret == FuzzyBoolean.NO) return ret;
				if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
			}
			return finalReturn;
		} else if (ellipsisCount == 1) {
			if (nameLength < patternLength-1) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns[patternIndex++];
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength-patternIndex);
				} else {
					FuzzyBoolean ret = p.matches(types[nameIndex++], kind);
				    if (ret == FuzzyBoolean.NO) return ret;
				    if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
				}
			}
			return finalReturn;
		} else {
//            System.err.print("match(" + arguments + ", " + types + ") -> ");
            FuzzyBoolean b =  outOfStar(typePatterns, types, 0, 0, patternLength - ellipsisCount, nameLength, ellipsisCount, kind);
//            System.err.println(b);
            return b;
    	}
    }
    
    
    // TODO Add TypePatternList.matches(Object[] objs)
    public FuzzyBoolean matches(Object[] objs, TypePattern.MatchKind kind) {
    	int nameLength = objs.length;
		int patternLength = typePatterns.length;
		
		int nameIndex = 0;
		int patternIndex = 0;
		
		if (ellipsisCount == 0) {
			if (nameLength != patternLength) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				FuzzyBoolean ret = typePatterns[patternIndex++].matches(objs[nameIndex++],kind);
				if (ret == FuzzyBoolean.NO) return ret;
				if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
			}
			return finalReturn;
		} else if (ellipsisCount == 1) {
			if (nameLength < patternLength-1) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns[patternIndex++];
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength-patternIndex);
				} else {
					FuzzyBoolean ret = p.matches(objs[nameIndex++],kind);
				    if (ret == FuzzyBoolean.NO) return ret;
				    if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
				}
			}
			return finalReturn;
		} else {
//            System.err.print("match(" + arguments + ", " + types + ") -> ");
            FuzzyBoolean b =  outOfStar(typePatterns, objs, 0, 0, patternLength - ellipsisCount, nameLength, ellipsisCount, kind);
//            System.err.println(b);
            return b;
    	}
    }
 
    // XXX run-time signature matching, too much duplicated code
    public FuzzyBoolean matches(Class[] types, TypePattern.MatchKind kind) {
    	int nameLength = types.length;
		int patternLength = typePatterns.length;
		
		int nameIndex = 0;
		int patternIndex = 0;
		
		if (ellipsisCount == 0) {
			if (nameLength != patternLength) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				FuzzyBoolean ret = typePatterns[patternIndex++].matches(types[nameIndex++], kind);
				if (ret == FuzzyBoolean.NO) return ret;
				if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
			}
			return finalReturn;
		} else if (ellipsisCount == 1) {
			if (nameLength < patternLength-1) return FuzzyBoolean.NO;
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns[patternIndex++];
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength-patternIndex);
				} else {
					FuzzyBoolean ret = p.matches(types[nameIndex++], kind);
				    if (ret == FuzzyBoolean.NO) return ret;
				    if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
				}
			}
			return finalReturn;
		} else {
//            System.err.print("match(" + arguments + ", " + types + ") -> ");
            FuzzyBoolean b =  outOfStar(typePatterns, types, 0, 0, patternLength - ellipsisCount, nameLength, ellipsisCount, kind);
//            System.err.println(b);
            return b;
    	}
    }
    
    private static FuzzyBoolean outOfStar(final TypePattern[] pattern, final ResolvedTypeX[] target, 
                                                  int           pi,            int    ti, 
                                                  int           pLeft,         int    tLeft,
                                           final int           starsLeft, TypePattern.MatchKind kind) {
        if (pLeft > tLeft) return FuzzyBoolean.NO;
        FuzzyBoolean finalReturn = FuzzyBoolean.YES;
        while (true) {
            // invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length) 
            if (tLeft == 0) return finalReturn;
            if (pLeft == 0) {
                if (starsLeft > 0) {
                    return finalReturn;
                } else {
                    return FuzzyBoolean.NO;
                }
            }
            if (pattern[pi] == TypePattern.ELLIPSIS) {
                return inStar(pattern, target, pi+1, ti, pLeft, tLeft, starsLeft-1, kind);
            }
            FuzzyBoolean ret = pattern[pi].matches(target[ti], kind);
            if (ret == FuzzyBoolean.NO) return ret;
            if (ret == FuzzyBoolean.MAYBE) finalReturn = ret;
            pi++; ti++; pLeft--; tLeft--;
        }
    }    
    private static FuzzyBoolean inStar(final TypePattern[] pattern, final ResolvedTypeX[] target, 
                                               int           pi,             int    ti, 
                                         final int           pLeft,         int    tLeft,
                                               int    starsLeft,     TypePattern.MatchKind kind) {
        // invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
        TypePattern patternChar = pattern[pi];
        while (patternChar == TypePattern.ELLIPSIS) {
            starsLeft--;
            patternChar = pattern[++pi];
        }
        while (true) {
            // invariant: if (tLeft > 0) then (ti < target.length)
            if (pLeft > tLeft) return FuzzyBoolean.NO;
            FuzzyBoolean ff = patternChar.matches(target[ti], kind);
            if (ff.maybeTrue()) {
                FuzzyBoolean xx = outOfStar(pattern, target, pi+1, ti+1, pLeft-1, tLeft-1, starsLeft, kind);
                if (xx.maybeTrue()) return ff.and(xx);
            } 
            ti++; tLeft--;
        }
    }

    
    
    private static FuzzyBoolean outOfStar(final TypePattern[] pattern,
			final Class[] target, int pi, int ti, int pLeft, int tLeft,
			final int starsLeft, TypePattern.MatchKind kind) {
		if (pLeft > tLeft)
			return FuzzyBoolean.NO;
		FuzzyBoolean finalReturn = FuzzyBoolean.YES;
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length && pi <
			// pattern.length)
			if (tLeft == 0)
				return finalReturn;
			if (pLeft == 0) {
				if (starsLeft > 0) {
					return finalReturn;
				} else {
					return FuzzyBoolean.NO;
				}
			}
			if (pattern[pi] == TypePattern.ELLIPSIS) {
				return inStar(pattern, target, pi + 1, ti, pLeft, tLeft,
						starsLeft - 1, kind);
			}
			FuzzyBoolean ret = pattern[pi].matches(target[ti], kind);
			if (ret == FuzzyBoolean.NO)
				return ret;
			if (ret == FuzzyBoolean.MAYBE)
				finalReturn = ret;
			pi++;
			ti++;
			pLeft--;
			tLeft--;
		}
	}       
  
    private static FuzzyBoolean inStar(final TypePattern[] pattern,
			final Class[] target, int pi, int ti, final int pLeft,
			int tLeft, int starsLeft, TypePattern.MatchKind kind) {
		// invariant: pLeft > 0, so we know we'll run out of stars and find a
		// real char in pattern
		TypePattern patternChar = pattern[pi];
		while (patternChar == TypePattern.ELLIPSIS) {
			starsLeft--;
			patternChar = pattern[++pi];
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length)
			if (pLeft > tLeft)
				return FuzzyBoolean.NO;
			FuzzyBoolean ff = patternChar.matches(target[ti], kind);
			if (ff.maybeTrue()) {
				FuzzyBoolean xx = outOfStar(pattern, target, pi + 1, ti + 1,
						pLeft - 1, tLeft - 1, starsLeft, kind);
				if (xx.maybeTrue())
					return ff.and(xx);
			}
			ti++;
			tLeft--;
		}
	}

    private static FuzzyBoolean outOfStar(final TypePattern[] pattern,
			final Object[] target, int pi, int ti, int pLeft, int tLeft,
			final int starsLeft, TypePattern.MatchKind kind) {
		if (pLeft > tLeft)
			return FuzzyBoolean.NO;
		FuzzyBoolean finalReturn = FuzzyBoolean.YES;
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length && pi <
			// pattern.length)
			if (tLeft == 0)
				return finalReturn;
			if (pLeft == 0) {
				if (starsLeft > 0) {
					return finalReturn;
				} else {
					return FuzzyBoolean.NO;
				}
			}
			if (pattern[pi] == TypePattern.ELLIPSIS) {
				return inStar(pattern, target, pi + 1, ti, pLeft, tLeft,
						starsLeft - 1,kind);
			}
			FuzzyBoolean ret = pattern[pi].matches(target[ti],kind);
			if (ret == FuzzyBoolean.NO)
				return ret;
			if (ret == FuzzyBoolean.MAYBE)
				finalReturn = ret;
			pi++;
			ti++;
			pLeft--;
			tLeft--;
		}
	}       
  
    private static FuzzyBoolean inStar(final TypePattern[] pattern,
			final Object[] target, int pi, int ti, final int pLeft,
			int tLeft, int starsLeft, TypePattern.MatchKind kind) {
		// invariant: pLeft > 0, so we know we'll run out of stars and find a
		// real char in pattern
		TypePattern patternChar = pattern[pi];
		while (patternChar == TypePattern.ELLIPSIS) {
			starsLeft--;
			patternChar = pattern[++pi];
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length)
			if (pLeft > tLeft)
				return FuzzyBoolean.NO;
			FuzzyBoolean ff = patternChar.matches(target[ti],kind);
			if (ff.maybeTrue()) {
				FuzzyBoolean xx = outOfStar(pattern, target, pi + 1, ti + 1,
						pLeft - 1, tLeft - 1, starsLeft,kind);
				if (xx.maybeTrue())
					return ff.and(xx);
			}
			ti++;
			tLeft--;
		}
	}
  

    
	public TypePatternList resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		for (int i=0; i<typePatterns.length; i++) {
			TypePattern p = typePatterns[i];
			if (p != null) {
				typePatterns[i] = typePatterns[i].resolveBindings(scope, bindings, allowBinding, requireExactType);
			}
		}
		return this;
	}
	
	public TypePatternList resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
		for (int i=0; i<typePatterns.length; i++) {
			TypePattern p = typePatterns[i];
			if (p != null) {
				typePatterns[i] = typePatterns[i].resolveBindingsFromRTTI(allowBinding, requireExactType);
			}
		}
		return this;		
	}
	
	public TypePatternList resolveReferences(IntMap bindings) {
		int len = typePatterns.length;
		TypePattern[] ret = new TypePattern[len];
		for (int i=0; i < len; i++) {
			ret[i] = typePatterns[i].remapAdviceFormals(bindings);
		}
		return new TypePatternList(ret);
	}

	public void postRead(ResolvedTypeX enclosingType) {
		for (int i=0; i<typePatterns.length; i++) {
			TypePattern p = typePatterns[i];
			p.postRead(enclosingType);
		}
	}
	

	public boolean equals(Object other) {
		if (!(other instanceof TypePatternList)) return false;
		TypePatternList o = (TypePatternList)other;
		int len = o.typePatterns.length;
		if (len != this.typePatterns.length) return false;
		for (int i=0; i<len; i++) {
			if (!this.typePatterns[i].equals(o.typePatterns[i])) return false;
		}
		return true;
	}
    public int hashCode() {
        int result = 41;
        for (int i = 0, len = typePatterns.length; i < len; i++) {
            result = 37*result + typePatterns[i].hashCode();
        }
        return result;
    }
    

	public static TypePatternList read(VersionedDataInputStream s, ISourceContext context) throws IOException  {
		short len = s.readShort();
		TypePattern[] arguments = new TypePattern[len];
		for (int i=0; i<len; i++) {
			arguments[i] = TypePattern.read(s, context);
		}
		TypePatternList ret = new TypePatternList(arguments);
		ret.readLocation(context, s);
		return ret;
	}


	public void write(DataOutputStream s) throws IOException {
		s.writeShort(typePatterns.length);
		for (int i=0; i<typePatterns.length; i++) {
			typePatterns[i].write(s);
		}
		writeLocation(s);
	}
    public TypePattern[] getTypePatterns() {
        return typePatterns;
    }

	public Collection getExactTypes() {
		ArrayList ret = new ArrayList();
		for (int i=0; i<typePatterns.length; i++) {
			TypeX t = typePatterns[i].getExactType();
			if (t != ResolvedTypeX.MISSING) ret.add(t);
		}
		return ret;
	}

}
