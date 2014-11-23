package org.indilib.i4j.driver.stellarium;

/*
 * #%L
 * INDI for Java Proxy for stelarium
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The server part of the stellarium proxy. This class reads and writes the
 * stellarium protokol and exposes and interface to it.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public abstract class StellariumServer implements Runnable {

    /**
     * This class represents one connected stellarium client that is currently
     * connected to the server.
     */
    private final class StellariumClient implements Runnable {

        /**
         * 100 milliseconds sleep time.
         */
        private static final long HUNDERERD_MILLISECONDS = 100L;

        /**
         * how many bytes in a integer.
         */
        private static final int INTEGER_SIZE_IN_BYTES = 4;

        /**
         * how many bytes in a long.
         */
        private static final int LONG_SIZE_IN_BYTES = 8;

        /**
         * the size of a standard stellarium responce.
         */
        private static final int STELLARIUM_REPONCE_MESSAGE_SIZE = 24;

        /**
         * max declination value.
         */
        private static final double DEC_90_DEGREES = 90d;

        /**
         * max right assertion value.
         */
        private static final double RA_24_HOURS = 24d;

        /**
         * stellarium sends the declination as a integer value between minus
         * this and plus this.
         */
        private static final long STELLARIUM_90_DEGREE_DEC_VALUE = 0x40000000L;

        /**
         * stellarium sends the right assertion as a ungined integer value
         * between 0 and...
         */
        private static final long STELLARIUM_24H_RA_VALUE = 0x100000000L;

        /**
         * mask to convert an unsigned in into a signed long value.
         */
        private static final long UNSIGNED_INT_TO_LONG = 0xFFFFFFFFL;

        /**
         * mask for the lowest byte of a integer value.
         */
        private static final int INTEGER_LOWEST_BYTE_MASK = 0xFF;

        /**
         * mask for the lowest byte of a long value.
         */
        private static final long LONG_LOWEST_BYTE_MASK = 0xFFL;

        /**
         * how many bits to shift for the 2e byte.
         */
        private static final int BYTE_SHIFT_2 = 8;

        /**
         * how many bits to shift for the 3e byte.
         */
        private static final int BYTE_SHIFT_3 = 16;

        /**
         * how many bits to shift for the 4e byte.
         */
        private static final int BYTE_SHIFT_4 = 24;

        /**
         * how many bits to shift for the 5e byte.
         */
        private static final int BYTE_SHIFT_5 = 32;

        /**
         * how many bits to shift for the 6e byte.
         */
        private static final int BYTE_SHIFT_6 = 40;

        /**
         * how many bits to shift for the 7e byte.
         */
        private static final int BYTE_SHIFT_7 = 48;

        /**
         * how many bits to shift for the 8e byte.
         */
        private static final int BYTE_SHIFT_8 = 56;

        /**
         * The client socket for the connection.
         */
        private Socket clientSocket;

        /**
         * The socket input stream.
         */
        private InputStream in;

        /**
         * The socket output stream.
         */
        private OutputStream out;

        /**
         * Constructor for a stellarium client.
         * 
         * @param clientSocket
         *            the socket over with to connect to the client.
         * @throws IOException
         *             if the connection could not be correctly initialized.
         */
        public StellariumClient(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            new Thread(this, "Stelarium client").start();
        }

        /**
         * Close the connection to this client.
         */
        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOG.warn("problem closing socket", e);
            }
            clients.remove(this);
        }

        /**
         * @return an read 4 byte integer from the connection.
         * @throws IOException
         *             if the connection fails.
         */
        public int readInt() throws IOException {
            int value = in.read();
            value = value | (in.read() << BYTE_SHIFT_2);
            value = value | (in.read() << BYTE_SHIFT_3);
            value = value | (in.read() << BYTE_SHIFT_4);
            return value;
        }

        /**
         * @return an read 8 byte long from the connection.
         * @throws IOException
         *             if the connection fails.
         */
        public long readLong() throws IOException {
            long value = readByteAsLong();
            value = value | (readByteAsLong() << BYTE_SHIFT_2);
            value = value | (readByteAsLong() << BYTE_SHIFT_3);
            value = value | (readByteAsLong() << BYTE_SHIFT_4);
            value = value | (readByteAsLong() << BYTE_SHIFT_5);
            value = value | (readByteAsLong() << BYTE_SHIFT_6);
            value = value | (readByteAsLong() << BYTE_SHIFT_7);
            value = value | (readByteAsLong() << BYTE_SHIFT_8);
            return value;
        }

        /**
         * @return an read 2 byte short from the connection.
         * @throws IOException
         *             if the connection fails.
         */
        public int readShort() throws IOException {
            int value = in.read();
            value = value | (in.read() << BYTE_SHIFT_2);
            return value;
        }

        @Override
        public void run() {
            try {
                while (!stop) {
                    if (in.available() < 2) {
                        Thread.sleep(HUNDERERD_MILLISECONDS);
                        continue;
                    }
                    int length = readShort();
                    length -= 2;
                    int type = -1;
                    if (length > 0) {
                        type = readShort();
                        length -= 2;
                    }
                    if (type == 0) {
                        long time = -1;
                        if (length > 0) {
                            time = readLong();
                            length -= LONG_SIZE_IN_BYTES;
                        }
                        long ra = -1;
                        if (length > 0) {
                            // & 0xFFFFFFFFL for unsigned int to long
                            ra = readInt() & UNSIGNED_INT_TO_LONG;
                            length -= INTEGER_SIZE_IN_BYTES;
                        }
                        long dec = -1;
                        if (length > 0) {
                            dec = readInt();
                            length -= INTEGER_SIZE_IN_BYTES;
                        }
                        double raDouble = (Long.valueOf(ra).doubleValue() / Long.valueOf(STELLARIUM_24H_RA_VALUE).doubleValue()) * RA_24_HOURS;
                        double decDouble = (Long.valueOf(dec).doubleValue() / Long.valueOf(STELLARIUM_90_DEGREE_DEC_VALUE).doubleValue()) * DEC_90_DEGREES;

                        vectorRecievedFromStellariumClient(raDouble, decDouble);
                    }
                    if (length > 0) {
                        LOG.warn("unknown stelarium message type " + type + " length " + length);
                        in.skip(length);
                    }
                }
            } catch (Exception e) {
                LOG.error("stelarium client disconnected", e);
            }
            try {
                this.clientSocket.close();
            } catch (IOException e1) {
                LOG.error("could not close client", e1);
            }
        }

        /**
         * Send the current scope coordinates to stellarium client.
         * 
         * @param ra
         *            the current right asertion
         * @param dec
         *            the current declination.
         * @throws IOException
         *             if the connection fails.
         */
        public void sendCoordinated(double ra, double dec) throws IOException {
            long raInt = 0;
            long decInt = 0;
            while (dec > DEC_90_DEGREES) {
                dec = dec - (DEC_90_DEGREES * 2);
                ra = ra - (RA_24_HOURS / 2);
            }
            while (dec < (-DEC_90_DEGREES)) {
                dec = dec + (DEC_90_DEGREES * 2);
                ra = ra + (RA_24_HOURS / 2);
            }
            while (ra > RA_24_HOURS) {
                ra = ra - RA_24_HOURS;
            }
            while (ra < 0d) {
                ra = ra + RA_24_HOURS;
            }
            raInt = (long) ((ra / RA_24_HOURS) * Long.valueOf(STELLARIUM_24H_RA_VALUE).doubleValue());
            decInt = (long) ((dec / DEC_90_DEGREES) * Long.valueOf(STELLARIUM_90_DEGREE_DEC_VALUE).doubleValue());
            writeShort(STELLARIUM_REPONCE_MESSAGE_SIZE);
            writeShort(0); // type
            writeLong(System.currentTimeMillis()); // time
            writeInt((int) raInt); // ra
            writeInt((int) decInt); // dec
            writeInt(0); // status all ok
        }

        /**
         * write a 4 byte integer to the client.
         * 
         * @param value
         *            the integer to write.
         * @throws IOException
         *             if the connection fails.
         */
        public void writeInt(int value) throws IOException {
            out.write(value & INTEGER_LOWEST_BYTE_MASK);
            out.write((value >> BYTE_SHIFT_2) & INTEGER_LOWEST_BYTE_MASK);
            out.write((value >> BYTE_SHIFT_3) & INTEGER_LOWEST_BYTE_MASK);
            out.write((value >> BYTE_SHIFT_4) & INTEGER_LOWEST_BYTE_MASK);
        }

        /**
         * write a 8 byte long to the client.
         * 
         * @param value
         *            the long to write.
         * @throws IOException
         *             if the connection fails.
         */
        public void writeLong(long value) throws IOException {
            out.write((int) (value & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_2) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_3) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_4) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_5) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_6) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_7) & LONG_LOWEST_BYTE_MASK));
            out.write((int) ((value >> BYTE_SHIFT_8) & LONG_LOWEST_BYTE_MASK));
        }

        /**
         * write a 2 byte short to the client.
         * 
         * @param value
         *            the short to write.
         * @throws IOException
         *             if the connection fails.
         */
        public void writeShort(int value) throws IOException {
            out.write(value & INTEGER_LOWEST_BYTE_MASK);
            out.write((value >> BYTE_SHIFT_2) & INTEGER_LOWEST_BYTE_MASK);
        }

        /**
         * 
         * @return the next byte from the connection as a long value.
         * @throws IOException
         *             if the connection fails.
         */
        private long readByteAsLong() throws IOException {
            return in.read();
        }
    }

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StellariumServer.class);

    /**
     * the list with currently connected clients.
     */
    private List<StellariumClient> clients = new ArrayList<>();

    /**
     * the stellarium binded server socket.
     */
    private ServerSocket stellariumServerSocket;

    /**
     * is the server shutdowned?
     */
    private boolean stop = false;

    /**
     * construtor for a stellarium server.
     * 
     * @param stellariumPort
     *            the server port to bind to.
     * @throws IOException
     *             if the server socket binding fails.
     */
    public StellariumServer(int stellariumPort) throws IOException {
        stellariumServerSocket = new ServerSocket(stellariumPort);
        LOG.info("stellarium server listening to port " + stellariumPort);
        new Thread(this, "stellarium server").start();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Socket clientSocket = stellariumServerSocket.accept();
                clients.add(new StellariumClient(clientSocket));
            } catch (IOException e) {
                LOG.error("could not initiate connection to stellarium client", e);
            }
        }
        // just to be sure all connections are closed.
        stop();
    }

    /**
     * send this vector to all stellarium clients.
     * 
     * @param ra
     *            the current right assertion.
     * @param dec
     *            teh current declination.
     */
    public void sendVectorToStellariumClients(double ra, double dec) {
        for (StellariumClient stellariumClient : new ArrayList<>(clients)) {
            try {
                stellariumClient.sendCoordinated(ra, dec);
            } catch (IOException e) {
                LOG.error("could not send current vector to stellarium");
                stellariumClient.close();
            }
        }
    }

    /**
     * stop the stellarium server.
     */
    public void stop() {
        stop = true;
        for (StellariumClient stellariumClient : new ArrayList<>(clients)) {
            stellariumClient.close();
        }
        if (stellariumServerSocket != null) {
            try {
                stellariumServerSocket.close();
            } catch (IOException e) {
                LOG.error("could not close stellarium port");
            }
        }
    }

    /**
     * a goto command was recieved from one of the stellarium clients,
     * subclasses should do something with it.
     * 
     * @param ra
     *            the current right assertion.
     * @param dec
     *            the current declination.
     */
    public abstract void vectorRecievedFromStellariumClient(double ra, double dec);

}
