/*******************************************************************************
 * Copyright (c) 2012, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DevHelpTopic implements ITopic {

    private static XPath xpath = XPathFactory.newInstance().newXPath();

    private Path path;
    private String bookName;
    private String label;
    private String link;
    private final List<ITopic> subTopics = new ArrayList<>();

    public DevHelpTopic(Path path) {
        this.path = path;
        // Use the directory name as a short identifier for the book
        bookName = path.getParent().getFileName().toString();
        init();
    }

    private void init() {
        try {
            DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
            docfactory.setValidating(false);
            docfactory.setFeature("http://xml.org/sax/features/namespaces", //$NON-NLS-1$
                    false);
            docfactory.setFeature("http://xml.org/sax/features/validation", //$NON-NLS-1$
                    false);
            docfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", //$NON-NLS-1$
                    false);
            docfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", //$NON-NLS-1$
                    false);
            DocumentBuilder docbuilder = docfactory.newDocumentBuilder();
            Document docroot = docbuilder.parse(path.toFile());

            // set label and index link
            String title = xpathEval("/book/@title", docroot); //$NON-NLS-1$
            if (title != null && !title.isBlank()) {
                label = title;
            }
            link = xpathEval("/book/@link", docroot); //$NON-NLS-1$

            // set subtopics
            NodeList nodes = xpathEvalNodes("/book/chapters/sub", docroot); //$NON-NLS-1$
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                subTopics.add(new SimpleTopic(bookName, node));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Platform.getLog(FrameworkUtil.getBundle(getClass()))
                    .error(MessageFormat.format(Messages.DevHelpTopic_ParseXMLError, path), e);
        }
    }

    private String xpathEval(String path, Document docroot) {
        String result = ""; //$NON-NLS-1$
        try {
            result = xpath.evaluate(path, docroot);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private NodeList xpathEvalNodes(String path, Document docroot) {
        NodeList result = null;
        try {
            result = (NodeList) xpath.evaluate(path, docroot,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isEnabled(IEvaluationContext context) {
        return true;
    }

    @Override
    public IUAElement[] getChildren() {
        return getSubtopics();
    }

    @Override
    public String getHref() {
        return "/" + DevHelpPlugin.PLUGIN_ID + "/" + bookName + "/" + link; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String getLabel() {
        return Objects.requireNonNullElse(label, bookName);
    }

    @Override
    public ITopic[] getSubtopics() {
        return subTopics.toArray(new ITopic[subTopics.size()]);
    }
}