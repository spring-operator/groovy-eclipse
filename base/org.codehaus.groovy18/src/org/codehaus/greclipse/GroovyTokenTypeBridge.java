/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.greclipse;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

/**
 * This class contains copies of the {@link GroovyTokenTypes} constants in static non-final
 * variables. The reason for this is that we must ensure the contants do not get inlined into
 * greclipse bytecode anywhere outside of the org.codehaus.groovy, org.codehaus.groovy16 and
 * org.codehaus.groovy18 bundles.
 * <p>
 * If those constants where inlined, this would create trouble since the values for the constants
 * are generated by the antlr parser generator and they tend to differ between version. This will
 * break things that uses constants inlined from one version while trying to run against another
 * version.
 *
 * @author Kris De Volder
 */
public class GroovyTokenTypeBridge {

    // Only the GroovyTokenTypes that we use inside Greclipse are represented here.
    // More can be added on an as needed basis.

    public static int IDENT             = GroovyTokenTypes.IDENT;
    public static int LBRACK            = GroovyTokenTypes.LBRACK;
    public static int LCURLY            = GroovyTokenTypes.LCURLY;
    public static int LPAREN            = GroovyTokenTypes.LPAREN;
    public static int NLS               = GroovyTokenTypes.NLS;
    public static int RPAREN            = GroovyTokenTypes.RPAREN;
    public static int STRING_CTOR_START = GroovyTokenTypes.STRING_CTOR_START;
    public static int WS                = GroovyTokenTypes.WS;
    public static int COMMA             = GroovyTokenTypes.COMMA;
    public static int SEMI              = GroovyTokenTypes.SEMI;
    public static int RCURLY            = GroovyTokenTypes.RCURLY;
    public static int SL_COMMENT        = GroovyTokenTypes.SL_COMMENT;
    public static int CLOSABLE_BLOCK_OP = GroovyTokenTypes.CLOSABLE_BLOCK_OP;
    public static int EOF               = GroovyTokenTypes.EOF;
    public static int LITERAL_if        = GroovyTokenTypes.LITERAL_if;
    public static int LITERAL_else      = GroovyTokenTypes.LITERAL_else;
    public static int LITERAL_for       = GroovyTokenTypes.LITERAL_for;
    public static int LITERAL_switch    = GroovyTokenTypes.LITERAL_switch;
    public static int LITERAL_while     = GroovyTokenTypes.LITERAL_while;
    public static int RBRACK            = GroovyTokenTypes.RBRACK;
    public static int ML_COMMENT        = GroovyTokenTypes.ML_COMMENT;
    public static int STRING_CTOR_END   = GroovyTokenTypes.STRING_CTOR_END;
    public static int LITERAL_as          = GroovyTokenTypes.LITERAL_as;
}
