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
package org.codehaus.groovy.eclipse.adapters;

import org.codehaus.groovy.eclipse.core.adapters.GroovyFileAdapterFactory;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorInput;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GroovyEditorAdapterFactory implements IAdapterFactory {

    public Class[] getAdapterList() {
        return new GroovyFileAdapterFactory().getAdapterList();
    }

    public Object getAdapter(Object adaptable, Class adapterType) {
        if (!(adaptable instanceof GroovyEditor)) {
            throw new IllegalArgumentException("adaptable is not the GroovyEditor");
        }

        IEditorInput editorInput = ((GroovyEditor) adaptable).getEditorInput();

        // delegate to GroovyIFileEditorInputAdapterFactory?
        Object adapter = editorInput.getAdapter(adapterType);
        if (adapter != null) {
            return adapter;
        }

        // delegate to GroovyFileAdapterFactory?
        @SuppressWarnings("cast")
        IFile file = (IFile) editorInput.getAdapter(IFile.class);
        if (file != null) {
            return file.getAdapter(adapterType);
        }

        return null;
    }
}
