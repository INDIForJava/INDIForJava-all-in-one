package org.indilib.i4j.driver.raspberry.camera.image;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.common.byteSources.ByteSourceArray;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.indilib.i4j.driver.raspberry.camera.CameraConstands;

public abstract class JpegStreamScanner implements Runnable {

    private static class VisibleByteArrayOutputStream extends ByteArrayOutputStream {

        public VisibleByteArrayOutputStream(int i) {
            super(i);
        }

        public byte[] bytes() {
            return buf;
        }
    }

    private static class JpegImage {

        List<JPegBlock> blocks = new ArrayList<>();

        public JpegImage(BufferedInputStream in) throws IOException {
            JPegBlock block;
            boolean lastBlock;
            do {
                block = new JPegBlock(in);
                blocks.add(block);
                lastBlock = block.isLastBlock();
                if (lastBlock) {
                    in.mark(2);
                    int marker = in.read();
                    in.reset();
                    if (marker == 0x40) {
                        block = new JPegBlock(in);
                        blocks.add(block);
                    }
                }
            } while (!lastBlock);
        }
    }

    private static class JPegBlock {

        private byte[] headerBytes = new byte[2];

        private byte[] sizeBytes = new byte[2];

        private byte[] data;

        private int marker = -1;

        private int size = 2;

        public JPegBlock(BufferedInputStream in) throws IOException {
            read(in);
        }

        private void read(BufferedInputStream in) throws IOException {
            if (!readFully(in, headerBytes)) {
                throw new EOFException();
            }
            int baseMarker = (headerBytes[0] & 0xff) << 8;
            if (baseMarker == 0x4000) {
                readRawImageData(in);
                return;
            }
            if (baseMarker != 0xff00) {
                throw new IllegalArgumentException("basemarker wrong!");
            }
            marker = (baseMarker + (headerBytes[1] & 0xff) << 0);
            if (marker == 0xFFD9) {
                return;
            }
            if (marker == 0xFFDD) {
                System.out.println();
            }
            if (hasLength()) {
                readFully(in, sizeBytes);
                size = (((sizeBytes[0] & 0xff) << 8) + ((sizeBytes[1] & 0xff) << 0));
                if (size > 0) {
                    data = new byte[size - 2];
                    readFully(in, data);
                }
            }
            if (marker == 0xFFDA) {
                readJpegFlexData(in);
            }
        }

        private void readJpegFlexData(BufferedInputStream in) throws IOException {
            try (VisibleByteArrayOutputStream out = new VisibleByteArrayOutputStream(4096)) {
                byte[] buffer = new byte[4096];
                int scanCount;
                in.mark(buffer.length + 16);
                int lastMarkPos = 0;
                while ((scanCount = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, scanCount);
                    byte[] buf = out.bytes();
                    int endOfBuf = out.size() - 1;
                    for (int index = out.size() - scanCount; index < endOfBuf; index++) {
                        if ((buf[index] & 0xff) == 0xff && (buf[index + 1] & 0xff) != 0) {
                            in.reset();
                            skip(in, index - lastMarkPos);
                            data = Arrays.copyOfRange(out.bytes(), 0, index);
                            return;
                        }
                    }
                    // was ist wenn das letzte byte ff ist
                    lastMarkPos = out.size();
                    in.mark(buffer.length + 16);
                }
            }
        }

        private void readRawImageData(BufferedInputStream in) throws IOException, UnsupportedEncodingException {
            // probable raspberry block
            byte[] brcmMarker = new byte[5];
            brcmMarker[0] = headerBytes[0];
            brcmMarker[1] = headerBytes[1];
            readFully(in, brcmMarker, 2);
            if (!new String(brcmMarker, "UTF-8").equals("@BRCM")) {
                throw new IllegalArgumentException("raspberry raw marker wrong!");
            }
            marker = 0xFFD9;
            // skip over the header
            skip(in, CameraConstands.HEADERSIZE - brcmMarker.length);
            data = new byte[CameraConstands.RAWBLOCKSIZE - CameraConstands.HEADERSIZE];
            if (!readFully(in, data)) {
                throw new IllegalArgumentException("Premature end of file, wrong file format?");
            }
        }

