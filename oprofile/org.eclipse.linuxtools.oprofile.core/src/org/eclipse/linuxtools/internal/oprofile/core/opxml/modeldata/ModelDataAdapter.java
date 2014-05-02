/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.linuxtools.internal.oprofile.core.opxml.AbstractDataAdapter;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class takes the XML that is output from 'opreport -X --details' for
 * the current session, and uses that data to modify it into the format
 * expected by the SAX parser.
 */
public class ModelDataAdapter extends AbstractDataAdapter {

    public final static String ID = "id"; //$NON-NLS-1$
    public final static String IDREF = "idref"; //$NON-NLS-1$
    public final static String NAME = "name"; //$NON-NLS-1$
    public final static String COUNT = "count"; //$NON-NLS-1$
    public final static String SAMPLE = "sample"; //$NON-NLS-1$
    public final static String LINE = "line"; //$NON-NLS-1$

    public final static String SYMBOL_DATA = "symboldata"; //$NON-NLS-1$
    public final static String SYMBOL_DETAILS = "symboldetails"; //$NON-NLS-1$
    public final static String SYMBOL = "symbol"; //$NON-NLS-1$

    public final static String FILE = "file"; //$NON-NLS-1$

    public final static String SETUP = "setup"; //$NON-NLS-1$
    public final static String EVENT_SETUP = "eventsetup"; //$NON-NLS-1$
    public final static String TIMER_SETUP = "timersetup"; //$NON-NLS-1$
    public final static String SETUP_COUNT = "setupcount"; //$NON-NLS-1$
    public final static String EVENT_NAME = "eventname"; //$NON-NLS-1$
    public final static String RTC_INTERRUPTS = "rtcinterrupts"; //$NON-NLS-1$

    public final static String PROFILE = "profile"; //$NON-NLS-1$
    public final static String MODEL_DATA = "model-data"; //$NON-NLS-1$

    public final static String MODULE = "module"; //$NON-NLS-1$
    public final static String DEPENDENT = "dependent"; //$NON-NLS-1$

    public final static String BINARY = "binary"; //$NON-NLS-1$
    public final static String IMAGE = "image"; //$NON-NLS-1$

    public final static String SYMBOLS = "symbols"; //$NON-NLS-1$
    public final static String SYMBOL_TABLE = "symboltable"; //$NON-NLS-1$
    public final static String DETAIL_TABLE = "detailtable"; //$NON-NLS-1$

    public final static String DETAIL_DATA = "detaildata"; //$NON-NLS-1$

    private boolean isParseable;
    private Document newDoc; // the document we intend to build
    private Element oldRoot; // the root of the document with data from opreport
    private Element newRoot; // the root of the document we intent to build

