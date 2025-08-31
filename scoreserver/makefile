.SUFFIXES:
.SUFFIXES: .class .java

JAVAC           =       javac -O -classpath .
JAVA            =       java

CLASSES         =       ScoreServer \
                        ScoreClient \
                        DataConnection

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

all: $(OBJ) jar

jar::
        @echo Building jar file for Score Client
        @jar -0cvf score_client.jar ScoreC*.class DataConnection*.class

.java.class:
        @echo Compiling $<
        @${JAVAC}       $<

clean:
        @rm -f $(OBJ) *.class *.jar
        @echo Done
