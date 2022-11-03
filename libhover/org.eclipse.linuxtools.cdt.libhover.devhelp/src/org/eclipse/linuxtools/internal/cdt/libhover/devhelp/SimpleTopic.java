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
 * Alexander Kurtakov - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleTopic implements ITopic {

    private static XPath xpath = XPathFactory.newInstance().newXPath();

    private String bookName;
    private Node node;
    private final List<ITopic> subTopics = new ArrayList<>();

    public SimpleTopic(String bookName, Node node) {
        this.bookName = bookName;
        this.node = node;
        initSubtopics();
    }

    private void initSubtopics() {
        NodeList nodes = xpathEvalNodes("sub", node); //$NON-NLS-1$
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node innerNode = nodes.item(i);
                subTopics.add(new SimpleTopic(bookName, innerNode));
            }
        }

    }

    private NodeList xpathEvalNodes(String path, Node docroot) {
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
        String link = ""; //$NON-NLS-1$
        try {
            link = xpath.evaluate("@link", node); //$NON-NLS-1$
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return "/" + DevHelpPlugin.PLUGIN_ID + "/" + bookName + "/" + link; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String getLabel() {
        try {
            return xpath.evaluate("@name", node); //$NON-NLS-1$
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public ITopic[] getSubtopics() {
        return subTopics.toArray(new ITopic[subTopics.size()]);
    }

}
