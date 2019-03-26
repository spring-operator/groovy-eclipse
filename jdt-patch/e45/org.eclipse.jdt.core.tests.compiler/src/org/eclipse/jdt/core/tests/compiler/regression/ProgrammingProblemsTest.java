/*******************************************************************************
 * Copyright (c) 2001, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						bug 185682 - Increment/decrement operators mark local variables as read
 *     						bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/* Collects potential programming problems tests that are not segregated in a
 * dedicated test class (aka NullReferenceTest). */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProgrammingProblemsTest extends AbstractRegressionTest {

public ProgrammingProblemsTest(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test0055" };
//		TESTS_NUMBERS = new int[] { 56 };
//  	TESTS_RANGE = new int[] { 1, -1 };
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
    return ProgrammingProblemsTest.class;
}
protected Map getCompilerOptions() {
	Map compilerOptions = super.getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal,  CompilerOptions.OPTIMIZE_OUT);
	return compilerOptions;
}
void runTest(
		String[] testFiles,
		String[] errorOptions,
		String[] warningOptions,
		String[] ignoreOptions,
		boolean expectingCompilerErrors,
		String expectedCompilerLog,
		String expectedOutputString,
		boolean forceExecution,
		String[] classLib,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
	Map compilerOptions = customOptions;
	if (errorOptions != null || warningOptions != null ||
			ignoreOptions != null) {
		if (compilerOptions == null) {
			compilerOptions = new HashMap();
		}
		if (errorOptions != null) {
		    for (int i = 0; i < errorOptions.length; i++) {
		    	compilerOptions.put(errorOptions[i], CompilerOptions.ERROR);
		    }
		}
		if (warningOptions != null) {
		    for (int i = 0; i < warningOptions.length; i++) {
		    	compilerOptions.put(warningOptions[i], CompilerOptions.WARNING);
		    }
		}
		if (ignoreOptions != null) {
		    for (int i = 0; i < ignoreOptions.length; i++) {
		    	compilerOptions.put(ignoreOptions[i], CompilerOptions.IGNORE);
		    }
		}
	}
	runTest(testFiles,
		expectingCompilerErrors,
		expectedCompilerLog,
		expectedOutputString,
		"" /* expectedErrorString */,
		forceExecution,
		classLib,
		shouldFlushOutputDirectory,
		vmArguments,
		compilerOptions,
		clientRequestor,
		skipJavac);
}

// default behavior upon unread parameters
public void test0001_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unread paramaters as warning
public void test0002_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disables by default
public void test0003_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @param b mute warning **/\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disabling can be disabled
public void test0004_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @param b mute warning **/\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using SuppressWarnings
public void test0005_unread_parameters() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"unused\")\n" + // most specific token
				"  public void foo(boolean b) {\n" +
				"  }\n" +
				"@SuppressWarnings(\"all\")\n" + // least specific token
				"  public void foo(int i) {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedParameter
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unread paramaters as error
public void test0006_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// default behavior upon unnecessary declaration of thrown checked exceptions
public void test0007_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unnecessary declaration of thrown checked exceptions as warning
public void test0008_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default
public void test0009_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"/** @throws IOException mute warning **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disabling can be disabled
public void test0010_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"/** @throws IOException mute warning **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using SuppressWarnings
public void test0011_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"@SuppressWarnings(\"all\")\n" + // no specific token
				"  public void foo() throws IOException {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unnecessary declaration of thrown checked exceptions as error
public void test0012_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default, but only exact matches work
public void test0013_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.io.EOFException;\n" +
			"public class X {\n" +
			"/** @throws EOFException does not mute warning for IOException **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
public void test0014_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo(int unused) throws IOException {}\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	             ^^^^^^\n" +
		"The value of the parameter unused is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	                            ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
// variant: both warnings show up
public void test0015_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo(int unused) throws IOException {}\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException,
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	             ^^^^^^\n" +
		"The value of the parameter unused is not used\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	                            ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// reporting unread paramaters as error on a constructor
