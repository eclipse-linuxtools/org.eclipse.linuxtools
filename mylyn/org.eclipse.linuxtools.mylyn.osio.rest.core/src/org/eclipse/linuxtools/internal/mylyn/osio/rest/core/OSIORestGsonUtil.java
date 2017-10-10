/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class OSIORestGsonUtil {

	private class RemoveAddStringHelper {
		Set<String> add;

		Set<String> remove;

		public RemoveAddStringHelper(Set<String> removeSet, Set<String> addSet) {
			add = new HashSet<String>(addSet);
			remove = new HashSet<String>(removeSet);
			if (remove.contains("")) { //$NON-NLS-1$
				remove.remove(""); //$NON-NLS-1$
			}
			if (add.contains("")) { //$NON-NLS-1$
				add.remove(""); //$NON-NLS-1$
			}
			Set<String> intersection = Sets.intersection(addSet, removeSet);
			remove.removeAll(intersection);
			add.removeAll(intersection);
			if (remove.isEmpty()) {
				remove = null;
			}
			if (add.isEmpty()) {
				add = null;
			}
		}
	}

	private class RemoveAddIntegerHelper {
		Set<Integer> add;

		Set<Integer> remove;

		public RemoveAddIntegerHelper(Set<Integer> removeSet, Set<Integer> addSet) {
			add = new HashSet<Integer>(addSet);
			remove = new HashSet<Integer>(removeSet);
			if (remove.contains(null)) {
				remove.remove(null);
			}
			if (add.contains(null)) {
				add.remove(null);
			}
			Set<Integer> intersection = Sets.intersection(addSet, removeSet);
			remove.removeAll(intersection);
			add.removeAll(intersection);
			if (remove.isEmpty()) {
				remove = null;
			}
			if (add.isEmpty()) {
				add = null;
			}
		}
	}

	private static Gson gson = new Gson();

	public static String convertString2GSonString(String str) {
		str = str.replace("\"", "\\\"").replace("\n", "\\\n"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
		StringBuffer ostr = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch >= 0x0020) && (ch <= 0x007e)) {
				ostr.append(ch);
			} else {
				ostr.append("\\u"); //$NON-NLS-1$
				String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);
				for (int j = 0; j < 4 - hex.length(); j++) {
					ostr.append("0"); //$NON-NLS-1$
				}
				ostr.append(hex.toLowerCase());
			}
		}
		return (new String(ostr));
	}

	private static OSIORestGsonUtil instance;

	public static OSIORestGsonUtil getDefault() {
		if (instance == null) {
			instance = new OSIORestGsonUtil();
		}
		return instance;
	}

	public static void buildArrayFromHash(JsonWriter out, String id, Set<String> setNew, boolean asInteger)
			throws IOException {
		if (!setNew.isEmpty()) {
			out.name(id).beginArray();
			for (String string : setNew) {
				if (!"".equals(string)) {
					if (asInteger) {
						out.value(Integer.parseInt(string));
					} else {
						out.value(string);
					}
				}
			}
			out.endArray();
		}
	}

	public void buildAddRemoveHash(JsonWriter out, String id, Set<String> setOld, Set<String> setNew)
			throws IOException {
		RemoveAddStringHelper test = new RemoveAddStringHelper(setOld, setNew);
		out.name(id);
		gson.toJson(test, RemoveAddStringHelper.class, out);
	}

	private final Function<String, Integer> convert2Integer = new Function<String, Integer>() {

		@Override
		public Integer apply(String input) {
			if ("".equals(input)) {
				return null;
			}
			return Integer.parseInt(input);
		}
	};

	public void buildAddRemoveIntegerHash(JsonWriter out, String id, Set<String> setOld, Set<String> setNew)
			throws IOException {
		RemoveAddIntegerHelper test = new RemoveAddIntegerHelper(
				newHashSet(Iterables.transform(setOld, convert2Integer)),
				newHashSet(Iterables.transform(setNew, convert2Integer)));
		out.name(id);
		gson.toJson(test, RemoveAddIntegerHelper.class, out);
	}

	// TODO: check if this is needed
//	public static void buildFlags(JsonWriter out, Set<TaskAttribute> oldAttributes, TaskAttribute rootTaskData)
//			throws IOException {
//		boolean flagFound = false;
//		for (TaskAttribute element : oldAttributes) {
//			TaskAttribute taskAttribute = rootTaskData.getAttribute(element.getId());
//			String id = taskAttribute.getId();
//			if (id.startsWith(IOSIORestConstants.KIND_FLAG)
//					|| id.startsWith(IOSIORestConstants.KIND_FLAG_TYPE)) {
//				if (!flagFound) {
//					out.name("flags").beginArray(); //$NON-NLS-1$
//				}
//				OSIORestFlagMapper flagMapper = OSIORestFlagMapper.createFrom(taskAttribute);
//				flagMapper.applyTo(out);
//				flagFound = true;
//			}
//		}
//		if (flagFound) {
//			out.endArray();
//		}
//	}

}