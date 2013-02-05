/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.sam;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlignmentBlock {

    private int start;
    private byte[] bases;
    public byte[] qualities;
    private short[] counts;

    private boolean softClipped = false;
    private Alignment baseAlignment = null;

    public static AlignmentBlock getInstance(int start, byte[] bases, byte[] qualities, Alignment baseAlignment) {

        return new AlignmentBlock(start, bases, qualities, baseAlignment);
    }

    public static AlignmentBlock getInstance(int start, byte[] bases, byte[] qualities, FlowSignalContext fContext, Alignment baseAlignment) {
        return new AlignmentBlockFS(start, bases, qualities, fContext, baseAlignment);
    }

    protected AlignmentBlock(int start, byte[] bases, byte[] qualities, Alignment baseAlignment) {
        this.start = start;
        this.bases = bases;
        this.baseAlignment = baseAlignment;
        if (qualities == null || qualities.length < bases.length) {
            this.qualities = new byte[bases.length];
            Arrays.fill(this.qualities, (byte) 126);
        } else {
            this.qualities = qualities;
        }

        this.counts = null;
    }

    public Alignment getBaseAlignment() {
        return baseAlignment;
    }

    public boolean contains(int position) {
        int offset = position - start;
        return offset >= 0 && offset < bases.length;
    }

    public byte[] getBases() {
        return bases;
    }

    public byte getBase(int offset) {
        return bases[offset];
    }

    public int getStart() {
        return start;
    }

    public byte getQuality(int offset) {
        return qualities[offset];

    }

    public byte[] getQualities() {
        return qualities;
    }

    public short[] getCounts() {
        return counts;
    }

    public short getCount(int i) {
        return counts[i];
    }

    public void setCounts(short[] counts) {
        this.counts = counts;
    }

    /**
     * Convenience method
     */
    public int getEnd() {
        return start + bases.length;
    }

    public boolean isSoftClipped() {
        return softClipped;
    }

    public void setSoftClipped(boolean softClipped) {
        this.softClipped = softClipped;
    }

    public boolean hasFlowSignals() {
        return false;
    }

    public boolean hasCounts() {
        return counts != null;
    }

    // Default implementation -- to be overridden
    public FlowSignalSubContext getFlowSignalSubContext(int offset) {
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[block ");
        sb.append(isSoftClipped() ? "softClipped " : " ");
        sb.append(getStart());
        sb.append("-");
        sb.append(getEnd());
        sb.append(" ");
        for (int i = 0; i < bases.length; i++) {
            sb.append((char) bases[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    static List<MismatchBlock> createMismatchBlocks(int start, byte[] refBases, byte[] readBases){
        List<MismatchBlock> mismatchBlocks = new ArrayList<MismatchBlock>();
        List<Byte> mismatches = null;
        assert readBases.length == refBases.length;
        int lastMMBlockStart = -1;
        for(int ii = 0; ii <= readBases.length; ii++){

            byte readBase = -1;
            byte refBase = -1;
            boolean atEnd = false;
            if(ii < readBases.length){
                readBase = readBases[ii];
                refBase = refBases[ii];
            }else{
                atEnd = true;
            }

            if(atEnd || AlignmentUtils.compareBases(refBase, readBase)){
                //Finish off last mismatch
                if(mismatches != null){
                    byte[] seq = ArrayUtils.toPrimitive(mismatches.toArray(new Byte[0]));
                    MismatchBlock curMMBlock = new MismatchBlock(lastMMBlockStart, seq);
                    mismatchBlocks.add(curMMBlock);
                    mismatches = null;
                    lastMMBlockStart = -1;
                }
            }else{
                if(mismatches == null){
                    lastMMBlockStart = start + ii;
                    mismatches = new ArrayList<Byte>();
                }
                mismatches.add(readBase);
            }
        }

        return mismatchBlocks;
    }

    public static class MismatchBlock {
        public final int start;
        public final byte[] seq;

        private MismatchBlock(int start, byte[] seq){
            this.start = start;
            this.seq = seq;
        }

    }
}