public void test0016_unread_parameters_constructor() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public X(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public X(boolean b) {\n" +
		"	                 ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=208001
public void test0017_shadowing_package_visible_methods() {
	runTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n",
			"q/Y.java",
			"package q;\n" +
			"public class Y extends p.X {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n",
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		new ICompilerRequestor() {
			public void acceptResult(CompilationResult result) {
				if (result.compilationUnit.getFileName()[0] == 'Y') {
					assertEquals("unexpected problems count", 1, result.problemCount);
					assertEquals("unexpected category", CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT, result.problems[0].getCategoryID());
				}
			}
		} /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0018_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws ArithmeticException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0019_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws RuntimeException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Exception
public void test0020_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Throwable
public void test0021_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Throwable {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0022_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws ArithmeticException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// the external API uses another string literal - had it wrong in first attempt
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0023_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0024_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws RuntimeException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0025_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0026_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Throwable {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Throwable {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Throwable is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0027_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Exception mute warning **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disabling can be disabled
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0028_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Exception mute warning **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using SuppressWarnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0029_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = new HashMap();
		customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
				CompilerOptions.DISABLED);
		runTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"all\")\n" + // no specific token
				"  public void foo() throws Exception {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			customOptions,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as error
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the error for unchecked exceptions, using Exception instead
public void test0030_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default, but only exact matches work
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0031_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Throwable does not mute warning for Exception **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions
public void test0032_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Error {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0033_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"    if (bar()) {\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216897
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0034_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static final class MyError extends Error {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws Throwable {\n" +
			"    try {\n" +
			"      bar();\n" +
			"    } catch (MyError e) {\n" +
			"    }\n" +
			"  }\n" +
			"  private void bar() {}\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	public void foo() throws Throwable {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Throwable is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0035_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static final class MyError extends Error {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws Throwable {\n" +
			"    throw new MyError();\n" +
			"  }\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0036_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static class E1 extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public static class E2 extends E1 {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws E1 {\n" +
			"    throw new E2();\n" +
			"  }\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=115814
public void test0037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = args == args;\n" +
			"		boolean b2 = args != args;\n" +
			"		boolean b3 = b1 == b1;\n" +
			"		boolean b4 = b1 != b1;\n" +
			"		boolean b5 = b1 && b1;\n" +
			"		boolean b6 = b1 || b1;\n" +
			"		\n" +
			"		boolean b7 = foo() == foo();\n" +
			"		boolean b8 = foo() != foo();\n" +
			"		boolean b9 = foo() && foo();\n" +
			"		boolean b10 = foo() || foo();\n" +
			"	}\n" +
			"	static boolean foo() { return true; }\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b1 = args == args;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	boolean b2 = args != args;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	boolean b3 = b1 == b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	boolean b4 = b1 != b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 7)\n" +
			"	boolean b5 = b1 && b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 8)\n" +
			"	boolean b6 = b1 || b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 16)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276740"
 */
public void test0038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = 1 == 1;\n" +
			"		boolean b2 = 1 != 1;\n" +
			"		boolean b3 = 1 == 1.0;\n" +
			"		boolean b4 = 1 != 1.0;\n" +
			"		boolean b5 = 1 == 2;\n" +
			"		boolean b6 = 1 != 2;\n" +
			"		boolean b7 = 1 == 2.0;\n" +
			"		boolean b8 = 1 != 2.0;\n" +
			"       final short s1 = 1;\n" +
			"       final short s2 = 2;\n" +
			"       boolean b9 = 1 == s1;\n" +
			"       boolean b10 = 1 == s2;\n" +
			"       boolean b91 = 1 != s1;\n" +
			"       boolean b101 = 1 != s2;\n" +
			"       final long l1 = 1;\n" +
			"       final long l2 = 2;\n" +
			"       boolean b11 = 1 == l1;\n" +
			"       boolean b12 = 1 == l2;\n" +
			"       boolean b111 = 1 != l1;\n" +
			"       boolean b121 = 1 != l2;\n" +
			"       boolean b13 = s1 == l1;\n" +
			"       boolean b14 = s1 == l2;\n" +
			"       boolean b15 = s1 != l1;\n" +
			"       boolean b16 = s1 != l2;\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	boolean b1 = 1 == 1;\n" + 
			"	             ^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	boolean b2 = 1 != 1;\n" + 
			"	             ^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 5)\n" + 
			"	boolean b3 = 1 == 1.0;\n" + 
			"	             ^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 6)\n" + 
			"	boolean b4 = 1 != 1.0;\n" + 
			"	             ^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 13)\n" + 
			"	boolean b9 = 1 == s1;\n" + 
			"	             ^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 15)\n" + 
			"	boolean b91 = 1 != s1;\n" + 
			"	              ^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 19)\n" + 
			"	boolean b11 = 1 == l1;\n" + 
			"	              ^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 21)\n" + 
			"	boolean b111 = 1 != l1;\n" + 
			"	               ^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 23)\n" + 
			"	boolean b13 = s1 == l1;\n" + 
			"	              ^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"10. WARNING in X.java (at line 25)\n" + 
			"	boolean b15 = s1 != l1;\n" + 
			"	              ^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 28)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276741"
 */
public void test0039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void gain(String[] args) {\n" +
			"		boolean b1 = this == this;\n" +
			"		boolean b2 = this != this;\n" +		
			"		boolean b3 = this != new X();\n" +
			"		boolean b4 = this == new X();\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	boolean b1 = this == this;\n" + 
			"	             ^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	boolean b2 = this != this;\n" + 
			"	             ^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281776"
 * We now tolerate comparison of float and double entities against
 * themselves as a legitimate idiom for NaN checking. 
 */
public void test0040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
		    "    public static void main(String[] args) {\n" +
		    "        double var = Double.NaN;\n" +
		    "            if(var != var) {\n" +
		    "                  System.out.println(\"NaN\");\n" +
		    "            }\n" +
		    "            float varf = 10;\n" +
		    "            if(varf != varf) {\n" +
		    "            	System.out.println(\"NaN\");\n" +
		    "            }\n" +
		    "   }\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251227
public void test0041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(1.0 == 1.0);\n" +
			"		System.out.println(1.0f == 1.0f);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	System.out.println(1.0 == 1.0);\n" + 
		"	                   ^^^^^^^^^^\n" + 
		"Comparing identical expressions\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	System.out.println(1.0f == 1.0f);\n" + 
		"	                   ^^^^^^^^^^^^\n" + 
		"Comparing identical expressions\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248897
public void test0042() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	runTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"    public static void main(String[]  args) {\n" +
				"        final String var = \"Hello\";\n" +
				"        final int local = 10;\n" +
				"        @ZAnn(var + local)\n" +
				"        class X {}\n" +
				"        new X();\n" +
				"    }\n" +
				"}\n" +
				"@interface ZAnn {\n" +
				"    String value();\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedLocal
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313825
public void test0043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	void foo(int i) {\n" + 
			"		foo((a));\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	foo((a));\n" + 
		"	     ^\n" + 
		"a cannot be resolved to a variable\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"   volatile int x;\n" +
			"   int nvx;\n" +
			"	void foo(int i) {\n" +
			"		x = x;\n" + 
			"       nvx = nvx;\n" +
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	nvx = nvx;\n" + 
		"	^^^^^^^^^\n" + 
		"The assignment to variable nvx has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"   volatile int x = this.x;\n" +
			"   int nvx = this.nvx;\n" +
			"	void foo(int i) {\n" +
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	volatile int x = this.x;\n" + 
		"	             ^^^^^^^^^^\n" + 
		"The assignment to variable x has no effect\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 3)\n" + 
		"	int nvx = this.nvx;\n" + 
		"	    ^^^^^^^^^^^^^^\n" + 
		"The assignment to variable nvx has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0046() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    int foo() {\n" + 
				"        int i=1;\n" + 
				"        boolean b=false;\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        int k = 2;\n" + 
				"        --k;\n" + 				// not a relevant usage
				"        k+=3;\n" + 			// not a relevant usage
				"        Integer j = 3;\n" + 
				"        j++;\n" + 				// relevant because unboxing is involved
				"        i++;\n" +				// not relevant but should still not report because next is relevant
				"        return i++;\n" + 		// value after increment is used
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	boolean b=false;\n" + 
			"	        ^\n" + 
			"The value of the local variable b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 6)\n" + 
			"	int k = 2;\n" + 
			"	    ^\n" + 
			"The value of the local variable k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals
public void test0046_field() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    private int i=1;\n" + 
				"    private boolean b=false;\n" + 
				"    private int k = 2;\n" + 
				"    private Integer j = 3;\n" + 
				"    int foo() {\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        --k;\n" + 				// not a relevant usage
				"        k+=3;\n" + 			// not a relevant usage
				"        j++;\n" + 				// relevant because unboxing is involved
				"        return i++;\n" + 		// value after increment is used
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	private boolean b=false;\n" + 
			"	                ^\n" + 
			"The value of the field X.b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	private int k = 2;\n" + 
			"	            ^\n" + 
			"The value of the field X.k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - this-qualified access
public void test0046_field_this_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    private int i=1;\n" + 
				"    private boolean b=false;\n" + 
				"    private int k = 2;\n" + 
				"    private Integer j = 3;\n" + 
				"    int foo() {\n" + 
				"        this.b|=true;\n" + 		// not a relevant usage
				"        --this.k;\n" + 			// not a relevant usage
				"        getThis().k+=3;\n" + 		// not a relevant usage
				"        this.j++;\n" + 			// relevant because unboxing is involved
				"        return this.i++;\n" + 		// value after increment is used
				"    }\n" +
				"    X getThis() { return this; }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	private boolean b=false;\n" + 
			"	                ^\n" + 
			"The value of the field X.b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	private int k = 2;\n" + 
			"	            ^\n" + 
			"The value of the field X.k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - regular qualified access
public void test0046_field_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    private int i=1;\n" + 
				"    private boolean b=false;\n" + 
				"    private int k = 2;\n" + 
				"    private Integer j = 3;\n" + 
				"    int foo(X that) {\n" + 
				"        that.b|=true;\n" + 		// not a relevant usage
				"        --that.k;\n" + 			// not a relevant usage
				"        that.k+=3;\n" + 			// not a relevant usage
				"        that.j++;\n" + 			// relevant because unboxing is involved
				"        that.i++;\n"+				// not relevant but should still not report because next is relevant
				"        return that.i++;\n" + 		// value after increment is used
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	private boolean b=false;\n" + 
			"	                ^\n" + 
			"The value of the field X.b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	private int k = 2;\n" + 
			"	            ^\n" + 
			"The value of the field X.k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with fields inside a private type
public void test0046_field_in_private_type() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    private class Y {\n" + 
				"        int i=1;\n" + 
				"        public boolean b=false;\n" + 
				"        protected int k = 2;\n" + 
				"        Integer j = 3;\n" +
				"    }\n" + 
				"    int foo(Y y) {\n" + 
				"        y.b|=true;\n" + 				// not a relevant usage
				"        --y.k;\n" + 					// not a relevant usage
				"        y.k+=3;\n" + 					// not a relevant usage
				"        y.j++;\n" + 					// relevant because unboxing is involved
				"        int result = y.i++;\n" + 	// value after increment is used
				"        y.i++;\n" +					// not relevant, but previous is
				"        return result;\n" +
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	public boolean b=false;\n" + 
			"	               ^\n" + 
			"The value of the field X.Y.b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	protected int k = 2;\n" + 
			"	              ^\n" + 
			"The value of the field X.Y.k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0047() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    void foo(int param1, int param2, Integer param3) {\n" + 
				"        boolean b=false;\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        {\n" +
				"            int val=23;\n" +
				"            param2 += val;\n" +// not a relevant usage of param2
				"        }\n" +
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	void foo(int param1, int param2, Integer param3) {\n" + 
			"	             ^^^^^^\n" + 
			"The value of the parameter param1 is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	void foo(int param1, int param2, Integer param3) {\n" + 
			"	                         ^^^^^^\n" + 
			"The value of the parameter param2 is not used\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	boolean b=false;\n" + 
			"	        ^\n" + 
			"The value of the local variable b is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an implementing method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract is disabled
public void test0048() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends A implements Y{\n" + 
				"   public void foo(int param1, int param2, Integer param3) {\n" + // implementing method, so dont warn
				"        boolean b=false;\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" + 
				"   public void foo(int param1, int param2) {\n" + // warn
				"        boolean b=false;\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"    }\n" +
				"   public void bar(int param1, int param2, Integer param3) {\n" + // implementing method, so dont warn
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"}\n" +
				"interface Y{\n" +
				"	public void foo(int param1, int param2, Integer param3);" +
				"}\n" +
				"abstract class A{\n" +
				"	public abstract void bar(int param1, int param2, Integer param3);" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	boolean b=false;\n" + 
			"	        ^\n" + 
			"The value of the local variable b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	public void foo(int param1, int param2) {\n" + 
			"	                    ^^^^^^\n" + 
			"The value of the parameter param1 is not used\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 9)\n" + 
			"	public void foo(int param1, int param2) {\n" + 
			"	                                ^^^^^^\n" + 
			"The value of the parameter param2 is not used\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\n" + 
			"	boolean b=false;\n" + 
			"	        ^\n" + 
			"The value of the local variable b is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an overriding method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete is disabled
public void test0049() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends A {\n" + 
				"   public void foo(int param1, int param2, Integer param3) {\n" + // overriding method, so dont warn
				"        boolean b=false;\n" + 
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" + 
				"   public void foo(int param1, Integer param3) {\n" + // overriding method, so dont warn
				"        param1++;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" + 
				"}\n" +
				"class A{\n" +
				"   public void foo(int param1, int param2, Integer param3) {\n" +
				"        param1 -=1;\n" + 		// not a relevant usage
				"        param2--;\n" + 		// not a relevant usage
				"        param3--;\n" + 		// relevant because unboxing is involved
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	boolean b=false;\n" + 
			"	        ^\n" + 
			"The value of the local variable b is not used\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	public void foo(int param1, Integer param3) {\n" + 
			"	                    ^^^^^^\n" + 
			"The value of the parameter param1 is not used\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 15)\n" + 
			"	public void foo(int param1, int param2, Integer param3) {\n" + 
			"	                    ^^^^^^\n" + 
			"The value of the parameter param1 is not used\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 15)\n" + 
			"	public void foo(int param1, int param2, Integer param3) {\n" + 
			"	                                ^^^^^^\n" + 
			"The value of the parameter param2 is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused local warning is not shown for locals declared in unreachable code
public void test0050() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"    int foo() {\n" + 
				"        int i=1;\n" +
				"		 if (false) {\n" + 
				"        	boolean b=false;\n" + // don't complain as unused
				"        	b|=true;\n" +
				"		 }\n" + 			// not a relevant usage
				"        int k = 2;\n" + 
				"        --k;\n" + 			// not a relevant usage
				"        k+=3;\n" + 		// not a relevant usage
				"        Integer j = 3;\n" + 
				"        j++;\n" + 			// relevant because unboxing is involved
				"        return i++;\n" + 	// value after increment is used
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (false) {\n" + 
			"        	boolean b=false;\n" + 
			"        	b|=true;\n" + 
			"		 }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	int k = 2;\n" + 
			"	    ^\n" + 
			"The value of the local variable k is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that a constructor argument is handled correctly
public void test0051() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X {\n" + 
					"    X(int abc) {\n" + 
					"        abc++;\n" +    // not a relevant usage
					"    }\n" + 
					"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	X(int abc) {\n" + 
			"	      ^^^\n" + 
			"The value of the parameter abc is not used\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
public void test0052() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runConformTest(
			new String[] {
					"X.java",
					"class X {\n" +
					"    Y y = new Y();\n" +
					"    private class Y {\n" +
					"        int abc;\n" + 
					"        Y() {\n" + 
					"            abc++;\n" +    // not a relevant usage
					"        }\n" +
					"    }\n" +
					"    class Z extends Y {}\n" + // makes 'abc' externally accessible
					"}"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// multi-level inheritance
public void test0052a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"class Outer {\n" + 
					"    private class Inner1 {\n" + 
					"        int foo;\n" +
					"    }\n" + 
					"    private class Inner2 extends Inner1 { }\n" + 
					"    class Inner3 extends Inner2 { }\n" +  // foo is exposed here
					"}\n"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// member type of private
public void test0052b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"class Outer {\n" + 
					"    private class Inner1 {\n" + 
					"        class Foo{}\n" +
					"    }\n" + 
					"    private class Inner2 extends Inner1 { }\n" + 
					"    class Inner3 extends Inner2 { }\n" +  // Foo is exposed here
					"}\n"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0053() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"    int foo() {\n" + 
			"        int i=1;\n" +
			"        i++;\n" + 	// value after increment is still not used
			"        return 0;\n" + 
			"    }\n" + 
			"}"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"  // Method descriptor #15 ()I\n" + 
		"  // Stack: 1, Locals: 1\n" + 
		"  int foo();\n" + 
		"    0  iconst_0\n" + 
		"    1  ireturn\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 5]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 2] local: this index: 0 type: X\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0054() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"    int foo() {\n" + 
			"        int i=1;\n" +
			"        return i+=1;\n" + 	// value is used as it is returned
			"    }\n" + 
			"}"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"  // Method descriptor #15 ()I\n" + 
		"  // Stack: 1, Locals: 2\n" + 
		"  int foo();\n" + 
		"    0  iconst_1\n" + 
		"    1  istore_1 [i]\n" + 
		"    2  iinc 1 1 [i]\n" + 
		"    5  iload_1 [i]\n" + 
		"    6  ireturn\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 2, line: 4]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" + 
		"        [pc: 2, pc: 7] local: i index: 1 type: int\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=329613
// regression caused by https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0055() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runNegativeTest(
			new String[] {
					"test1/E.java",
					"package test1;\n" +
					"public class E {\n" +
						"    private void foo() {\n" +
						"        int a= 10;\n" +
						"        a++;\n" +
						"        a--;\n" +
						"        --a;\n" +
						"        ++a;\n" +
						"        for ( ; ; a++) {\n" +
							"        }\n" +
							"    }\n" +
							"}"
			},
			"----------\n" +
			"1. WARNING in test1\\E.java (at line 4)\n" +
			"	int a= 10;\n" +
			"	    ^\n" +
			"The value of the local variable a is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0056() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    static int foo() {\n" + 
			"        int i = 2;\n" + 
			"        int j = 3;\n" + 
			"        return (i += j *= 3);\n" + 	// value is used as it is returned
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"        System.out.println(foo());\n" + 
			"    }\n" + 
			"}"
		},
		"11",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0057() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main (String args[]) {\n" + 
			"        int i = 0;\n" + 
			"        i += 4 + foo();\n" + 
			"    }\n" + 
			"    public static int foo() {\n" + 
			"    	System.out.println(\"OK\");\n" + 
			"    	return 0;\n" + 
			"    }\n" + 
			"}"
		},
		"OK",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336648
public void _test0058() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    void foo(String m) {\n" +
				"        final String message= m;\n" +
				"        new Runnable() {\n" +
				"            public void run() {\n" +
				"                if (\"x\".equals(message)) {\n" +
				"                    bug(); // undefined method\n" +
				"                }\n" +
				"            }\n" +
				"        }.run();\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	bug(); // undefined method\n" + 
			"	^^^\n" + 
			"The method bug() is undefined for the type new Runnable(){}\n" + 
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339139
// Issue local variable not used warning inside deadcode
public void test0059() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	Object a = null;\n" + 
			"    	if (a != null){\n" + 
			"        	int j = 3;\n" + 
			"        	j++;\n" + 	// value is not used
			"    	}\n" +
			"    	System.out.println(\"OK\");\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	if (a != null){\n" + 
		"        	int j = 3;\n" + 
		"        	j++;\n" + 
		"    	}\n" + 
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 5)\n" + 
		"	int j = 3;\n" + 
		"	    ^\n" + 
		"The value of the local variable j is not used\n" + 
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0060() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"package org.eclipse.jdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 7)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"2. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 9)\n" + 
		"	if (t1 == t2) {\n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"3. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 16)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"4. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 18)\n" + 
		"	if (t1 == t2) {\n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"5. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 28)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0061() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/nonjdt/internal/compiler/lookup/X.java",
			"package org.eclipse.nonjdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0062() throws Exception {
	Map customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"package org.eclipse.jdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0063() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/core/dom/X.java",
			"package org.eclipse.jdt.core.dom;\n" +
			"interface ITypeBinding {\n" +
			"}\n" +
			"class TypeBinding implements ITypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 9)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"2. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 11)\n" + 
		"	if (t1 == t2) {\n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"3. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 18)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"4. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 20)\n" + 
		"	if (t1 == t2) {\n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n" + 
		"5. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 30)\n" + 
		"	if (t1 == t2) { \n" + 
		"	    ^^^^^^^^\n" + 
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" + 
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
}