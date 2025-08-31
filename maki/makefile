.SUFFIXES:
.SUFFIXES: .class .java

JAVAC           =       javac -O -classpath .:/web/docs/score_client.jar
JAVA            =       java

CLASSES         =       Maki

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

JAR=maki.jar

all: $(OBJ) $(JAR)

$(JAR)::
	@echo Building ${JAR} file
	@jar -0cvf ${JAR} *.class

.java.class:
	@echo Compiling $<
	@${JAVAC}       $<

clean:
	@rm -f $(OBJ) *.class *.jar
	@echo Done
