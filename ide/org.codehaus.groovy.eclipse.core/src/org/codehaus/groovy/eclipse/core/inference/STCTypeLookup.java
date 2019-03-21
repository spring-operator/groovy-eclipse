/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

public class STCTypeLookup implements ITypeLookup {

    // only enabled for Groovy 2.0 or greater
    private static final boolean isEnabled = (CompilerUtils.getActiveGroovyBundle().getVersion().getMajor() >= 2);

    @Override
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
    }

    @Override
    public TypeLookupResult lookupType(Expression expr, VariableScope scope, ClassNode objectExpressionType) {
        if (isEnabled) {
            ASTNode declaration = expr;
            ClassNode declaringType = objectExpressionType;
            TypeConfidence confidence = TypeConfidence.INFERRED;
            Object inferredType = expr.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);

            if (expr instanceof ClassExpression) {
                declaration = expr.getType();
            } else if (expr instanceof FieldExpression) {
                declaration = ((FieldExpression) expr).getField();
            } else if (expr instanceof VariableExpression) {
                VariableExpression vexp = (VariableExpression) expr;
                if (vexp.isThisExpression() || vexp.isSuperExpression()) {
                    declaration = (ClassNode) inferredType;
                } else {
                    Variable accessedVariable = vexp.getAccessedVariable();
                    if (accessedVariable instanceof DynamicVariable) {
                        // defer to other type lookup impls
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (accessedVariable instanceof ASTNode) {
                        declaration = (ASTNode) accessedVariable;
                        if (inferredType instanceof ClassNode &&
                                VariableScope.isPlainClosure((ClassNode) inferredType)) {
                            VariableInfo info = scope.lookupName(accessedVariable.getName());
                            if (info != null && VariableScope.isParameterizedClosure(info.type)) {
                                inferredType = info.type; // Closure --> Closure<String>
                            }
                        }
                    }
                }
            } else if (expr instanceof ConstantExpression &&
                    (declaration = scope.getEnclosingNode()) instanceof MethodCallExpression &&
                    ((MethodCallExpression) declaration).getMethod() == expr && !(inferredType instanceof ClassNode)) {
                MethodNode target = getMopMethodTarget((MethodCallExpression) declaration);
                if (target != null) {
                    declaration = target;
                    inferredType = target.getReturnType();
                    declaringType = target.getDeclaringClass();
                }
            } else if (expr instanceof MethodCallExpression ||
                    expr instanceof StaticMethodCallExpression || expr instanceof ConstructorCallExpression ||
                    !(inferredType instanceof ClassNode) && // check for VariableExpressionTransformer's substitution
                    expr instanceof ConstantExpression && (declaration = scope.getEnclosingNode()) instanceof PropertyExpression) {
                Object target = declaration.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                if (target instanceof MethodNode) {
                    declaration = (MethodNode) target;
                    declaringType = ((MethodNode) target).getDeclaringClass();
                    if (!(inferredType instanceof ClassNode) && !(expr instanceof ConstructorCallExpression)) {
                        inferredType = ((MethodNode) target).getReturnType();
                    }
                } else {
                    // defer to other type lookup impls
                    confidence = TypeConfidence.UNKNOWN;
                }
            }

            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, declaringType, declaration, confidence, scope);
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        if (isEnabled) {
            Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        if (isEnabled) {
            Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * Resolves {@code this.super$2$method()} to {@code super.method()}
     *
     * @see MethodCallExpressionTransformer.transformToMopSuperCall
     */
    private static MethodNode getMopMethodTarget(MethodCallExpression call) {
        if (call.isImplicitThis() && MopWriter.isMopMethod(call.getMethodAsString()) &&
                call.getObjectExpression().getNodeMetaData(ClassCodeVisitorSupport.ORIGINAL_EXPRESSION) != null) {
            Matcher m = Pattern.compile("super\\$(\\d+)\\$(.+)").matcher(call.getMethodAsString());
            if (m.matches()) {
                int dist = Integer.parseInt(m.group(1));
                List<ClassNode> types = new ArrayList<>();
                for (ClassNode next = call.getMethodTarget().getDeclaringClass(); next != null; next = next.getSuperClass()) {
                    types.add(next);
                }

                String name = m.group(2);
                ClassNode type = types.get(types.size() - dist);
                return type.getMethod(name, call.getMethodTarget().getParameters());
            }
        }
        return null;
    }
}
