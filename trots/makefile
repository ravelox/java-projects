.SUFFIXES:
.SUFFIXES: .class .java

JAVA_HOME	=	/usr
JAVA			=	$(JAVA_HOME)/bin/java
JAVAC			=	$(JAVA_HOME)/bin/javac
JAR			=	$(JAVA_HOME)/bin/jar
ANT			= /usr/java/apache-ant-1.6.0/bin/ant

JAR_FILE		=	trots.jar

CLASSES		=	countdownLabel\
					trotsReceiver\
					trotsServer\
					trotsSender\
					trotsControl\
					iniFileGen\
					dataConnection\
					AllStats\
					trotsMessage\
					Constants\
					ControlGUI\
					NoFocusButton\
					YesNoDialog\
					OKDialog\
					DetailDialog\
					BSumTime\
					debug

COMPILECLASSPATH	= .
RUNTIMECLASSPATH	= $(JAR_FILE):.

SRC=$(CLASSES:=.java)
OBJ=$(SRC:.java=.class)

$(JAR_FILE): $(OBJ)
	$(JAR) -0cvf $(JAR_FILE) *.class

.java.class:
	$(JAVAC) -deprecation -classpath $(COMPILECLASSPATH) $<

clean:
	rm -f $(OBJ) *.class $(JAR_FILE)
