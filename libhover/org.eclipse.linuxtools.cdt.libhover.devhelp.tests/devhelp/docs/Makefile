DOC_MODULE=test1
FLAGS='-I/usr/include/glib-2.0 -I/usr/lib64/glib-2.0/include'
LFLAGS="-L/usr/lib64 -lglib-2.0 -lgobject-2.0"
all:
	export CFLAGS=$(FLAGS)
	echo $(CFLAGS)
	CFLAGS=$(FLAGS) gtkdoc-scan --module=$(DOC_MODULE) --source-dir=../src
	CFLAGS=$(FLAGS) LDFLAGS=$(LFLAGS) gtkdoc-scangobj --module=$(DOC_MODULE)
	CFLAGS=$(FLAGS) gtkdoc-mkdb --module=$(DOC_MODULE) --output-format=xml
	mkdir -p html
	cd html && gtkdoc-mkhtml $(DOC_MODULE) ../test1-docs.xml
	gtkdoc-fixxref --module=$(DOC_MODULE) --module-dir=html
