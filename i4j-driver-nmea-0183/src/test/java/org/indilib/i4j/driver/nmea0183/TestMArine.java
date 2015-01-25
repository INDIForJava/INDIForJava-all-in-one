package org.indilib.i4j.driver.nmea0183;

/*
 * #%L
 * INDI for Java NMEA 0183 stream driver
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Enumeration;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.parser.XDRParserFixed;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TimeSentence;

import org.indilib.i4j.driver.nmea0183.parser.MDAParser;
import org.indilib.i4j.driver.nmea0183.parser.MWDParser;

public class TestMArine implements SentenceListener {

    public static void main(String[] args) throws IOException {

        Enumeration<InputStream> streamEnum = new Enumeration<InputStream>() {

            private int index = 0;

            private final InputStream[] streams = {
                TestMArine.class.getClassLoader().getResourceAsStream("xdr.txt"),
            // TestMArine.class.getClassLoader().getResourceAsStream("gpsout.txt"),
            // TestMArine.class.getClassLoader().getResourceAsStream("xxx.txt")
                    };

            @Override
            public boolean hasMoreElements() {
                return index < streams.length;
            }

            @Override
            public InputStream nextElement() {
                return streams[index++];
            }
        };
        InputStream data = new SequenceInputStream(streamEnum);

        TestMArine testMArine = new TestMArine();

        SentenceFactory factory = SentenceFactory.getInstance();
        factory.registerParser(MDAParser.MDASentenceId, MDAParser.class);
        factory.registerParser(MWDParser.MWDSentenceId, MWDParser.class);
        factory.registerParser(SentenceId.XDR.name(), XDRParserFixed.class);

        BufferedReader reader = new BufferedReader(new InputStreamReader(data));

        String readLine;
        while ((readLine = reader.readLine()) != null) {
            Sentence sentence = factory.createParser(readLine);
            analyseSentence(sentence);
        }

    }

    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingPaused()
     */
    public void readingPaused() {
        System.out.println("-- Paused --");
        System.exit(0);
    }

    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingStarted()
     */
    public void readingStarted() {
        System.out.println("-- Started --");
    }

    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingStopped()
     */
    public void readingStopped() {
        System.out.println("-- Stopped --");
    }

    /*
     * (non-Javadoc)
     * @see
     * net.sf.marineapi.nmea.event.SentenceListener#sentenceRead(net.sf.marineapi
     * .nmea.event.SentenceEvent)
     */
    public void sentenceRead(SentenceEvent event) {
        // Safe to cast as we are registered only for GGA updates. Could
        // also cast to PositionSentence if interested only in position data.
        // When receiving all sentences without filtering, you should check the
        // sentence type before casting (e.g. with Sentence.getSentenceId()).
        Sentence sentence = event.getSentence();
        analyseSentence(sentence);

    }

    public static void analyseSentence(Sentence sentence) {
        if (sentence instanceof PositionSentence) {
            PositionSentence s = (PositionSentence) sentence;
            // Do something with sentence data..
            System.out.println("pos:" + s.getPosition());

            System.out.println("getPosition().getAltitude:" + s.getPosition().getAltitude());
            System.out.println("getPosition().getLatitude:" + s.getPosition().getLatitude());
            System.out.println("getPosition().getLongitude:" + s.getPosition().getLongitude());
            System.out.println("getPosition().getDatum:" + s.getPosition().getDatum());
        }
        if (sentence instanceof TimeSentence) {
            TimeSentence s = (TimeSentence) sentence;
            // Do something with sentence data..
            System.out.println(s.getTime().toString());
        }
        if (WeatherParser.hasData(sentence)) {
            WeatherParser mda = new WeatherParser(sentence);
            System.out.println("getAbsoluteHumidity:" + mda.getAbsoluteHumidity());
            System.out.println("getAirTemperature:" + mda.getAirTemperature());
            System.out.println("getBarometricPressure:" + mda.getBarometricPressure());
            System.out.println("getDewPoint:" + mda.getDewPoint());
            System.out.println("getMagneticWindDirection:" + mda.getMagneticWindDirection());
            System.out.println("getRelativeHumidity:" + mda.getRelativeHumidity());
            System.out.println("getTrueWindDirection:" + mda.getTrueWindDirection());
            System.out.println("getWaterTemperature:" + mda.getWaterTemperature());
            System.out.println("getWindSpeed:" + mda.getWindSpeed());
        }
    }
}
