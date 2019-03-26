/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Declares a Nd field of type float. Can be used in place of  {@link Field}&lt{@link Float}&gt in order to
 * avoid extra GC overhead.
 */
public class FieldFloat extends BaseField {
	private final Tag tag;

	public FieldFloat(String structName, int fieldNumber) {
		setFieldName("field " + fieldNumber + ", a " + getClass().getSimpleName() + " in struct " + structName); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		this.tag = ModificationLog.createTag("Writing " + getFieldName()); //$NON-NLS-1$
	}

	public float get(Nd nd, long address) {
		Database db = nd.getDB();
		return db.getFloat(address + this.offset);
	}

	public void put(Nd nd, long address, float newValue) {
		Database db = nd.getDB();
		db.getLog().start(this.tag);
		try {
			nd.getDB().putFloat(address + this.offset, newValue);
		} finally {
			db.getLog().end(this.tag);
		}
	}

	@Override
	public int getRecordSize() {
		return Database.FLOAT_SIZE;
	}
}
