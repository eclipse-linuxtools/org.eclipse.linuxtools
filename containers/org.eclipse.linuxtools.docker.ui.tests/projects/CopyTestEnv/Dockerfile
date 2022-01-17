FROM bash:latest

# Default is still the alpine ash
SHELL ["/usr/local/bin/bash", "-c"]

RUN mkdir -p \ 
		/test/a/b/c \
		/test/a/b/d \
	&& echo "Hello 1" > test/a/b/c/h1.txt \
	&& echo "Hello 2" > test/a/b/d/h2.txt
	
RUN mkdir -p \
		/test/sl/ \
	&& ln -s  /test/a/b/c /test/sl/link-c-abs \
	&& ln -s ../a/b/d/ /test/sl/link-d-rel
	
RUN mkdir -p \
		/test/sl-rec/ \
		/test/sl-rec/subdir \
	&& ln -s .. /test/sl-rec/subdir/link-up

# Test should terminate earlier, but more is better
RUN mkdir -p /test/sl-depth/l30 \
	&& echo "Test" > /test/sl-depth/l30/F30 \
	&& for I in {0..29} ; do \
			mkdir -p /test/sl-depth/l$I ; \
			echo "Test" > /test/sl-depth/l$I/F$I; \
			ln -s "../l$(($I+1))"  /test/sl-depth/l$I/L$I; \
	   done;
		
	
RUN mkdir -p \
		/test/manyfiles \
	&&  for I in {1..10000} ; do \
			echo "File" > /test/manyfiles/F$I ;\
	    done
	    
	    
RUN mkdir -p /test/sl-back/l0 /test/sl-back/l30 \
	&& echo "Test" > /test/sl-back/l30/F30 \
	&& echo "Test" > /test/sl-back/l0/F0 \
	&& ln -s "../l1"  /test/sl-back/l0/L0 \
	&& ln -s "../l29"  /test/sl-back/l30/LB30 \
	&& for I in {1..29} ; do \
			mkdir -p /test/sl-back/l$I ; \
			echo "Test" > /test/sl-back/l$I/F$I; \
			ln -s "../l$(($I+1))"  /test/sl-back/l$I/L$I; \
			ln -s "../l$(($I-1))"  /test/sl-back/l$I/LB$I; \
	   done
	
 
