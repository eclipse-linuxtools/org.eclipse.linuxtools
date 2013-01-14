/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class CircularPointBuffer {
	
	private DataPoint[] buffer;
	private int head, tail;
	
	
	/**
	 * Create circular buffer with name 'Unknown'
	 * @param size
	 */
	public CircularPointBuffer(int size) {
		buffer = new DataPoint[size];
		head = 0;
		tail = 0;
	}

	public void add(DataPoint p) {
		buffer[tail] = p;
		tail++;
		if (tail >= buffer.length) {
			tail = 0;
		} 
		if (tail == head) {
			head++;
			if (head >= buffer.length) {
				head = 0;
			}
		}
	}
	
	public Iterator<DataPoint> getIterator() {
		return new CircularIterator();
	}
	
	private class CircularIterator implements Iterator<DataPoint> {
		private int tempHead;
		private boolean finished;
		
		public CircularIterator() {
			tempHead = head;
			finished = false;
		}

		@Override
		public boolean hasNext() {
			return tempHead != tail;
		}

		@Override
		public DataPoint next() {
			if (finished)
				throw new NoSuchElementException();
			
			DataPoint p = buffer[tempHead];
			if (tempHead == tail)
				finished = true;
			else {
				tempHead++;
				if (tempHead == buffer.length)
					tempHead = 0;
			}
			return p;
		}

		@Override
		public void remove() {
			//Do nothing
		}
		
	}

}
