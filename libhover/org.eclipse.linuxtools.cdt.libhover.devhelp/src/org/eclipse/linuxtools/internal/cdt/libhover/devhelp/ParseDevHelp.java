/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat Inc. and others.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.FuncFoundSaxException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParseDevHelp {

    public static class DevHelpParser {

        private static final class NullEntityResolver implements EntityResolver {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) {
                return new InputSource(new StringReader("")); //$NON-NLS-1$
            }
        }

        private final List<Path> books = new ArrayList<>();
        private LibHoverInfo libhover;
        private boolean debug;
        private NullEntityResolver entityResolver = new NullEntityResolver();
        private DocumentBuilderFactory factory;

        public DevHelpParser(String paths) {
            this(findAllDevhelpBooks(paths));
        }

        public DevHelpParser(List<Path> books) {
            this(books, false);
        }

        public DevHelpParser(List<Path> books, boolean debug) {
            this.books.addAll(books);
            this.libhover = new LibHoverInfo();
            this.debug = debug;
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
        }

        public LibHoverInfo getLibHoverInfo() {
            return libhover;
        }

        public LibHoverInfo parse(IProgressMonitor monitor) {
            monitor.beginTask(Messages.ParseDevHelp_ParseTask, books.size());
            Collections.sort(books);
            for (Path book : books) {
                if (monitor.isCanceled()) {
                    return null;
                }
                monitor.setTaskName(MessageFormat.format(Messages.ParseDevHelp_ParseFileTask,
                        book.getParent().getFileName().toString()));
                parse(book.toAbsolutePath(), monitor);
                monitor.worked(1);
            }
            return libhover;
        }

        private void parseLinks(HashMap<String, String> funcMap, String fileName, Path path, LibHoverInfo libhover) {
            InputStream reader = null;
            AbstractSAXParser parser = null;
            try {
                reader = Files.newInputStream(path.getParent().resolve(fileName));
                if (funcMap.size() == 1 && funcMap.containsKey("name")) { //$NON-NLS-1$
                	parser = new HTMLSAXParser(funcMap);
                } else {
                	parser = new HTMLSAXParserOld(funcMap);
                }
                try {
                    parser.parse(new InputSource(reader));
                } catch (FuncFoundSaxException e) {
                    // ignore because this is just how we shorten parse time
                }
                reader.close();
                TreeMap<String, FunctionInfo> finfos = ((DevHelpSAXParser)parser).getFunctionInfos();
                if (finfos != null) {
                    if (debug) {
                        System.out.println(parser.toString());
                    }
                    libhover.functions.putAll(finfos);
                }
            } catch (IOException e) {
                // ignore
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }

        private void parse(Path path, IProgressMonitor monitor) {
            try(InputStream stream = Files.newInputStream(path)) {
                HashMap<String, HashMap<String,String>> files = new HashMap<>();
                DocumentBuilder builder = factory.newDocumentBuilder();
                builder.setEntityResolver(entityResolver);
                Document doc = builder.parse(stream);
                NodeList bookNodes = doc.getElementsByTagName("book"); //$NON-NLS-1$
                for (int x = 0; x < bookNodes.getLength(); ++x) {
                    Node n = bookNodes.item(x);
                    NamedNodeMap m = n.getAttributes();
                    Node language = m.getNamedItem("language"); //$NON-NLS-1$
                    if (language != null && !language.getNodeValue().equals("c")) { //$NON-NLS-1$
                        return;
                    }
                }
                NodeList nl = doc.getElementsByTagName("keyword"); // $NON-NLS-1$ //$NON-NLS-1$
                for (int i = 0; i < nl.getLength(); ++i) {
                    if (monitor.isCanceled())
                        return;
                    Node n = nl.item(i);
                    NamedNodeMap m = n.getAttributes();
                    Node type = m.getNamedItem("type"); // $NON-NLS-1$ //$NON-NLS-1$
                    if (type != null) {
                        String typeName = type.getNodeValue();
                        // Look for all function references in the devhelp file
                        if (typeName.equals("function")) { //$NON-NLS-1$
                            // Each function reference will have a link associated with it
                            Node name = m.getNamedItem("name"); // $NON-NLS-1$ //$NON-NLS-1$
                            Node link = m.getNamedItem("link"); // $NON-NLS-1$ //$NON-NLS-1$
                            if (name != null && link != null) {
                                // Clean up the name and make sure it isn't a non-C-function
                                String nameValue = name.getNodeValue();
                                nameValue = nameValue.replaceAll("\\(.*\\);+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                if (!nameValue.contains("::") && !nameValue.startsWith("enum ") //$NON-NLS-1$ //$NON-NLS-2$
                                        && !nameValue.contains("\"")) { //$NON-NLS-1$
                                    String linkValue = link.getNodeValue();
                                    String[] linkParts = linkValue.split("#"); //$NON-NLS-1$
                                    // Check to see if the file referred to by the link has been seen before
                                    // If not, create a new function list for it
                                    HashMap<String, String> funcMap = files.get(linkParts[0]);
                                    if (funcMap == null) {
                                        funcMap = new HashMap<>();
                                        files.put(linkParts[0], funcMap);
                                    }
                                    if (linkParts.length < 2) {
                                        funcMap.put("name", nameValue); //$NON-NLS-1$
                                    } else {
                                        // Add the function to the function list for the link file
                                        funcMap.put(linkParts[1], nameValue);
                                    }
                                }
                            }
                        }
                    }
                }
                // For each different file found in function links in the devhelp file,
                // parse it and get all function info that is referred to
                for (Map.Entry<String, HashMap<String, String>> entry : files.entrySet()) {
                    String fname = entry.getKey();
                    HashMap<String, String> funcMap = entry.getValue();
                    parseLinks(funcMap, fname, path, libhover);
                }
            } catch (FileNotFoundException e1) {
                // ignore
            } catch (ParserConfigurationException|SAXException|IOException e) {
                e.printStackTrace();
            }
        }
    }

	/**
	 * A utility to find all the devhelp indexes within the given set of
	 * directories.
	 * 
	 * @param paths a string containing a set of paths, delimited by the
	 *              platform-specific path separator
	 * @return a list of paths to devhelp indexes
	 */
	public static List<Path> findAllDevhelpBooks(String paths) {
		List<Path> books = new ArrayList<>();
		if (paths != null) {
			for (String path : paths.split(File.pathSeparator)) {
				Path p = Path.of(path);
				if (!Files.isDirectory(p)) {
					continue;
				}
				try (Stream<Path> htmlDirs = Files.walk(p, 1)) {
					books.addAll(htmlDirs.map(dir -> {
						return dir.resolve(dir.getFileName() + ".devhelp2"); //$NON-NLS-1$
					}).filter(Files::isReadable).toList());
				} catch (IOException e) {
					// No big deal, we just might not have permission to read this directory, for
					// example, carry on to the next one
				}
			}
		}
		return books;
	}

    public static void main(String[] args) {
        long startParse = System.currentTimeMillis();
        String devhelpDirs = "/usr/share/doc:/usr/share/gtk-doc/html:/usr/share/devhelp/books"; //$NON-NLS-1$
        List<Path> books = findAllDevhelpBooks(devhelpDirs);
        DevHelpParser p = new DevHelpParser(books, false);
        p.parse(new NullProgressMonitor());
        long endParse = System.currentTimeMillis();
        System.out.println("Parse Complete:"+(endParse-startParse)); //$NON-NLS-1$
        long startSerialize = System.currentTimeMillis();
        LibHoverInfo hover = p.getLibHoverInfo();
        try {
            // Now, output the LibHoverInfo for caching later
            Path location = Path.of(args[0], "org.eclipse.linuxtools.cdt.libhover", "C"); //$NON-NLS-1$ //$NON-NLS-2$
            Files.createDirectories(location);
            try (OutputStream f = Files.newOutputStream(location.resolve("devhelp.libhover")); //$NON-NLS-1$
                    ObjectOutputStream out = new ObjectOutputStream(f)) {
                out.writeObject(hover);
            }
        } catch(IOException e) {
        }
        long endSerialize = System.currentTimeMillis();

        System.out.println("Parse Complete:"+(endSerialize-startSerialize)); //$NON-NLS-1$

    }

}
