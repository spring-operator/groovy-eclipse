/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.core.tests.util;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

/**
 * @author Andrew Eisenberg
 * @created Mar 17, 2011
 */
public abstract class GroovyUtils {

    public static final int GROOVY_LEVEL;
    static {
        Version ver = Platform.getBundle("org.codehaus.groovy").getVersion();
        GROOVY_LEVEL = ver.getMajor() * 10 + ver.getMinor();
    }

    public static boolean isGroovy16() {
        return GROOVY_LEVEL == 16;
    }

    public static boolean isGroovy17() {
        return GROOVY_LEVEL == 17;
    }

    public static boolean isGroovy18() {
        return GROOVY_LEVEL == 18;
    }

    public static boolean isGroovy20() {
        return GROOVY_LEVEL == 20;
    }

    public static boolean isGroovy21() {
        return GROOVY_LEVEL == 21;
    }

    public static boolean isGroovy22() {
        return GROOVY_LEVEL == 22;
    }

    public static boolean isAtLeastGroovy(int level) {
        return GROOVY_LEVEL >= level;
    }
}
