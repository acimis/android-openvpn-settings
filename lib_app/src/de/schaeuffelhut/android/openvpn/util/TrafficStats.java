/*
 * This file is part of OpenVPN-Settings.
 *
 * Copyright © 2009-2012  Friedrich Schäuffelhut
 *
 * OpenVPN-Settings is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenVPN-Settings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
 * Contact the author at:          android.openvpn@schaeuffelhut.de
 */

package de.schaeuffelhut.android.openvpn.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * @author chri
 *
 */
public class TrafficStats {

	private final static String LOG_TAG = "TrafficStats";
	public static final int mPollInterval = 3;
	
	private Date updated = new Date();
	DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	private long tuntapReadBytes = 0;
	private long tuntapWriteBytes = 0;
	private long tcpudpReadBytes = 0;
	private long tcpudpWriteBytes = 0;
	private long authReadBytes = 0;
	private long preCompressBytes = 0;
	private long postCompressBytes = 0;
	private long preDecompressBytes = 0;
	private long postDecompressBytes = 0;
	
	private long tuntapReadBytesPerSec = 0;
	private long tuntapWriteBytesPerSec = 0;
	private long tcpudpReadBytesPerSec = 0;
	private long tcpudpWriteBytesPerSec = 0;
	private long authReadBytesPerSec = 0;
	private long preCompressBytesPerSec = 0;
	private long postCompressBytesPerSec = 0;
	private long preDecompressBytesPerSec = 0;
	private long postDecompressBytesPerSec = 0;
	
	
	public TrafficStats() {
		
	}
	
	public void setStats(final long newReadBytes, final long newWriteBytes) {
		tuntapReadBytesPerSec = deltaPerSecond(tuntapReadBytes, newReadBytes);
		tuntapReadBytes = newReadBytes;
		
		tuntapWriteBytesPerSec = deltaPerSecond(tuntapWriteBytes, newWriteBytes);
		tuntapWriteBytes = newWriteBytes;
	}

	private long deltaPerSecond(long oldBytes, long newBytes) {
		return ((newBytes - oldBytes)/ mPollInterval);
	}
	
	/**
	 * Fill in the statistics based on the OpenVPN MultiLine status output
	 * @param multiLineHistory and ArrayList containing the output from openvpn
	 */
	@Deprecated
	public void setStats(ArrayList<String> multiLineHistory) {
//		OpenVPN STATISTICS
//		Updated,Sun Nov 15 15:49:24 2009
//		TUN/TAP read bytes,5261
//		TUN/TAP write bytes,0
//		TCP/UDP read bytes,7115
//		TCP/UDP write bytes,10493
//		Auth read bytes,64
//		pre-compress bytes,0
//		post-compress bytes,0
//		pre-decompress bytes,0
//		post-decompress bytes,0
		
		Iterator<String> iterator = multiLineHistory.iterator();
		// skip the first command as it will always be the same
		iterator.next();
		
		// now loop over every line and fill in the variables
		while (iterator.hasNext()) {
			String line = iterator.next();
			String command = null, value = null;
			// search for the comma "," and split the message in two 
			Matcher matcher = Pattern.compile("^(.*),(.*)$").matcher(line);
			if (matcher.find()) {
				command = matcher.group(1);
				value = matcher.group(2);
			}
			else {
				Log.e(LOG_TAG, "ERROR: following status line could not be split: " + line);
				break;
			}

			if (0 == command.compareTo("Updated")) {
				// date of last update
				try {
					updated = dateFormat.parse(value);
				} catch (ParseException e) {
					Log.e(LOG_TAG, "Cannot parse date: " + value);
				}
			} else if (0 == command.compareTo("TUN/TAP read bytes")) {
				// traffic from the tunnel
				long newTuntapReadBytes = Long.parseLong(value);
				tuntapReadBytesPerSec = ((newTuntapReadBytes - tuntapReadBytes)/ getDivideFactor());
				tuntapReadBytes = newTuntapReadBytes;
			} else if (0 == command.compareTo("TUN/TAP write bytes")) {
				// traffic to the tunnel
				long newTuntapWriteBytes = Long.parseLong(value);
				tuntapWriteBytesPerSec = ((newTuntapWriteBytes - tuntapWriteBytes)/ getDivideFactor());
				tuntapWriteBytes = newTuntapWriteBytes;
			} else if (0 == command.compareTo("TCP/UDP read bytes")) {
				// traffic generated by the tunnel
				long newTcpudpReadBytes = Long.parseLong(value);
				tcpudpReadBytesPerSec = ((newTcpudpReadBytes - tcpudpReadBytes)/ getDivideFactor());
				tcpudpReadBytes = newTcpudpReadBytes;
			} else if (0 == command.compareTo("TCP/UDP write bytes")) {
				// traffic generated by the tunnel
				long newTcpudpWriteBytes = Long.parseLong(value);
				tcpudpWriteBytesPerSec = ((newTcpudpWriteBytes - tcpudpWriteBytes)/ getDivideFactor());
				tcpudpWriteBytes = newTcpudpWriteBytes;
			} else if (0 == command.compareTo("Auth read bytes")) {
				// traffic because of the authentication
				long newAuthReadBytes = Long.parseLong(value);
				authReadBytesPerSec = ((newAuthReadBytes - authReadBytes)/ getDivideFactor());
				authReadBytes = newAuthReadBytes;
			} else if (0 == command.compareTo("pre-compress bytes")) {
				// size before compression
				long newPreCompressBytes = Long.parseLong(value);
				preCompressBytesPerSec = ((newPreCompressBytes - preCompressBytes)/ getDivideFactor());
				preCompressBytes = newPreCompressBytes;
			} else if (0 == command.compareTo("post-compress bytes")) {
				// size after compression
				long newPostCompressBytes = Long.parseLong(value);
				postCompressBytesPerSec = ((newPostCompressBytes - postCompressBytes)/ getDivideFactor());
				postCompressBytes = newPostCompressBytes;
			} else if (0 == command.compareTo("pre-decompress bytes")) {
				// size before decompression
				long newPreDecompressBytes = Long.parseLong(value);
				preDecompressBytesPerSec = ((newPreDecompressBytes - preDecompressBytes)/ getDivideFactor());
				preDecompressBytes = newPreDecompressBytes;
			} else if (0 == command.compareTo("post-decompress bytes")) {
				// size after decompression
				long newPostDecompressBytes = Long.parseLong(value);
				postDecompressBytesPerSec = ((newPostDecompressBytes - postDecompressBytes)/ getDivideFactor());
				postDecompressBytes = newPostDecompressBytes;
			} else {
				// should never happen
				Log.e(LOG_TAG, "ERROR: following status line was not understood: "
						+ line);
			}
		}

	}
	
	@Deprecated
	private int getDivideFactor() {
		return mPollInterval ;
	}
	
	public String toSmallInOutPerSecString() {
		// TODO chri - use stringbuilder
		return "up: " 
				+ Util.roundDecimalsToString((double) tuntapReadBytesPerSec / 1000) 
				+ " kBps - down: "
				+ Util.roundDecimalsToString((double) tuntapWriteBytesPerSec / 1000) 
				+ " kBps";
	}
	
	
}