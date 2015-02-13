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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.common.byteSources.ByteSourceArray;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.indilib.i4j.driver.raspberry.camera.CameraConstands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The raw image extractor from a raspberry pi camera jpeg. Because the
 * raspberry images are send in a constand stream constising of the jpeg than
 * the raw than the next jpeg and so on the jpeg must be scipped over. Thats
 * what this class does, reading the jpeg in such a way it can be scipped.
 * Second task i the extract the exif infos with all the used settings.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class JpegStreamScanner implements Runnable {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JpegStreamScanner.class);

    /**
     * prefix size for a dummy jpeg image.
     */
    private static final int DUMMY_JPEG_PREFIX_SIZE = 6;

    /**
     * default buffer size to use.
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * number of bits in a byte.
     */
    private static final int BITS_IN_BYTE = 8;

    /**
     * mask to extract bytes.
     */
    private static final int BYTE_MASK = 0xff;

    /**
     * marker for a fexible size block.
     */
    private static final int MARKER_FFDA = 0xFFDA;

    /**
     * marker for a fixed size block.
     */
    private static final int MARKER_FFD9 = 0xFFD9;

    /**
     * unexpected marker with length info.
     */
    private static final int MARKER_FF00 = 0xff00;

    /**
     * marker for the raw image block.
     */
    private static final int MARKER_4000 = 0x4000;

    /**
     * first byte if the raw image marker.
     */
    private static final int RAW_BLOCK_MARKER_PREFIX = 0x40;

    /**
     * first marker that has length information.
     */
    private static final int MARKER_FFD0 = 0xFFD0;

    /**
     * marker for the exif block.
     */
    private static final int MARKER_FFE1 = 0xffe1;

    /**
     * another maker with length informations.
     */
    private static final int MARKER_FF01 = 0xFF01;

    /**
     * the size of the marker for the BRCM block with raw data.
     */
    private static final int BRCM_MARKER_SIZE = 5;

    /**
     * extra space to use.
     */
    private static final int EXTRA_MARK_SPACE = 16;

    /**
     * buffer used for skipping informations.
     */
    private static byte[] skipBuffer = new byte[BUFFER_SIZE];

    /**
     * the imput stream from with to read the jpeg images.
     */
    private final BufferedInputStream in;

    /**
     * Byte array output stream where the bytes are accessable.
     */
    private static class VisibleByteArrayOutputStream extends ByteArrayOutputStream {

        /**
         * create a byte array output stream of the specified size.
         * 
         * @param i
         *            the initial size of the stream
         */
        public VisibleByteArrayOutputStream(int i) {
            super(i);
        }

        /**
         * @return the internal byte array.
         */
        public byte[] bytes() {
            return buf;
        }
    }

    /**
     * This class represents the jpeg image. (only to scan the jpeg an get to
     * the raw data at the end).
     */
    private static class JpegImage {

        /**
         * blocks of jpeg data.
         */
        private List<JPegBlock> blocks = new ArrayList<>();

        /**
         * create a jpeg image from the inputstream by parsing the jpeg blocks.
         * 
         * @param in
         *            the input stream with jpeg data.
         * @throws IOException
         *             the the image was not jpeg.
         */
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
                    if (marker == RAW_BLOCK_MARKER_PREFIX) {
                        block = new JPegBlock(in);
                        blocks.add(block);
                    }
                }
            } while (!lastBlock);
        }
    }

    /**
     * One block of jepeg data.
     */
    private static class JPegBlock {

        /**
         * block header bytes.
         */
        private byte[] headerBytes = new byte[2];

        /**
         * block size bytes.
         */
        private byte[] sizeBytes = new byte[2];

        /**
         * the real jpeg data.
         */
        private byte[] data;

        /**
         * jpeg marker.
         */
        private int marker = -1;

        /**
         * the parsed size.
         */
        private int size = 2;

        /**
         * jpeg block constructor.
         * 
         * @param in
         *            the input stream
         * @throws IOException
         *             the the image was not jpeg.
         */
        public JPegBlock(BufferedInputStream in) throws IOException {
            read(in);
        }

        /**
         * read the block from the input stream.
         * 
         * @param in
         *            the input stream.
         * @throws IOException
         *             the the image was not jpeg.
         */
        private void read(BufferedInputStream in) throws IOException {
            if (!readFully(in, headerBytes)) {
                throw new EOFException();
            }
            int baseMarker = (headerBytes[0] & BYTE_MASK) << BITS_IN_BYTE;
            if (baseMarker == MARKER_4000) {
                readRawImageData(in);
                return;
            }
            if (baseMarker != MARKER_FF00) {
                throw new IllegalArgumentException("basemarker wrong!");
            }
            marker = baseMarker + (headerBytes[1] & BYTE_MASK) << 0;
            if (marker == MARKER_FFD9) {
                return;
            }
            if (hasLength()) {
                readFully(in, sizeBytes);
                size = ((sizeBytes[0] & BYTE_MASK) << BITS_IN_BYTE) + ((sizeBytes[1] & BYTE_MASK) << 0);
                if (size > 0) {
                    data = new byte[size - 2];
                    readFully(in, data);
                }
            }
            if (marker == MARKER_FFDA) {
                readJpegFlexData(in);
            }
        }

        /**
         * read a jpeg block that has a flexable size.
         * 
         * @param in
         *            the input stream
         * @throws IOException
         *             the the image was not jpeg.
         */
        private void readJpegFlexData(BufferedInputStream in) throws IOException {
            try (VisibleByteArrayOutputStream out = new VisibleByteArrayOutputStream(BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int scanCount;
                in.mark(buffer.length + EXTRA_MARK_SPACE);
                int lastMarkPos = 0;
                while ((scanCount = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, scanCount);
                    byte[] buf = out.bytes();
                    int endOfBuf = out.size() - 1;
                    for (int index = out.size() - scanCount; index < endOfBuf; index++) {
                        if ((buf[index] & BYTE_MASK) == BYTE_MASK && (buf[index + 1] & BYTE_MASK) != 0) {
                            in.reset();
                            skip(in, index - lastMarkPos);
                            data = Arrays.copyOfRange(out.bytes(), 0, index);
                            return;
                        }
                    }
                    // was ist wenn das letzte byte ff ist
                    lastMarkPos = out.size();
                    in.mark(buffer.length + EXTRA_MARK_SPACE);
                }
            }
        }

        /**
         * now read our target the raw sensor data.
         * 
         * @param in
         *            the input stream
         * @throws IOException
         *             if the image is corrupt
         */
        private void readRawImageData(BufferedInputStream in) throws IOException {
            // probable raspberry block
            byte[] brcmMarker = new byte[BRCM_MARKER_SIZE];
            brcmMarker[0] = headerBytes[0];
            brcmMarker[1] = headerBytes[1];
            readFully(in, brcmMarker, 2);
            if (!new String(brcmMarker, "UTF-8").equals("@BRCM")) {
                throw new IllegalArgumentException("raspberry raw marker wrong!");
            }
            marker = MARKER_FFD9;
            // skip over the header
            skip(in, CameraConstands.HEADERSIZE - brcmMarker.length);
            data = new byte[CameraConstands.RAWBLOCKSIZE - CameraConstands.HEADERSIZE];
            if (!readFully(in, data)) {
                throw new IllegalArgumentException("Premature end of file, wrong file format?");
            }
        }

        /**
         * @return true if this block has a length value.
         */
        private boolean hasLength() {
            if (marker >= MARKER_FFD0 && marker < MARKER_FFD9 || marker == MARKER_FF01 || marker == MARKER_FF00) {
                return false;
            }
            return true;
        }

        /**
         * read the byte array and fill it completely.
         * 
         * @param in
         *            the input stream to read
         * @param bytes
         *            the bytes to fill
         * @return true if successful.
         * @throws IOException
         *             if the image is corrupt
         */
        private boolean readFully(BufferedInputStream in, byte[] bytes) throws IOException {
            return readFully(in, bytes, 0);
        }

        /**
         * read the byte array and fill it completely starting with the offset.
         * 
         * @param in
         *            the input stream to read
         * @param bytes
         *            the bytes to fill
         * @param offset
         *            the start index in the byte array.
         * @return true if successful.
         * @throws IOException
         *             if the image is corrupt
         */
        private boolean readFully(BufferedInputStream in, byte[] bytes, int offset) throws IOException {
            int count;
            do {
                count = in.read(bytes, offset, bytes.length - offset);
                if (count >= 0) {
                    offset += count;
                }
            } while (count >= 0 && offset < bytes.length);
            if (count < 0) {
                LOG.warn("partial read of image block " + offset);
            }
            return count >= 0;
        }

        /**
         * @return true if this is the last block.
         */
        protected boolean isLastBlock() {
            return marker == MARKER_FFD9;
        }

        /**
         * @return true if this is the raw block.
         */
        protected boolean isRawBlock() {
            return isLastBlock() && (headerBytes[0] & BYTE_MASK) == RAW_BLOCK_MARKER_PREFIX;
        }

        /**
         * @return true if this is the exif block
         */
        protected boolean isExifBlock() {
            return marker == MARKER_FFE1;
        }
    }

    /**
     * interpred the read jpeg and report the extracted raw image.
     * 
     * @param jpegData
     *            the jpeg image in blocks.
     */
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

    /**
     * a raw image was extracted.
     * 
     * @param rawImage
     *            the extraced raw image.
     */
    protected abstract void rawImage(RawImage rawImage);

    /**
     * constructor around the input stream. That is a continuous stream of jpegs
     * that contain raw data, this raw data will be extraced and reported.
     * 
     * @param in
     *            the stream to extract all the raw images.
     */
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
     * do not throw any exception when we are in the process of shutdown.
     * 
     * @return true if we are stopping.
     */
    protected abstract boolean isInShutdown();

    /**
     * skip over a number of bytes.
     * 
     * @param in
     *            the input stream
     * @param toSkipStart
     *            todo
     * @return the orginal toSkipStart value
     * @throws IOException
     *             if the image is corrupt
     */
    private static long skip(InputStream in, long toSkipStart) throws IOException {
        long toSkip = toSkipStart;
        toSkip -= in.read(skipBuffer, 0, (int) Math.min(toSkip, skipBuffer.length));
        while (toSkip > 0) {
            toSkip -= in.read(skipBuffer, 0, (int) Math.min(toSkip, skipBuffer.length));
        }
        return toSkipStart;
    }

    /**
     * extract the exif meta datas from the jepeg stream.
     * 
     * @param startBlock
     *            start block
     * @param endBlock
     *            end block
     * @param block
     *            the exif block
     * @return the exif headers
     * @throws ImageReadException
     *             if the exif data could not be extracted
     * @throws IOException
     *             if the exif data could not be extracted
     */
    private static TiffImageMetadata getExifMetaData(JPegBlock startBlock, JPegBlock endBlock, JPegBlock block) throws ImageReadException, IOException {
        HashMap<Object, Object> params = new HashMap<Object, Object>();
        byte[] segment = new byte[block.data.length + DUMMY_JPEG_PREFIX_SIZE + 2];
        int segmentIndex = 0;
        segment[segmentIndex++] = startBlock.headerBytes[0];
        segment[segmentIndex++] = startBlock.headerBytes[1];
        segment[segmentIndex++] = block.headerBytes[0];
        segment[segmentIndex++] = block.headerBytes[1];
        segment[segmentIndex++] = block.sizeBytes[0];
        segment[segmentIndex++] = block.sizeBytes[1];
        segment[segment.length - 2] = endBlock.headerBytes[0];
        segment[segment.length - 1] = endBlock.headerBytes[1];

        System.arraycopy(block.data, 0, segment, DUMMY_JPEG_PREFIX_SIZE, block.data.length);
        return new JpegImageParser().getExifMetadata(new ByteSourceArray(segment), params);
    }
}