        private boolean hasLength() {
            if ((marker >= 0xFFD0 && marker < 0xFFD9) || marker == 0xFF01 || marker == 0xFF00) {
                return false;
            }
            return true;
        }

        private boolean readFully(BufferedInputStream in, byte[] bytes) throws IOException {
            return readFully(in, bytes, 0);
        }

        private boolean readFully(BufferedInputStream in, byte[] bytes, int offset) throws IOException {
            int count;
            do {
                count = in.read(bytes, offset, bytes.length - offset);
                if (count >= 0) {
                    offset += count;
                }
            } while (count >= 0 && offset < bytes.length);
            if (count < 0) {
                System.out.println("partial read " + offset);
            }
            return count >= 0;
        }

        protected boolean isLastBlock() {
            return marker == 0xFFD9;
        }

        protected boolean isRawBlock() {
            return isLastBlock() && (headerBytes[0] & 0xff) == 0x40;
        }

        protected boolean isExifBlock() {
            return marker == 0xffe1;
        }
    }

    private final BufferedInputStream in;

    protected void jpeg(JpegImage jpegData) {
        try {
            JPegBlock startBlock = jpegData.blocks.get(0);
            JPegBlock endBlock = null;
            JPegBlock rawBlock = null;
            JPegBlock exifBlock = null;

            for (JPegBlock block : jpegData.blocks) {
                if (block.isRawBlock()) {
                    rawBlock = block;
                } else if (block.isExifBlock()) {
                    exifBlock = block;
                } else if (block.isLastBlock()) {
                    endBlock = block;
                }
            }

            TiffImageMetadata metadata = getExifMetaData(startBlock, endBlock, exifBlock);
            rawImage(new RawImage(metadata, rawBlock.data));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse image data from stream", e);
        }

    }

    protected void rawImage(RawImage rawImage) throws Exception {

    }

    public JpegStreamScanner(BufferedInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                jpeg(new JpegImage(in));
            }
        } catch (EOFException e) {
            return;
        } catch (Exception e) {
            if (!isInShutdown()) {
                throw new RuntimeException("interruppted", e);
            }
        }
    }

    /**
     * do not throw any exceptione when we are in the process of shutdown.
     * 
     * @return
     */
    protected boolean isInShutdown() {
        return false;
    }

    private static byte[] skipBuffer = new byte[4096];

    private static long skip(InputStream in, long toSkipStart) throws IOException {
        long toSkip = toSkipStart;
        toSkip -= in.read(skipBuffer, 0, (int) Math.min(toSkip, skipBuffer.length));
        while (toSkip > 0) {
            toSkip -= in.read(skipBuffer, 0, (int) Math.min(toSkip, skipBuffer.length));
        }
        return toSkipStart;
    }

    private static TiffImageMetadata getExifMetaData(JPegBlock startBlock, JPegBlock endBlock, JPegBlock block) throws ImageReadException, IOException {
        HashMap<Object, Object> params = new HashMap<Object, Object>();
        byte[] segment = new byte[block.data.length + 8];

        segment[0] = startBlock.headerBytes[0];
        segment[1] = startBlock.headerBytes[1];
        segment[2] = block.headerBytes[0];
        segment[3] = block.headerBytes[1];
        segment[4] = block.sizeBytes[0];
        segment[5] = block.sizeBytes[1];
        segment[segment.length - 2] = endBlock.headerBytes[0];
        segment[segment.length - 1] = endBlock.headerBytes[1];

        System.arraycopy(block.data, 0, segment, 6, block.data.length);
        return new JpegImageParser().getExifMetadata(new ByteSourceArray(segment), params);
    }
}
