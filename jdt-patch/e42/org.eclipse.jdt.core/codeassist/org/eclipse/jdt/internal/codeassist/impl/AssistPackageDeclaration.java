/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageDeclaration;

public class AssistPackageDeclaration extends PackageDeclaration {
	private Map infoCache;
	public AssistPackageDeclaration(CompilationUnit parent, String name, Map infoCache) {
		super(parent, name);
		this.infoCache = infoCache;
	}

	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	public IAnnotation getAnnotation(String name) {
		return new AssistAnnotation(this, name, this.infoCache);
	}
}