    /**
     * Constructor to the ModelAdapter class
     * @param is The input stream to be parsed
     */
    public ModelDataAdapter(InputStream is) {
        isParseable = true;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            try {
                Document oldDoc = builder.parse(is);
                Element elem = (Element) oldDoc.getElementsByTagName(PROFILE).item(0);
                oldRoot = elem;

                newDoc = builder.newDocument();
                newRoot = newDoc.createElement(MODEL_DATA);
                newDoc.appendChild(newRoot);
            } catch (IOException e) {
                isParseable = false;
            } catch (SAXException e) {
                isParseable = false;
            }
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void process (){
        createXML();
    }

    private void createXML() {

        // get the binary name and the image count
        Element oldImage = (Element) oldRoot.getElementsByTagName(BINARY).item(0);
        Element newImage = newDoc.createElement(IMAGE);

        String binName = oldImage.getAttribute(NAME);
        newImage.setAttribute(NAME, binName);

        Element countTag = (Element) oldImage.getElementsByTagName(COUNT).item(0);
        String imageCount = countTag.getTextContent().trim();
        newImage.setAttribute(COUNT, imageCount);

        // There is no setup count in timer mode
        if (!InfoAdapter.hasTimerSupport()){
            // get the count that was used to profile
            Element setupTag = (Element) oldRoot.getElementsByTagName(SETUP).item(0);
            Element eventSetupTag = (Element) setupTag.getElementsByTagName(EVENT_SETUP).item(0);
            String setupcount = eventSetupTag.getAttribute(SETUP_COUNT);
            newImage.setAttribute(SETUP_COUNT, setupcount);
        }

        // these elements contain the data needed to populate the new symbol table
        Element oldSymbolTableTag = (Element) oldRoot.getElementsByTagName(SYMBOL_TABLE).item(0);
        NodeList oldSymbolDataList = oldSymbolTableTag.getElementsByTagName(SYMBOL_DATA);

        Element oldDetailTableTag = (Element) oldRoot.getElementsByTagName(DETAIL_TABLE).item(0);
        NodeList oldDetailTableList = oldDetailTableTag.getElementsByTagName(SYMBOL_DETAILS);

        // parse the data into HashMaps for O(1) lookup time, as opposed to O(n).
        HashMap<String, HashMap<String, String>> oldSymbolDataListMap = parseDataList (oldSymbolDataList);
        HashMap<String, NodeList> oldDetailTableListMap = parseDetailTable (oldDetailTableList);

        // An ArrayList to hold the binary and other modules
        ArrayList<Element> oldImageList = new ArrayList<>();
        // The first element is the original binary!
        oldImageList.add(oldImage);

        NodeList oldModuleList = oldImage.getElementsByTagName(MODULE);
        // Set up the dependent tag for any modules run by this binary
        Element dependentTag = newDoc.createElement(DEPENDENT);
        if (oldModuleList.getLength() > 0){
            dependentTag.setAttribute(COUNT, "0"); //$NON-NLS-1$

            for (int t = 0; t < oldModuleList.getLength(); t++){
                oldImageList.add((Element)oldModuleList.item(t));
            }
        }

        // iterate through all (binary/modules)
        for (Element oldImg : oldImageList) {
            Element newImg;
            if (oldImg.getTagName().equals(BINARY)){
                newImg = newImage;
            }else{
                newImg = newDoc.createElement(IMAGE);

                String imgName = oldImg.getAttribute(NAME);
                newImg.setAttribute(NAME, imgName);

                Element modCountTag = (Element) oldImg.getElementsByTagName(COUNT).item(0);
                String imgCount = modCountTag.getTextContent().trim();
                newImg.setAttribute(COUNT, imgCount);
            }

            Element newSymbolsTag = newDoc.createElement(SYMBOLS);

            // these elements contain the data needed to populate the new symbol table
            NodeList oldSymbolList = oldImg.getElementsByTagName(SYMBOL);

            // iterate through all symbols
            for (int i = 0; i < oldSymbolList.getLength(); i++) {
                Element oldSymbol = (Element) oldSymbolList.item(i);

                /**
                 * The original binary is a parent for all symbols
                 * We only want library function calls under their respective
                 * modules, and not under the original binary as well.
                 */
                if (!oldSymbol.getParentNode().isSameNode(oldImg)){
                    continue;
                }

                Element newSymbol = newDoc.createElement(SYMBOL);
                String idref = oldSymbol.getAttribute(IDREF);
                String symbolCount = ((Element) oldSymbol.getElementsByTagName(COUNT).item(0)).getTextContent().trim();
                newSymbol.setAttribute(COUNT, symbolCount);

                // get the symboltable entry corresponding to the id of this symbol
                HashMap<String, String> symbolData = oldSymbolDataListMap.get(idref);
                newSymbol.setAttribute(NAME, symbolData.get(NAME));
                newSymbol.setAttribute(FILE, symbolData.get(FILE));
                newSymbol.setAttribute(LINE, symbolData.get(LINE));

                // get the symboldetails entry corresponding to the id of this symbol
                NodeList detailDataList = oldDetailTableListMap.get(idref);

                // go through the detail data of each symbol's details
                HashMap<String, Element> tmp = new HashMap<>();
                // temporary place to store the elements for sorting
                TreeSet<Element> sorted = new TreeSet<>(SAMPLE_COUNT_ORDER);
                for (int l = 0; l < detailDataList.getLength(); l++) {

                    Element detailData = (Element) detailDataList.item(l);
                    String sampleFile = detailData.getAttribute(FILE);
                    String sampleLine = detailData.getAttribute(LINE);

                    // The sample has a line number but no file
                    // This means that the file is the same as the symbol (parent)
                    if (sampleFile.equals("") && !sampleLine.equals("")){ //$NON-NLS-1$ $NON-NLS-2$
                        sampleFile = symbolData.get(FILE);
                    }else{
                        if (sampleFile.equals("")){ //$NON-NLS-1$
                            sampleFile = "??"; //$NON-NLS-1$
                        }
                        if (sampleLine.equals("")){ //$NON-NLS-1$
                            sampleLine = "0"; //$NON-NLS-1$
                        }
                    }
                    Element detailDataCount = (Element) detailData.getElementsByTagName(COUNT).item(0);
                    String count = detailDataCount.getTextContent().trim();

                    // if a sample at this line already exists then increase count for that line.
                    if (tmp.containsKey(sampleLine)) {
                        Element elem = (Element) tmp.get(sampleLine).getElementsByTagName(COUNT).item(0);
                        int val = Integer.parseInt(elem.getTextContent().trim()) + Integer.parseInt(count);
                        elem.setTextContent(String.valueOf(val));
                    } else {
                        Element sampleTag = newDoc.createElement(SAMPLE);

                        Element fileTag = newDoc.createElement(FILE);
                        fileTag.setTextContent(sampleFile);

                        Element lineTag = newDoc.createElement(LINE);
                        lineTag.setTextContent(sampleLine);

                        Element sampleCountTag = newDoc.createElement(COUNT);
                        sampleCountTag.setTextContent(count);

                        sampleTag.appendChild(fileTag);
                        sampleTag.appendChild(lineTag);
                        sampleTag.appendChild(sampleCountTag);

                        tmp.put(sampleLine, sampleTag);
                    }
                }

                // add the elements to the sorter
                for (Element elem : tmp.values()) {
                    sorted.add(elem);
                }

                // append the elements in sorted order
                for (Element e : sorted) {
                    newSymbol.appendChild(e);
                }

                newSymbolsTag.appendChild(newSymbol);
            }

            newImg.appendChild(newSymbolsTag);
            // If this is a module, attach it to the dependent tag
            if (oldImg.getTagName().equals(MODULE)){
                dependentTag.appendChild(newImg);
                int currVal =  Integer.parseInt(dependentTag.getAttribute(COUNT));
                int val =  Integer.parseInt(newImg.getAttribute(COUNT));
                dependentTag.setAttribute(COUNT, String.valueOf(currVal + val));
            }else{
                newRoot.appendChild(newImg);
            }
        }

        if (oldModuleList.getLength() > 0){
            newImage.appendChild(dependentTag);
        }

    }

    /**
     *
     * @param oldDetailTableList the list of 'symboldetails' tags within detailtable
     * @return a HashMap where the key is a function id and the value is a NodeList
     * containing a list of the 'detaildata' tags that contain sample information.
     */
    private HashMap<String, NodeList> parseDetailTable(NodeList oldDetailTableList) {
        HashMap<String, NodeList> ret = new HashMap<> ();
        for (int i = 0; i < oldDetailTableList.getLength(); i++){
            Element symbolDetails = (Element) oldDetailTableList.item(i);
            String id = symbolDetails.getAttribute(ID);
            NodeList detailDataList = symbolDetails.getElementsByTagName(DETAIL_DATA);
            ret.put(id, detailDataList);
        }
        return ret;
    }

    /**
     *
     * @param oldSymbolDataList the list of 'symboldata' tags within symboltable
     * @return a Hashmap where the key is a function id and the value is a HashMap
     * with various parameters of data
     */
    private HashMap<String, HashMap<String, String>> parseDataList(NodeList oldSymbolDataList) {
        HashMap<String, HashMap<String,String>> ret = new HashMap<> ();
        for (int j = 0; j < oldSymbolDataList.getLength(); j++){
            HashMap<String,String> tmp = new HashMap<> ();
            Element symbolData = (Element) oldSymbolDataList.item(j);
            String id = symbolData.getAttribute(ID);
            String name = symbolData.getAttribute(NAME);
            String file = symbolData.getAttribute(FILE);
            if (file.equals("")){ //$NON-NLS-1$
                file = "??"; //$NON-NLS-1$
            }
            String line = symbolData.getAttribute(LINE);
            if (line.equals("")){ //$NON-NLS-1$
                line = "0"; //$NON-NLS-1$
            }
            tmp.put(NAME, name);
            tmp.put(FILE, file);
            tmp.put(LINE, line);
            ret.put(id, tmp);
        }
        return ret;
    }

    @Override
    public Document getDocument() {
        return newDoc;
    }

    /**
     * Helper class to sort the samples of a given symbol in descending order from largest
     * to smallest
     */
    private static final Comparator<Element> SAMPLE_COUNT_ORDER = new Comparator<Element>()
    {
        @Override
        public int compare(Element a, Element b) {
            // sort from largest to smallest count in descending order
            // items with the same count are sorted by line number from smallest
            // to largest in descending order
            Element a_countTag = (Element) a.getElementsByTagName(COUNT).item(0);
            Element b_countTag = (Element) b.getElementsByTagName(COUNT).item(0);
            Element a_LineTag = (Element) a.getElementsByTagName(LINE).item(0);
            Element b_LineTag = (Element) b.getElementsByTagName(LINE).item(0);

            Integer a_count = Integer.parseInt(a_countTag.getTextContent().trim());
            Integer b_count = Integer.parseInt(b_countTag.getTextContent().trim());
            Integer a_line = Integer.parseInt(a_LineTag.getTextContent().trim());
            Integer b_line = Integer.parseInt(b_LineTag.getTextContent().trim());

            if (a_count.compareTo(b_count) == 0){
                return a_line.compareTo(b_line);
            }
            return -a_count.compareTo(b_count);
        }
    };

    /**
     * Returns if parseable
     * @return isParseable boolean variable
     */
    public boolean isParseable() {
        return isParseable;
    }

}
