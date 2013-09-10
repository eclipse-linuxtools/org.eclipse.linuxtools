/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSample;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSymbol;
import org.eclipse.linuxtools.oprofile.tests.TestPlugin;
import org.eclipse.linuxtools.oprofile.tests.TestingOpModelRoot;
import org.junit.Before;
import org.junit.Test;

public class TestUiDataModel {

	private static class TestingUiModelRoot extends UiModelRoot {
		@Override
		protected OpModelEvent[] getModelDataEvents() {
			TestingOpModelRoot modelRoot = new TestingOpModelRoot();
			modelRoot.refreshModel();
			return modelRoot.getEvents();
		}
	}

	private static class TestingUiModelRoot2 extends UiModelRoot {
		@Override
		protected OpModelEvent[] getModelDataEvents() {
			return null;
		}
	}

	private TestingUiModelRoot _uiModelRoot;
	private TestingUiModelRoot2 _uiModelRoot2;


	@Before
	public void setUp() {
		_uiModelRoot = new TestingUiModelRoot();
		_uiModelRoot.refreshModel();

		_uiModelRoot2 = new TestingUiModelRoot2();
		_uiModelRoot2.refreshModel();
	}

	@Test
	public void testParse() {
		/* test UiModelRoot */
		assertNull(_uiModelRoot2.getLabelImage());
		assertNull(_uiModelRoot2.getParent());
		assertNull(_uiModelRoot2.getLabelText());
		assertTrue(_uiModelRoot2.hasChildren());
		IUiModelElement r2_events[] = _uiModelRoot2.getChildren();
		assertNotNull(r2_events);
		assertEquals(1, r2_events.length);
		IUiModelElement r2_event1 = r2_events[0];
		assertFalse(r2_event1.hasChildren());
		assertNull(r2_event1.getChildren());
		assertNull(r2_event1.getParent());
		assertNotNull(r2_event1.getLabelImage());
		assertNotNull(r2_event1.getLabelText());

		assertNull(_uiModelRoot.getLabelImage());
		assertNull(_uiModelRoot.getParent());
		assertNull(_uiModelRoot.getLabelText());
		assertTrue(_uiModelRoot.hasChildren());

		/* test UiModelEvent */
		IUiModelElement events[] = _uiModelRoot.getChildren();
		assertNotNull(events);
		assertEquals(3, events.length);
		assertNotNull(events[0]);
		assertNotNull(events[1]);
		assertNotNull(events[2]);

		assertEquals(TestingOpModelRoot.NAME_E1, events[0].toString());
		assertEquals(TestingOpModelRoot.NAME_E1, events[0].getLabelText());
		assertTrue(events[0].hasChildren());
		assertNull(events[0].getParent());		//events are top level tree elements
		assertNotNull(events[0].getLabelImage());

		assertEquals(TestingOpModelRoot.NAME_E2, events[1].toString());
		assertEquals(TestingOpModelRoot.NAME_E2, events[1].getLabelText());
		assertTrue(events[1].hasChildren());
		assertNull(events[1].getParent());		//events are top level tree elements
		assertNotNull(events[1].getLabelImage());

		assertTrue(events[2].toString().isEmpty());
		assertTrue(events[2].getLabelText().isEmpty());
		assertFalse(events[2].hasChildren());
		assertNull(events[2].getParent());		//events are top level tree elements
		assertNotNull(events[2].getLabelImage());

		/* test UiModelSession */
		IUiModelElement[] e1_sessions = events[0].getChildren(), e2_sessions = events[1].getChildren();
		assertNotNull(e1_sessions);
		assertNotNull(e2_sessions);
		assertEquals(1, e1_sessions.length);
		assertEquals(4, e2_sessions.length);
		assertNotNull(e1_sessions[0]);
		assertNotNull(e2_sessions[0]);
		assertNotNull(e2_sessions[1]);
		assertNotNull(e2_sessions[2]);
		assertNotNull(e2_sessions[3]);

		assertEquals(TestingOpModelRoot.NAME_E1_S1, e1_sessions[0].toString());
		assertEquals(TestingOpModelRoot.NAME_E1_S1, e1_sessions[0].getLabelText());
		assertFalse(((UiModelSession)e1_sessions[0]).isDefaultSession());
		assertTrue(e1_sessions[0].hasChildren());
		assertEquals(events[0], e1_sessions[0].getParent());
		assertNotNull(e1_sessions[0].getLabelImage());

		assertEquals(TestingOpModelRoot.NAME_E2_S1, e2_sessions[0].toString());
		assertEquals(TestingOpModelRoot.NAME_E2_S1, e2_sessions[0].getLabelText());
		assertFalse(((UiModelSession)e2_sessions[0]).isDefaultSession());
		assertTrue(e2_sessions[0].hasChildren());
		assertEquals(events[1], e2_sessions[0].getParent());
		assertNotNull(e2_sessions[0].getLabelImage());

		assertEquals(TestingOpModelRoot.NAME_E2_S2, e2_sessions[1].toString());
		assertEquals(TestingOpModelRoot.NAME_E2_S2, e2_sessions[1].getLabelText());
		assertFalse(((UiModelSession)e2_sessions[1]).isDefaultSession());
		assertTrue(e2_sessions[1].hasChildren());
		assertEquals(events[1], e2_sessions[1].getParent());
		assertNotNull(e2_sessions[1].getLabelImage());

		assertEquals(TestingOpModelRoot.NAME_E2_S3, e2_sessions[2].toString());
		assertEquals(TestingOpModelRoot.NAME_E2_S3, e2_sessions[2].getLabelText());
		assertFalse(((UiModelSession)e2_sessions[2]).isDefaultSession());
		assertTrue(e2_sessions[2].hasChildren());
		assertEquals(events[1], e2_sessions[2].getParent());
		assertNotNull(e2_sessions[2].getLabelImage());

		assertEquals(TestingOpModelRoot.NAME_E2_S4, e2_sessions[3].toString());
		assertEquals(TestingOpModelRoot.NAME_E2_S4, e2_sessions[3].getLabelText());
		assertFalse(((UiModelSession)e2_sessions[3]).isDefaultSession());
		assertFalse(e2_sessions[3].hasChildren());
		assertEquals(events[1], e2_sessions[3].getParent());
		assertNotNull(e2_sessions[3].getLabelImage());

		/* test UiModelImage and UiModelDependent */
		IUiModelElement[] e1_s1_images = e1_sessions[0].getChildren(),
							e2_s1_images = e2_sessions[0].getChildren(),
							e2_s2_images = e2_sessions[1].getChildren(),
							e2_s3_images = e2_sessions[2].getChildren(),
							e2_s4_images = e2_sessions[3].getChildren();

		assertNotNull(e1_s1_images);
		assertNotNull(e2_s1_images);
		assertNotNull(e2_s2_images);
		assertNotNull(e2_s3_images);
		assertNotNull(e2_s4_images);
		assertEquals(2, e1_s1_images.length);	//#2 is a dep image
		assertEquals(2, e2_s1_images.length);	//#2 is a dep image
		assertEquals(1, e2_s2_images.length);
		assertEquals(1, e2_s3_images.length);
		assertEquals(1, e2_s4_images.length);
		assertNotNull(e1_s1_images[0]);
		assertNotNull(e1_s1_images[1]);
		assertNotNull(e2_s1_images[0]);
		assertNotNull(e2_s1_images[1]);
		assertNotNull(e2_s2_images[0]);
		assertNotNull(e2_s3_images[0]);
		assertNull(e2_s4_images[0]);

		//dont test string output -- strings are i18n'd and L10n'd
		assertNotNull(e1_s1_images[0].toString());
		assertNotNull(e1_s1_images[0].getLabelText());
		assertTrue(e1_s1_images[0].hasChildren());
		assertEquals(e1_sessions[0], e1_s1_images[0].getParent());
		assertNotNull(e1_s1_images[0].getLabelImage());

		assertNotNull(e1_s1_images[1].toString());
		assertNotNull(e1_s1_images[1].getLabelText());
		assertTrue(e1_s1_images[1].hasChildren());
		assertEquals(e1_sessions[0], e1_s1_images[1].getParent());
		assertNotNull(e1_s1_images[1].getLabelImage());

		assertNotNull(e2_s1_images[0].toString());
		assertNotNull(e2_s1_images[0].getLabelText());
		assertTrue(e2_s1_images[0].hasChildren());
		assertEquals(e2_sessions[0], e2_s1_images[0].getParent());
		assertNotNull(e2_s1_images[0].getLabelImage());

		assertNotNull(e2_s1_images[1].toString());
		assertNotNull(e2_s1_images[1].getLabelText());
		assertTrue(e2_s1_images[1].hasChildren());
		assertEquals(e2_sessions[0], e2_s1_images[1].getParent());
		assertNotNull(e2_s1_images[1].getLabelImage());

		assertNotNull(e2_s2_images[0].toString());
		assertNotNull(e2_s2_images[0].getLabelText());
		assertTrue(e2_s2_images[0].hasChildren());
		assertEquals(e2_sessions[1], e2_s2_images[0].getParent());
		assertNotNull(e2_s2_images[0].getLabelImage());

		assertNotNull(e2_s3_images[0].toString());
		assertNotNull(e2_s3_images[0].getLabelText());
		assertFalse(e2_s3_images[0].hasChildren());
		assertEquals(e2_sessions[2], e2_s3_images[0].getParent());
		assertNotNull(e2_s3_images[0].getLabelImage());

		/* test UiModelSymbol */
		IUiModelElement[] e1_s1_i1_symbols = e1_s1_images[0].getChildren(),
							e1_s1_i2_depimages = e1_s1_images[1].getChildren(),
							e2_s1_i1_symbols = e2_s1_images[0].getChildren(),
							e2_s1_i2_depimages = e2_s1_images[1].getChildren(),
							e2_s2_i1_symbols = e2_s2_images[0].getChildren();

		assertNotNull(e1_s1_i1_symbols);
		assertNotNull(e1_s1_i2_depimages);
		assertNotNull(e2_s1_i1_symbols);
		assertNotNull(e2_s1_i2_depimages);
		assertNotNull(e2_s2_i1_symbols);
		assertEquals(2, e1_s1_i1_symbols.length);
		assertEquals(4, e1_s1_i2_depimages.length);
		assertEquals(2, e2_s1_i1_symbols.length);
		assertEquals(4, e2_s1_i2_depimages.length);
		assertEquals(2, e2_s2_i1_symbols.length);
		assertNotNull(e1_s1_i1_symbols[0]);
		assertNotNull(e1_s1_i1_symbols[1]);
		assertNotNull(e1_s1_i2_depimages[0]);
		assertNotNull(e1_s1_i2_depimages[1]);
		assertNotNull(e1_s1_i2_depimages[2]);
		assertNotNull(e1_s1_i2_depimages[3]);
		assertNotNull(e2_s1_i1_symbols[0]);
		assertNotNull(e2_s1_i1_symbols[1]);
		assertNotNull(e2_s1_i2_depimages[0]);
		assertNotNull(e2_s1_i2_depimages[1]);
		assertNotNull(e2_s1_i2_depimages[2]);
		assertNotNull(e2_s1_i2_depimages[3]);
		assertNotNull(e2_s2_i1_symbols[0]);
		assertNotNull(e2_s2_i1_symbols[1]);

		assertNotNull(e1_s1_i1_symbols[0].toString());
		assertNotNull(e1_s1_i1_symbols[0].getLabelText());
		assertEquals(TestPlugin.SYMBOL1_FILENAME, ((UiModelSymbol)e1_s1_i1_symbols[0]).getFileName());
		assertTrue(e1_s1_i1_symbols[0].hasChildren());
		assertEquals(e1_s1_images[0], e1_s1_i1_symbols[0].getParent());
		assertNotNull(e1_s1_i1_symbols[0].getLabelImage());

		assertNotNull(e1_s1_i1_symbols[1].toString());
		assertNotNull(e1_s1_i1_symbols[1].getLabelText());
		assertEquals(TestPlugin.SYMBOL2_FILENAME, ((UiModelSymbol)e1_s1_i1_symbols[1]).getFileName());
		assertTrue(e1_s1_i1_symbols[1].hasChildren());
		assertEquals(e1_s1_images[0], e1_s1_i1_symbols[1].getParent());
		assertNotNull(e1_s1_i1_symbols[1].getLabelImage());

		assertNotNull(e1_s1_i2_depimages[0].toString());
		assertNotNull(e1_s1_i2_depimages[0].getLabelText());
		assertFalse(e1_s1_i2_depimages[0].hasChildren());
		assertEquals(e1_s1_images[1], e1_s1_i2_depimages[0].getParent());
		assertNotNull(e1_s1_i2_depimages[0].getLabelImage());

		assertNotNull(e1_s1_i2_depimages[1].toString());
		assertNotNull(e1_s1_i2_depimages[1].getLabelText());
		assertTrue(e1_s1_i2_depimages[1].hasChildren());
		assertEquals(e1_s1_images[1], e1_s1_i2_depimages[1].getParent());
		assertNotNull(e1_s1_i2_depimages[1].getLabelImage());

		assertNotNull(e1_s1_i2_depimages[2].toString());
		assertNotNull(e1_s1_i2_depimages[2].getLabelText());
		assertFalse(e1_s1_i2_depimages[2].hasChildren());
		assertEquals(e1_s1_images[1], e1_s1_i2_depimages[2].getParent());
		assertNotNull(e1_s1_i2_depimages[2].getLabelImage());

		assertNotNull(e1_s1_i2_depimages[3].toString());
		assertNotNull(e1_s1_i2_depimages[3].getLabelText());
		assertTrue(e1_s1_i2_depimages[3].hasChildren());
		assertEquals(e1_s1_images[1], e1_s1_i2_depimages[3].getParent());
		assertNotNull(e1_s1_i2_depimages[3].getLabelImage());

		assertNotNull(e2_s1_i1_symbols[0].toString());
		assertNotNull(e2_s1_i1_symbols[0].getLabelText());
		assertEquals(TestPlugin.SYMBOL1_FILENAME, ((UiModelSymbol)e2_s1_i1_symbols[0]).getFileName());
		assertTrue(e2_s1_i1_symbols[0].hasChildren());
		assertEquals(e2_s1_images[0], e2_s1_i1_symbols[0].getParent());
		assertNotNull(e2_s1_i1_symbols[0].getLabelImage());

		assertNotNull(e2_s1_i1_symbols[1].toString());
		assertNotNull(e2_s1_i1_symbols[1].getLabelText());
		assertEquals(TestPlugin.SYMBOL2_FILENAME, ((UiModelSymbol)e2_s1_i1_symbols[1]).getFileName());
		assertTrue(e2_s1_i1_symbols[1].hasChildren());
		assertEquals(e2_s1_images[0], e2_s1_i1_symbols[1].getParent());
		assertNotNull(e2_s1_i1_symbols[1].getLabelImage());

		assertNotNull(e2_s1_i2_depimages[0].toString());
		assertNotNull(e2_s1_i2_depimages[0].getLabelText());
		assertFalse(e2_s1_i2_depimages[0].hasChildren());
		assertEquals(e2_s1_images[1], e2_s1_i2_depimages[0].getParent());
		assertNotNull(e2_s1_i2_depimages[0].getLabelImage());

		assertNotNull(e2_s1_i2_depimages[1].toString());
		assertNotNull(e2_s1_i2_depimages[1].getLabelText());
		assertTrue(e2_s1_i2_depimages[1].hasChildren());
		assertEquals(e2_s1_images[1], e2_s1_i2_depimages[1].getParent());
		assertNotNull(e2_s1_i2_depimages[1].getLabelImage());

		assertNotNull(e2_s1_i2_depimages[2].toString());
		assertNotNull(e2_s1_i2_depimages[2].getLabelText());
		assertFalse(e2_s1_i2_depimages[2].hasChildren());
		assertEquals(e2_s1_images[1], e2_s1_i2_depimages[2].getParent());
		assertNotNull(e2_s1_i2_depimages[2].getLabelImage());

		assertNotNull(e2_s1_i2_depimages[3].toString());
		assertNotNull(e2_s1_i2_depimages[3].getLabelText());
		assertTrue(e2_s1_i2_depimages[3].hasChildren());
		assertEquals(e2_s1_images[1], e2_s1_i2_depimages[3].getParent());
		assertNotNull(e2_s1_i2_depimages[3].getLabelImage());

		assertNotNull(e2_s2_i1_symbols[0].toString());
		assertNotNull(e2_s2_i1_symbols[0].getLabelText());
		assertEquals(TestPlugin.SYMBOL1_FILENAME, ((UiModelSymbol)e2_s2_i1_symbols[0]).getFileName());
		assertTrue(e2_s2_i1_symbols[0].hasChildren());
		assertEquals(e2_s2_images[0], e2_s2_i1_symbols[0].getParent());
		assertNotNull(e2_s2_i1_symbols[0].getLabelImage());

		assertNotNull(e2_s2_i1_symbols[1].toString());
		assertNotNull(e2_s2_i1_symbols[1].getLabelText());
		assertEquals(TestPlugin.SYMBOL2_FILENAME, ((UiModelSymbol)e2_s2_i1_symbols[1]).getFileName());
		assertTrue(e2_s2_i1_symbols[1].hasChildren());
		assertEquals(e2_s2_images[0], e2_s2_i1_symbols[1].getParent());
		assertNotNull(e2_s2_i1_symbols[1].getLabelImage());

		/* test the symbols from the dep images */
		IUiModelElement[] e1_s1_i2_dep1_symbols = e1_s1_i2_depimages[0].getChildren(),
							e1_s1_i2_dep2_symbols = e1_s1_i2_depimages[1].getChildren(),
							e1_s1_i2_dep3_symbols = e1_s1_i2_depimages[2].getChildren(),
							e1_s1_i2_dep4_symbols = e1_s1_i2_depimages[3].getChildren(),
							e2_s1_i2_dep1_symbols = e2_s1_i2_depimages[0].getChildren(),
							e2_s1_i2_dep2_symbols = e2_s1_i2_depimages[1].getChildren(),
							e2_s1_i2_dep3_symbols = e2_s1_i2_depimages[2].getChildren(),
							e2_s1_i2_dep4_symbols = e2_s1_i2_depimages[3].getChildren();

    	assertNull(e1_s1_i2_dep1_symbols);
		assertNotNull(e1_s1_i2_dep2_symbols);
		assertNull(e1_s1_i2_dep3_symbols);
		assertNotNull(e1_s1_i2_dep4_symbols);
		assertNull(e2_s1_i2_dep1_symbols);
		assertNotNull(e2_s1_i2_dep2_symbols);
		assertNull(e2_s1_i2_dep3_symbols);
		assertNotNull(e2_s1_i2_dep4_symbols);
		assertEquals(2, e1_s1_i2_dep2_symbols.length);
		assertEquals(2, e1_s1_i2_dep4_symbols.length);
		assertEquals(2, e2_s1_i2_dep2_symbols.length);
		assertEquals(2, e2_s1_i2_dep4_symbols.length);
		assertNotNull(e1_s1_i2_dep2_symbols[0]);
		assertNotNull(e1_s1_i2_dep2_symbols[1]);
		assertNotNull(e1_s1_i2_dep4_symbols[0]);
		assertNotNull(e1_s1_i2_dep4_symbols[1]);
		assertNotNull(e2_s1_i2_dep2_symbols[0]);
		assertNotNull(e2_s1_i2_dep2_symbols[1]);
		assertNotNull(e2_s1_i2_dep4_symbols[0]);
		assertNotNull(e2_s1_i2_dep4_symbols[1]);

		assertNotNull(e1_s1_i2_dep2_symbols[0].toString());
		assertNotNull(e1_s1_i2_dep2_symbols[0].getLabelText());
		assertEquals(TestPlugin.DEP2_SYMBOL1_FILENAME, ((UiModelSymbol)e1_s1_i2_dep2_symbols[0]).getFileName());
		assertFalse(e1_s1_i2_dep2_symbols[0].hasChildren());
		assertEquals(e1_s1_i2_depimages[1], e1_s1_i2_dep2_symbols[0].getParent());
		assertNotNull(e1_s1_i2_dep2_symbols[0].getLabelImage());

		assertNotNull(e1_s1_i2_dep2_symbols[1].toString());
		assertNotNull(e1_s1_i2_dep2_symbols[1].getLabelText());
		assertEquals(TestPlugin.DEP2_SYMBOL2_FILENAME, ((UiModelSymbol)e1_s1_i2_dep2_symbols[1]).getFileName());
		assertFalse(e1_s1_i2_dep2_symbols[1].hasChildren());
		assertEquals(e1_s1_i2_depimages[1], e1_s1_i2_dep2_symbols[1].getParent());
		assertNotNull(e1_s1_i2_dep2_symbols[1].getLabelImage());

		assertNotNull(e1_s1_i2_dep4_symbols[0].toString());
		assertNotNull(e1_s1_i2_dep4_symbols[0].getLabelText());
		assertEquals(TestPlugin.DEP4_SYMBOL_FILENAME, ((UiModelSymbol)e1_s1_i2_dep4_symbols[0]).getFileName());
		assertFalse(e1_s1_i2_dep4_symbols[0].hasChildren());
		assertEquals(e1_s1_i2_depimages[3], e1_s1_i2_dep4_symbols[0].getParent());
		assertNotNull(e1_s1_i2_dep4_symbols[0].getLabelImage());

		assertNotNull(e1_s1_i2_dep4_symbols[1].toString());
		assertNotNull(e1_s1_i2_dep4_symbols[1].getLabelText());
		assertEquals(TestPlugin.DEP4_SYMBOL_FILENAME, ((UiModelSymbol)e1_s1_i2_dep4_symbols[1]).getFileName());
		assertFalse(e1_s1_i2_dep4_symbols[1].hasChildren());
		assertEquals(e1_s1_i2_depimages[3], e1_s1_i2_dep4_symbols[1].getParent());
		assertNotNull(e1_s1_i2_dep4_symbols[1].getLabelImage());

		assertNotNull(e2_s1_i2_dep2_symbols[0].toString());
		assertNotNull(e2_s1_i2_dep2_symbols[0].getLabelText());
		assertEquals(TestPlugin.DEP2_SYMBOL1_FILENAME, ((UiModelSymbol)e2_s1_i2_dep2_symbols[0]).getFileName());
		assertFalse(e2_s1_i2_dep2_symbols[0].hasChildren());
		assertEquals(e2_s1_i2_depimages[1], e2_s1_i2_dep2_symbols[0].getParent());
		assertNotNull(e2_s1_i2_dep2_symbols[0].getLabelImage());

		assertNotNull(e2_s1_i2_dep2_symbols[1].toString());
		assertNotNull(e2_s1_i2_dep2_symbols[1].getLabelText());
		assertEquals(TestPlugin.DEP2_SYMBOL2_FILENAME, ((UiModelSymbol)e2_s1_i2_dep2_symbols[1]).getFileName());
		assertFalse(e2_s1_i2_dep2_symbols[1].hasChildren());
		assertEquals(e2_s1_i2_depimages[1], e2_s1_i2_dep2_symbols[1].getParent());
		assertNotNull(e2_s1_i2_dep2_symbols[1].getLabelImage());

		assertNotNull(e2_s1_i2_dep4_symbols[0].toString());
		assertNotNull(e2_s1_i2_dep4_symbols[0].getLabelText());
		assertEquals(TestPlugin.DEP4_SYMBOL_FILENAME, ((UiModelSymbol)e2_s1_i2_dep4_symbols[0]).getFileName());
		assertFalse(e2_s1_i2_dep4_symbols[0].hasChildren());
		assertEquals(e2_s1_i2_depimages[3], e2_s1_i2_dep4_symbols[0].getParent());
		assertNotNull(e2_s1_i2_dep4_symbols[0].getLabelImage());

		assertNotNull(e2_s1_i2_dep4_symbols[1].toString());
		assertNotNull(e2_s1_i2_dep4_symbols[1].getLabelText());
		assertEquals(TestPlugin.DEP4_SYMBOL_FILENAME, ((UiModelSymbol)e2_s1_i2_dep4_symbols[1]).getFileName());
		assertFalse(e2_s1_i2_dep4_symbols[1].hasChildren());
		assertEquals(e2_s1_i2_depimages[3], e2_s1_i2_dep4_symbols[1].getParent());
		assertNotNull(e2_s1_i2_dep4_symbols[1].getLabelImage());

		/* test UiModelSample */
		IUiModelElement[] e1_s1_i1_s1_samples = e1_s1_i1_symbols[0].getChildren(),
							e1_s1_i1_s2_samples = e1_s1_i1_symbols[1].getChildren(),
							e1_s1_d2_s1_samples = e1_s1_i2_dep2_symbols[0].getChildren(),
							e1_s1_d2_s2_samples = e1_s1_i2_dep2_symbols[1].getChildren(),
							e1_s1_d4_s1_samples = e1_s1_i2_dep4_symbols[0].getChildren(),
							e1_s1_d4_s2_samples = e1_s1_i2_dep4_symbols[1].getChildren(),
							e2_s1_i1_s1_samples = e2_s1_i1_symbols[0].getChildren(),
							e2_s1_i1_s2_samples = e2_s1_i1_symbols[1].getChildren(),
							e2_s1_d2_s1_samples = e2_s1_i2_dep2_symbols[0].getChildren(),
							e2_s1_d2_s2_samples = e2_s1_i2_dep2_symbols[1].getChildren(),
							e2_s1_d4_s1_samples = e2_s1_i2_dep4_symbols[0].getChildren(),
							e2_s1_d4_s2_samples = e2_s1_i2_dep4_symbols[1].getChildren(),
							e2_s2_i1_s1_samples = e2_s2_i1_symbols[0].getChildren(),
							e2_s2_i1_s2_samples = e2_s2_i1_symbols[1].getChildren();

		assertNotNull(e1_s1_i1_s1_samples);
		assertNotNull(e1_s1_i1_s2_samples);
		assertNotNull(e1_s1_d2_s1_samples);
		assertNotNull(e1_s1_d2_s2_samples);
		assertNotNull(e1_s1_d4_s1_samples);
		assertNotNull(e1_s1_d4_s2_samples);
		assertNotNull(e2_s1_i1_s1_samples);
		assertNotNull(e2_s1_i1_s2_samples);
		assertNotNull(e2_s1_d2_s1_samples);
		assertNotNull(e2_s1_d2_s2_samples);
		assertNotNull(e2_s1_d4_s1_samples);
		assertNotNull(e2_s1_d4_s2_samples);
		assertNotNull(e2_s2_i1_s1_samples);
		assertNotNull(e2_s2_i1_s2_samples);
		assertEquals(4, e1_s1_i1_s1_samples.length);
		assertEquals(3, e1_s1_i1_s2_samples.length);
		//0 but not null for dep images due to samples with no line #
		assertEquals(0, e1_s1_d2_s1_samples.length);
		assertEquals(0, e1_s1_d2_s2_samples.length);
		assertEquals(0, e1_s1_d4_s1_samples.length);
		assertEquals(0, e1_s1_d4_s2_samples.length);
		assertEquals(4, e2_s1_i1_s1_samples.length);
		assertEquals(3, e2_s1_i1_s2_samples.length);
		assertEquals(0, e2_s1_d2_s1_samples.length);
		assertEquals(0, e2_s1_d2_s2_samples.length);
		assertEquals(0, e2_s1_d4_s1_samples.length);
		assertEquals(0, e2_s1_d4_s2_samples.length);
		assertEquals(4, e2_s2_i1_s1_samples.length);
		assertEquals(3, e2_s2_i1_s2_samples.length);
		assertNotNull(e1_s1_i1_s1_samples[0]);
		assertNotNull(e1_s1_i1_s1_samples[1]);
		assertNotNull(e1_s1_i1_s1_samples[2]);
		assertNotNull(e1_s1_i1_s1_samples[3]);
		assertNotNull(e1_s1_i1_s2_samples[0]);
		assertNotNull(e1_s1_i1_s2_samples[1]);
		assertNotNull(e1_s1_i1_s2_samples[2]);

		assertNotNull(e2_s1_i1_s1_samples[0]);
		assertNotNull(e2_s1_i1_s1_samples[1]);
		assertNotNull(e2_s1_i1_s1_samples[2]);
		assertNotNull(e2_s1_i1_s1_samples[3]);
		assertNotNull(e2_s1_i1_s2_samples[0]);
		assertNotNull(e2_s1_i1_s2_samples[1]);
		assertNotNull(e2_s1_i1_s2_samples[2]);
		assertNotNull(e2_s2_i1_s1_samples[0]);
		assertNotNull(e2_s2_i1_s1_samples[1]);
		assertNotNull(e2_s2_i1_s1_samples[2]);
		assertNotNull(e2_s2_i1_s1_samples[3]);
		assertNotNull(e2_s2_i1_s2_samples[0]);
		assertNotNull(e2_s2_i1_s2_samples[1]);
		assertNotNull(e2_s2_i1_s2_samples[2]);

		assertNotNull(e1_s1_i1_s1_samples[0].toString());
		assertNotNull(e1_s1_i1_s1_samples[0].getLabelText());
		assertEquals(42, ((UiModelSample)e1_s1_i1_s1_samples[0]).getLine());
		assertEquals(0.6341, ((UiModelSample)e1_s1_i1_s1_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s1_samples[0].getChildren());
		assertFalse(e1_s1_i1_s1_samples[0].hasChildren());
		assertEquals(e1_s1_i1_symbols[0], e1_s1_i1_s1_samples[0].getParent());
		assertNotNull(e1_s1_i1_s1_samples[0].getLabelImage());

		assertNotNull(e1_s1_i1_s1_samples[1].toString());
		assertNotNull(e1_s1_i1_s1_samples[1].getLabelText());
		assertEquals(36, ((UiModelSample)e1_s1_i1_s1_samples[1]).getLine());
		assertEquals(0.1951, ((UiModelSample)e1_s1_i1_s1_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s1_samples[1].getChildren());
		assertFalse(e1_s1_i1_s1_samples[1].hasChildren());
		assertEquals(e1_s1_i1_symbols[0], e1_s1_i1_s1_samples[1].getParent());
		assertNotNull(e1_s1_i1_s1_samples[1].getLabelImage());

		assertNotNull(e1_s1_i1_s1_samples[2].toString());
		assertNotNull(e1_s1_i1_s1_samples[2].getLabelText());
		assertEquals(31, ((UiModelSample)e1_s1_i1_s1_samples[2]).getLine());
		assertEquals(0.0488, ((UiModelSample)e1_s1_i1_s1_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s1_samples[2].getChildren());
		assertFalse(e1_s1_i1_s1_samples[2].hasChildren());
		assertEquals(e1_s1_i1_symbols[0], e1_s1_i1_s1_samples[2].getParent());
		assertNotNull(e1_s1_i1_s1_samples[2].getLabelImage());

		assertNotNull(e1_s1_i1_s1_samples[3].toString());
		assertNotNull(e1_s1_i1_s1_samples[3].getLabelText());
		assertEquals(39, ((UiModelSample)e1_s1_i1_s1_samples[3]).getLine());
		assertEquals(0.0, ((UiModelSample)e1_s1_i1_s1_samples[3]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s1_samples[3].getChildren());
		assertFalse(e1_s1_i1_s1_samples[3].hasChildren());
		assertEquals(e1_s1_i1_symbols[0], e1_s1_i1_s1_samples[3].getParent());
		assertNotNull(e1_s1_i1_s1_samples[3].getLabelImage());

		assertNotNull(e1_s1_i1_s2_samples[0].toString());
		assertNotNull(e1_s1_i1_s2_samples[0].getLabelText());
		assertEquals(94, ((UiModelSample)e1_s1_i1_s2_samples[0]).getLine());
		assertEquals(0.0976, ((UiModelSample)e1_s1_i1_s2_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s2_samples[0].getChildren());
		assertFalse(e1_s1_i1_s2_samples[0].hasChildren());
		assertEquals(e1_s1_i1_symbols[1], e1_s1_i1_s2_samples[0].getParent());
		assertNotNull(e1_s1_i1_s2_samples[0].getLabelImage());

		assertNotNull(e1_s1_i1_s2_samples[1].toString());
		assertNotNull(e1_s1_i1_s2_samples[1].getLabelText());
		assertEquals(12, ((UiModelSample)e1_s1_i1_s2_samples[1]).getLine());
		assertEquals(0.0, ((UiModelSample)e1_s1_i1_s2_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s2_samples[1].getChildren());
		assertFalse(e1_s1_i1_s2_samples[1].hasChildren());
		assertEquals(e1_s1_i1_symbols[1], e1_s1_i1_s2_samples[1].getParent());
		assertNotNull(e1_s1_i1_s2_samples[1].getLabelImage());

		assertNotNull(e1_s1_i1_s2_samples[2].toString());
		assertNotNull(e1_s1_i1_s2_samples[2].getLabelText());
		assertEquals(55, ((UiModelSample)e1_s1_i1_s2_samples[2]).getLine());
		assertEquals(0.0, ((UiModelSample)e1_s1_i1_s2_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e1_s1_i1_s2_samples[2].getChildren());
		assertFalse(e1_s1_i1_s2_samples[2].hasChildren());
		assertEquals(e1_s1_i1_symbols[1], e1_s1_i1_s2_samples[2].getParent());
		assertNotNull(e1_s1_i1_s2_samples[2].getLabelImage());

		assertNotNull(e2_s1_i1_s1_samples[0].toString());
		assertNotNull(e2_s1_i1_s1_samples[0].getLabelText());
		assertEquals(42, ((UiModelSample)e2_s1_i1_s1_samples[0]).getLine());
		assertEquals(0.6341, ((UiModelSample)e2_s1_i1_s1_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s1_samples[0].getChildren());
		assertFalse(e2_s1_i1_s1_samples[0].hasChildren());
		assertEquals(e2_s1_i1_symbols[0], e2_s1_i1_s1_samples[0].getParent());
		assertNotNull(e2_s1_i1_s1_samples[0].getLabelImage());

		assertNotNull(e2_s1_i1_s1_samples[1].toString());
		assertNotNull(e2_s1_i1_s1_samples[1].getLabelText());
		assertEquals(36, ((UiModelSample)e2_s1_i1_s1_samples[1]).getLine());
		assertEquals(0.1951, ((UiModelSample)e2_s1_i1_s1_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s1_samples[1].getChildren());
		assertFalse(e2_s1_i1_s1_samples[1].hasChildren());
		assertEquals(e2_s1_i1_symbols[0], e2_s1_i1_s1_samples[1].getParent());
		assertNotNull(e2_s1_i1_s1_samples[1].getLabelImage());

		assertNotNull(e2_s1_i1_s1_samples[2].toString());
		assertNotNull(e2_s1_i1_s1_samples[2].getLabelText());
		assertEquals(31, ((UiModelSample)e2_s1_i1_s1_samples[2]).getLine());
		assertEquals(0.0488, ((UiModelSample)e2_s1_i1_s1_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s1_samples[2].getChildren());
		assertFalse(e2_s1_i1_s1_samples[2].hasChildren());
		assertEquals(e2_s1_i1_symbols[0], e2_s1_i1_s1_samples[2].getParent());
		assertNotNull(e2_s1_i1_s1_samples[2].getLabelImage());

		assertNotNull(e2_s1_i1_s1_samples[3].toString());
		assertNotNull(e2_s1_i1_s1_samples[3].getLabelText());
		assertEquals(39, ((UiModelSample)e2_s1_i1_s1_samples[3]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s1_i1_s1_samples[3]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s1_samples[3].getChildren());
		assertFalse(e2_s1_i1_s1_samples[3].hasChildren());
		assertEquals(e2_s1_i1_symbols[0], e2_s1_i1_s1_samples[3].getParent());
		assertNotNull(e2_s1_i1_s1_samples[3].getLabelImage());

		assertNotNull(e2_s1_i1_s2_samples[0].toString());
		assertNotNull(e2_s1_i1_s2_samples[0].getLabelText());
		assertEquals(94, ((UiModelSample)e2_s1_i1_s2_samples[0]).getLine());
		assertEquals(0.0976, ((UiModelSample)e2_s1_i1_s2_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s2_samples[0].getChildren());
		assertFalse(e2_s1_i1_s2_samples[0].hasChildren());
		assertEquals(e2_s1_i1_symbols[1], e2_s1_i1_s2_samples[0].getParent());
		assertNotNull(e2_s1_i1_s2_samples[0].getLabelImage());

		assertNotNull(e2_s1_i1_s2_samples[1].toString());
		assertNotNull(e2_s1_i1_s2_samples[1].getLabelText());
		assertEquals(12, ((UiModelSample)e2_s1_i1_s2_samples[1]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s1_i1_s2_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s2_samples[1].getChildren());
		assertFalse(e2_s1_i1_s2_samples[1].hasChildren());
		assertEquals(e2_s1_i1_symbols[1], e2_s1_i1_s2_samples[1].getParent());
		assertNotNull(e2_s1_i1_s2_samples[1].getLabelImage());

		assertNotNull(e2_s1_i1_s2_samples[2].toString());
		assertNotNull(e2_s1_i1_s2_samples[2].getLabelText());
		assertEquals(55, ((UiModelSample)e2_s1_i1_s2_samples[2]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s1_i1_s2_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e2_s1_i1_s2_samples[2].getChildren());
		assertFalse(e2_s1_i1_s2_samples[2].hasChildren());
		assertEquals(e2_s1_i1_symbols[1], e2_s1_i1_s2_samples[2].getParent());
		assertNotNull(e2_s1_i1_s2_samples[2].getLabelImage());

		assertNotNull(e2_s2_i1_s1_samples[0].toString());
		assertNotNull(e2_s2_i1_s1_samples[0].getLabelText());
		assertEquals(42, ((UiModelSample)e2_s2_i1_s1_samples[0]).getLine());
		assertEquals(0.65, ((UiModelSample)e2_s2_i1_s1_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s1_samples[0].getChildren());
		assertFalse(e2_s2_i1_s1_samples[0].hasChildren());
		assertEquals(e2_s2_i1_symbols[0], e2_s2_i1_s1_samples[0].getParent());
		assertNotNull(e2_s2_i1_s1_samples[0].getLabelImage());

		assertNotNull(e2_s2_i1_s1_samples[1].toString());
		assertNotNull(e2_s2_i1_s1_samples[1].getLabelText());
		assertEquals(36, ((UiModelSample)e2_s2_i1_s1_samples[1]).getLine());
		assertEquals(0.2, ((UiModelSample)e2_s2_i1_s1_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s1_samples[1].getChildren());
		assertFalse(e2_s2_i1_s1_samples[1].hasChildren());
		assertEquals(e2_s2_i1_symbols[0], e2_s2_i1_s1_samples[1].getParent());
		assertNotNull(e2_s2_i1_s1_samples[1].getLabelImage());

		assertNotNull(e2_s2_i1_s1_samples[2].toString());
		assertNotNull(e2_s2_i1_s1_samples[2].getLabelText());
		assertEquals(31, ((UiModelSample)e2_s2_i1_s1_samples[2]).getLine());
		assertEquals(0.05, ((UiModelSample)e2_s2_i1_s1_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s1_samples[2].getChildren());
		assertFalse(e2_s2_i1_s1_samples[2].hasChildren());
		assertEquals(e2_s2_i1_symbols[0], e2_s2_i1_s1_samples[2].getParent());
		assertNotNull(e2_s2_i1_s1_samples[2].getLabelImage());

		assertNotNull(e2_s2_i1_s1_samples[3].toString());
		assertNotNull(e2_s2_i1_s1_samples[3].getLabelText());
		assertEquals(39, ((UiModelSample)e2_s2_i1_s1_samples[3]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s2_i1_s1_samples[3]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s1_samples[3].getChildren());
		assertFalse(e2_s2_i1_s1_samples[3].hasChildren());
		assertEquals(e2_s2_i1_symbols[0], e2_s2_i1_s1_samples[3].getParent());
		assertNotNull(e2_s2_i1_s1_samples[3].getLabelImage());

		assertNotNull(e2_s2_i1_s2_samples[0].toString());
		assertNotNull(e2_s2_i1_s2_samples[0].getLabelText());
		assertEquals(94, ((UiModelSample)e2_s2_i1_s2_samples[0]).getLine());
		assertEquals(0.1, ((UiModelSample)e2_s2_i1_s2_samples[0]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s2_samples[0].getChildren());
		assertFalse(e2_s2_i1_s2_samples[0].hasChildren());
		assertEquals(e2_s2_i1_symbols[1], e2_s2_i1_s2_samples[0].getParent());
		assertNotNull(e2_s2_i1_s2_samples[0].getLabelImage());

		assertNotNull(e2_s2_i1_s2_samples[1].toString());
		assertNotNull(e2_s2_i1_s2_samples[1].getLabelText());
		assertEquals(12, ((UiModelSample)e2_s2_i1_s2_samples[1]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s2_i1_s2_samples[1]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s2_samples[1].getChildren());
		assertFalse(e2_s2_i1_s2_samples[1].hasChildren());
		assertEquals(e2_s2_i1_symbols[1], e2_s2_i1_s2_samples[1].getParent());
		assertNotNull(e2_s2_i1_s2_samples[1].getLabelImage());

		assertNotNull(e2_s2_i1_s2_samples[2].toString());
		assertNotNull(e2_s2_i1_s2_samples[2].getLabelText());
		assertEquals(55, ((UiModelSample)e2_s2_i1_s2_samples[2]).getLine());
		assertEquals(0.0, ((UiModelSample)e2_s2_i1_s2_samples[2]).getCountPercentage(), 0.0001);
		assertNull(e2_s2_i1_s2_samples[2].getChildren());
		assertFalse(e2_s2_i1_s2_samples[2].hasChildren());
		assertEquals(e2_s2_i1_symbols[1], e2_s2_i1_s2_samples[2].getParent());
		assertNotNull(e2_s2_i1_s2_samples[2].getLabelImage());
 	}
}
