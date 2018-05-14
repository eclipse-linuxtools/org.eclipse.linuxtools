/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat, Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public abstract class AbstractDataAdapter {

	/**
	 * @return a Document representing the newly created XML data.
	 */
	public abstract Document getDocument();

	/**
	 * parse the XML data modifying it as necessary to produce the necessary XML
	 * output.
	 */
	public abstract void process();

	/**
	 * @return an InputStream to the newly created XML data.
	 */
	public InputStream getInputStream() {
		InputStream inp = null;
		Source source = new DOMSource(getDocument());
		StringWriter stw = new StringWriter();
		Result result = new StreamResult(stw);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer xformer;
		try {
			xformer = factory.newTransformer();
			xformer.setOutputProperty("indent", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
			xformer.transform(source, result);
			inp = new ByteArrayInputStream(stw.toString().getBytes(StandardCharsets.UTF_8));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return inp;
	}

	@Override
	public String toString() {
		String ret = null;
		Source source = new DOMSource(getDocument());
		StringWriter stw = new StringWriter();
		Result result = new StreamResult(stw);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer xformer;
		try {
			xformer = factory.newTransformer();
			xformer.setOutputProperty("indent", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
			xformer.transform(source, result);
			ret = stw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
