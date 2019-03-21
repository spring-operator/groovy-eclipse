/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.junit.Test

final class CodeSelectAttributesTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectOnAttributeName1() {
        String source = '''\
            @SuppressWarnings(value=['rawtypes','unchecked'])
            class C {
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'value')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test
    void testCodeSelectOnAttributeName2() {
        addJavaSource '''\
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface Anno {
              String one();
              String two();
            }
            ''', 'Anno'

        String source = '''\
            @Anno(one='1')
            class C {
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'one')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test
    void testCodeSelectOnAttributeValue1() {
        String source = '''\
            @SuppressWarnings(value=['rawtypes','unchecked'])
            class C {
            }
            '''.stripIndent()

        assertCodeSelect([source], 'rawtypes', '')
    }

    @Test
    void testCodeSelectOnAttributeValue2() {
        addJUnit(4)

        String source = '''\
            import org.junit.Test
            class C {
              @Test(expected=Exception)
              void testSomething() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue2a() {
        addJUnit(4)

        String source = '''\
            import org.junit.Test
            class C {
              @Test(expected=Exception.class)
              void testSomething() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue2b() {
        addJUnit(4)

        String source = '''\
            import org.junit.Test
            class C {
              @Test(expected=java.lang.Exception)
              void testSomething() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue3() {
        String source = '''\
            class C {
              public static final String VALUE = 'rawtypes'
              @SuppressWarnings(value=VALUE)
              def method() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }

    @Test
    void testCodeSelectOnAttributeValue3a() {
        String source = '''\
            class C {
              public static final String VALUE = 'rawtypes'
              @SuppressWarnings(value=C.VALUE)
              def method() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }

    @Test
    void testCodeSelectOnAttributeValue4() {
        addGroovySource '''\
            class Bar {
              public static final String VALUE = 'nls'
            }
            '''.stripIndent(), 'Bar', 'foo'

        String source = '''\
            import static foo.Bar.VALUE
            class C {
              @SuppressWarnings(VALUE)
              def method() {
              }
            }
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }
}
