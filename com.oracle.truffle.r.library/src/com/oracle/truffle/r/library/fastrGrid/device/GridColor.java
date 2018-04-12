/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.library.fastrGrid.device;

/**
 * Simple color representation, so that the device interface does not have to depend on a specific
 * GUI framework.
 */
public final class GridColor {
    public static final int OPAQUE_ALPHA = 0xff;
    private static final int TRANSPARENT_ALPHA = 0;
    public static final GridColor TRANSPARENT = new GridColor(0, 0, 0, TRANSPARENT_ALPHA);
    public static final GridColor BLACK = new GridColor(0, 0, 0, OPAQUE_ALPHA);

    private final int value;

    private GridColor(int value) {
        this.value = value;
    }

    public GridColor(int red, int green, int blue, int alpha) {
        value = ((alpha & 0xFF) << 24) |
                        ((red & 0xFF) << 16) |
                        ((green & 0xFF) << 8) |
                        (blue & 0xFF);
    }

    public static GridColor fromRawValue(int value) {
        return new GridColor(value);
    }

    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    public int getBlue() {
        return value & 0xFF;
    }

    public int getAlpha() {
        return (value >> 24) & 0xff;
    }

    public int getRawValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GridColor && value == ((GridColor) obj).value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
