/* Generated By:JJTree: Do not edit this line. D:/aw/cvs_bp/backport175/src/main/org/codehaus/backport175/compiler/parser/ast\AnnotationParserVisitor.java */

package org.codehaus.backport175.compiler.parser.ast;

public interface AnnotationParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTRoot node, Object data);
  public Object visit(ASTAnnotation node, Object data);
  public Object visit(ASTKeyValuePair node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTBoolean node, Object data);
  public Object visit(ASTChar node, Object data);
  public Object visit(ASTString node, Object data);
  public Object visit(ASTArray node, Object data);
  public Object visit(ASTInteger node, Object data);
  public Object visit(ASTFloat node, Object data);
  public Object visit(ASTHex node, Object data);
  public Object visit(ASTOct node, Object data);
}
