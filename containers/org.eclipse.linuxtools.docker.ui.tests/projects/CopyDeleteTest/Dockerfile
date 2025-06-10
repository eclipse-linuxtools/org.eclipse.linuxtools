 # Copyright (c) 2022 Mathema.
 #
 # This program and the accompanying materials are made
 # available under the terms of the Eclipse Public License 2.0
 # which is available at https://www.eclipse.org/legal/epl-2.0/
 #
 # SPDX-License-Identifier: EPL-2.0



FROM mirror.gcr.io/library/bash:latest

# Default is still the alpine ash
SHELL ["/usr/local/bin/bash", "-c"]

RUN mkdir -p \ 
		/test/deletetest \
	&& echo "Hello 1" > test/deletetest/hello1.txt \
	&& echo "Hello 2" > test/deletetest/hello2.txt
