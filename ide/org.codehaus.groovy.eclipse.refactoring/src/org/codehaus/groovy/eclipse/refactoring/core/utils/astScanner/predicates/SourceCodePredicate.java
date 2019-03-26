/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * https://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;

public class SourceCodePredicate implements IASTNodePredicate {

	int line, col;
	
	public SourceCodePredicate(int line, int col) {
		this.line = line;
		this.col = col;
	}

	public ASTNode evaluate(ASTNode input) {
		if (/*!(input instanceof BlockStatement) &&*/ // ignore block statements because we are really interested in the statements that it contains.
				input.getLineNumber() == line && input.getColumnNumber() == col)
			return input;
		return null;
	}

}
