.SUFFIXES:
.SUFFIXES: .class .java

JAVAC           =       javac -O -classpath .:..
JAVA            =       java

# All game and score server classes
CLASSES         =       Maki \
                        ScoreServer \
                        ScoreClient \
                        DataConnection

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

JAR=maki.jar

all: $(OBJ) $(JAR)

$(JAR):: $(OBJ)
        @echo Building ${JAR} file
        @jar -0cvf ${JAR} $(OBJ)

.java.class:
	@echo Compiling $<
	@${JAVAC}       $<

clean:
	@rm -f $(OBJ) *.class *.jar
	@echo Done
