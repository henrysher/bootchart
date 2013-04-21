/*
 * Bootchart -- Boot Process Visualization
 *
 * Copyright (C) 2006  Ziga Mahkovec <ziga.mahkovec@klika.si>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bootchart.parser.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.bootchart.common.Common;
import org.bootchart.common.DiskTPutSample;
import org.bootchart.common.NetDevTPutSample;
import org.bootchart.common.Stats;


/**
 * ProcNetDev parses log files produced by logging the output of
 * <code>/proc/net/dev</code>.  The samples contain information about network
 * interface throughput, which is used for IO throughput on diskless network
 * stations.
 */
public class ProcNetDevParser {
	private static final Logger log = Logger.getLogger(ProcNetDevParser.class.getName());

	/** NetDevSample encapsulates a /proc/net/dev sample. */
	private static class NetDevSample {
		long[] values  = new long[2]; // {rbytes, wbytes}
		long[] changes = new long[2]; // {rbytes, wbytes}
	}
	
	/**
	 * Parses the <code>proc_netdev.log</code> file.  The output from
	 * <code>/proc/net/dev</code> is used to collect the network interface
	 * statistics, which is returned as disk throughput statistics.
	 * 
	 * @param is      the input stream to read from
	 * @return        disk statistics {@link DiskTPutSample} samples)
	 * @throws IOException  if an I/O error occurs
	 */
	public static Stats parseLog(InputStream is)
		throws IOException {
		BufferedReader reader = Common.getReader(is);
		String line = reader.readLine();

		int numSamples = 0;
		Stats diskStats = new Stats();
		// last time
		Date ltime = null;
		// a map of /proc/diskstat values for each disk
		Map diskStatMap = new HashMap();
		
		while (line != null) {
			// skip empty lines
			while (line != null && line.trim().length() == 0) {
				line = reader.readLine();
			}
			if (line == null) {
				// EOF
				break;
			}
			line = line.trim();
			if (line.startsWith("#")) {
				continue;
			}
			Date time = null;
			if (line.matches("^\\d+$")) {
				time = new Date(Long.parseLong(line) * 10);
				numSamples++;
			} else {
				line = reader.readLine();
				continue;
			}
			
			// eat the header lines
			reader.readLine();
			reader.readLine();
			
			// read stats for all disks
			line = reader.readLine();
			while (line != null && line.trim().length() > 0) {
				line = line.trim();
				// {interface rbytes rpackets rerrs rdrop rfifo rframe rcompressed rmulticast
				// 	          tbytes tpackets terrs tdrop tfifo tframe tcompressed tmulticast }
				String[] tokens = line.split(":");
				String iface = tokens[0];
				if (tokens.length != 2 || "lo".equals(tokens[0])) {
					line = reader.readLine();
					continue;
				}
				tokens = tokens[1].trim().split("\\s+");
				if (tokens.length != 16) {
					line = reader.readLine();
					continue;
				}
				
				long rbytes = Long.parseLong(tokens[1]);
				long tbytes = Long.parseLong(tokens[9]);
				NetDevSample sample = (NetDevSample)diskStatMap.get(iface);
				if (sample == null) {
					sample = new NetDevSample();
					diskStatMap.put(iface, sample);
				}
				if (ltime != null) {
					sample.changes[0] = rbytes - sample.values[0];
					sample.changes[1] = tbytes - sample.values[1];
				}
				sample.values = new long[]{rbytes, tbytes};
				line = reader.readLine();
			}
			if (ltime != null) {
				long interval = time.getTime() - ltime.getTime();
				interval = Math.max(interval, 1);
				
				// sum up changes for all disks
				long[] sums = new long[2];
				for (Iterator i=diskStatMap.entrySet().iterator(); i.hasNext(); ) {
					Map.Entry entry = (Map.Entry)i.next();
					NetDevSample sample = (NetDevSample)entry.getValue();
					for (int j=0; j<2; j++) {
						sums[j] += sample.changes[j];
					}
				}
				
				double readTPut = sums[0] * 1000.0 / interval;
				double writeTPut = sums[1] * 1000.0 / interval;
				
				NetDevTPutSample tputSample = new NetDevTPutSample(time, readTPut, writeTPut);
				diskStats.addSample(tputSample);
			}
			ltime = time;
			if (numSamples > Common.MAX_PARSE_SAMPLES) {
				break;
			}
		}
		log.fine("Parsed " + diskStats.getSamples().size() + " /proc/net/dev samples");
		return diskStats;
	}
}
