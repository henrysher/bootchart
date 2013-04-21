/*
 * Bootchart -- Boot Process Visualization
 *
 * Copyright (C) 2004  Ziga Mahkovec <ziga.mahkovec@klika.si>
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
package org.bootchart.common;

import java.util.Date;

/**
 * Disk I/O throughput sample.
 */
public class DiskTPutSample extends Sample {
	/** Read throughput (KB/s). */
	public double read;
	/** Write throughput (KB/s). */
	public double write;
	
	/**
	 * Creates a new sample.
	 * 
	 * @param time   sample time
	 * @param read   read throughput
	 * @param write  write throughput
	 */
	public DiskTPutSample(Date time, double read, double write) {
		this.time = time != null ? new Date(time.getTime()) : null;
		this.read = read;
		this.write = write;
	}
	
	/**
	 * Returns the string representation of the sample.
	 * 
	 * @return  string representation
	 */
	public String toString() {
		return TIME_FORMAT.format(time) + "\t" + read + "\t" + write;
	}
}
