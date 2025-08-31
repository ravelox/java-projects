.SUFFIXES:
.SUFFIXES: .class .java

JAVA_HOME	=	/usr/java/j2sdk1.4.2_04
JAVAC		=	${JAVA_HOME}/bin/javac -O -classpath .:/web/docs/score_client.jar
JAVA		=	${JAVA_HOME}/bin/java

CLASSES		=	maki

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

JAR=maki.jar

all: $(OBJ) $(JAR)

$(JAR)::
	@echo Building ${JAR} file
	@jar -0cvf ${JAR} *.class

.java.class:
	@echo Compiling $<
	@${JAVAC}	$<

clean:
	@rm -f $(OBJ) *.class *.jar
	@echo Done
