JAVAC =         /src/bin/javac
SRCDIR  =src
BINDIR  =bin
DOCDIR  =doc
.SUFFIXES:      .java .class
all:
	javac -d bin $(SRCDIR)/*.java
run:
	java -cp bin MedianFilterSerial Assign_Test_images/6016.jpg ij.jpg 15
clean:
	rm $(BINDIR)/*.class
doc:
	javadoc -cp bin/ -d doc/ src/*.java
