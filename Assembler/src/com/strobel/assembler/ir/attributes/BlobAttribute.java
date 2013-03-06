/*
 * BlobAttribute.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.ir.attributes;

import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class BlobAttribute extends SourceAttribute {
    private final byte[] _data;

    public BlobAttribute(final String name, final byte[] data) {
        super(name, data.length);
        _data = VerifyArgument.notNull(data, "data");
    }

    public byte[] getData() {
        return _data;
    }
}