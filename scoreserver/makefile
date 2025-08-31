.SUFFIXES:
.SUFFIXES: .class .java

JAVA_HOME		=	/usr/java/j2sdk1.4.2_04
JAVABIN			=	$(JAVA_HOME)/bin
JAVAC			=	$(JAVABIN)/javac -O  -classpath /opt/netscape/java/classes/java40.jar:.
JAVA			=	$(JAVABIN)/java

CLASSES		=	ScoreServer \
					ScoreClient \
					dataConnection

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

all: $(OBJ) $(JAR)

jar::
	@echo Building jar file for Score Client
	@jar -0cvf score_client.jar ScoreC*.class dataConnection*.class

.java.class:
	@echo Compiling $<
	@${JAVAC}	$<

clean:
	@rm -f $(OBJ) *.class *.jar
	@echo Done
