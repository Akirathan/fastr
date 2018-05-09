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

import java.util.function.Supplier;

import com.oracle.truffle.r.library.fastrGrid.device.GridColor;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice;
import com.oracle.truffle.r.library.fastrGrid.grDevices.FileDevUtils;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.env.REnvironment;

public final class GridState {
    private REnvironment gridEnv;
    private GridDeviceState devState;
    private GridPalette palette;

    /**
     * Current grob being drawn (for determining the list of grobs to search when evaluating a
     * grobwidth/height unit via gPath). May be RNull or RList.
     */
    private Object currentGrob;

    GridState() {
    }

    <T> T runWithoutRecording(Supplier<T> code) {
        boolean recording = isDisplayListOn();
        setIsDisplayListOn(false);
        try {
            return code.get();
        } finally {
            setIsDisplayListOn(recording);
        }
    }

    void setDeviceState(GridDeviceState state) {
        devState = state;
    }

    public int getDevHoldCount() {
        return devState.devHoldCount;
    }

    public int setDevHoldCount(int devHoldCount) {
        devState.devHoldCount = devHoldCount;
        return devHoldCount;
    }

    public RList getDisplayList() {
        if (devState.displayList == null) {
            devState.displayList = DisplayList.createInitialDisplayList();
        }
        return devState.displayList;
    }

    public void setDisplayList(RList newList) {
        newList.makeSharedPermanent();
        devState.displayList = newList;
    }

    public void setDisplayListElement(Object element) {
        devState.displayList.setDataAt(devState.displayListIndex, element);
    }

    public boolean isDisplayListOn() {
        return devState.isDisplayListOn;
    }

    public void setIsDisplayListOn(boolean flag) {
        devState.isDisplayListOn = flag;
    }

    public int getDisplayListIndex() {
        return devState.displayListIndex;
    }

    public void setDisplayListIndex(int newValue) {
        devState.displayListIndex = newValue;
    }

    public GridPalette getPalette() {
        return palette == null ? GridColorUtils.getDefaultPalette() : palette;
    }

    public void setPalette(GridPalette palette) {
        this.palette = palette;
    }

    public void init(REnvironment newGridEnv) {
        this.gridEnv = newGridEnv;
        this.currentGrob = RNull.instance;
    }

    public void initGPar(GridDevice currentDevice) {
        devState.gpar = GPar.createNew(currentDevice);
        assert RAbstractVector.verify(devState.gpar);
    }

    /**
     * Returns something like a canonical gpar, or top level gpar. This is used when we need a
     * context to do e.g. unit conversion, but we are in a situation that no context is available.
     */
    public static GPar getInitialGPar(GridDevice device) {
        return GPar.create(GPar.createNew(device));
    }

    public RList getGpar() {
        assert gridEnv != null : "GridState not initialized";
        return devState.gpar;
    }

    public void setGpar(RList gpar) {
        assert gridEnv != null : "GridState not initialized";
        devState.gpar = gpar;
    }

    /**
     * Has the current device been initialized for use by grid? Note: the null device should never
     * get initialized. The code initializing device should check if any device is open and if not,
     * it should open the default device and initialize it.
     */
    public boolean isDeviceInitialized() {
        return devState.isDeviceInitialized;
    }

    public void setDeviceInitialized() {
        devState.isDeviceInitialized = true;
    }

    public RList getViewPort() {
        return devState.viewPort;
    }

    public void setViewPort(RList viewPort) {
        assert RAbstractVector.verify(viewPort);
        devState.viewPort = viewPort;
    }

    public REnvironment getGridEnv() {
        return gridEnv;
    }

    public Object getCurrentGrob() {
        return currentGrob;
    }

    public void setCurrentGrob(Object currentGrob) {
        this.currentGrob = currentGrob;
    }

    public double getScale() {
        return devState.scale;
    }

    public String getNextPageFilename() {
        return FileDevUtils.formatFilename(devState.filenamePattern, devState.pageIndex++);
    }

    public static final class GridPalette {
        public final GridColor[] colors;
        public final String[] colorNames;

        public GridPalette(String[] colorNames) {
            this.colorNames = colorNames;
            colors = new GridColor[colorNames.length];
            for (int i = 0; i < colorNames.length; i++) {
                colors[i] = GridColorUtils.gridColorFromString(colorNames[i]);
            }
        }
    }

    static final class GridDeviceState {
        private boolean isDeviceInitialized = false;
        private RList gpar;
        private RList viewPort;
        private double scale = 1;
        private int devHoldCount;
        private boolean isDisplayListOn = true;
        private RList displayList;
        private int displayListIndex = 0;
        private int pageIndex = 2;
        private String filenamePattern;

        GridDeviceState(String filenamePattern) {
            this.filenamePattern = filenamePattern;
        }
    }
}
