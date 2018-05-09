/*
 * Copyright (C) 2001-3 Paul Murrell
 * Copyright (c) 1998-2013, The R Core Team
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.R-project.org/Licenses/
 */
package com.oracle.truffle.r.library.fastrGrid;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.r.library.fastrGrid.device.FileGridDevice;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice.DeviceCloseException;
import com.oracle.truffle.r.nodes.builtin.RExternalBuiltinNode;
import com.oracle.truffle.r.runtime.RError.Message;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RNull;

final class LNewPage extends RExternalBuiltinNode {
    static {
        Casts.noCasts(LNewPage.class);
    }

    @Child private LGridDirty gridDirty = new LGridDirty();

    @Override
    public Object call(VirtualFrame frame, RArgsValuesAndNames args) {
        GridDevice device = GridContext.getContext().getCurrentDevice();
        if (GridContext.getContext().getGridState().isDeviceInitialized()) {
            openNewPage(device);
            return RNull.instance;
        }
        // There are some exceptions to the rule that any external call from grid R code is
        // preceded by L_gridDirty call, L_newpage is one of them.
        CompilerDirectives.transferToInterpreter();
        return gridDirty.call(frame, RArgsValuesAndNames.EMPTY);
    }

    @TruffleBoundary
    private void openNewPage(GridDevice device) {
        if (device instanceof FileGridDevice) {
            String path = GridContext.getContext().getGridState().getNextPageFilename();
            try {
                ((FileGridDevice) device).openNewPage(path);
            } catch (DeviceCloseException e) {
                throw error(Message.GENERIC, "Cannot save the image. Details: " + e.getMessage());
            }
        } else {
            device.openNewPage();
        }
    }

    @Override
    protected Object call(RArgsValuesAndNames args) {
        assert false : "should be shadowed by the overload with frame";
        return null;
    }
}
