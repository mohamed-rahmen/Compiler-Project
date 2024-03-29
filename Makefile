###############################################################################
#
# Makefile for Java project
#
###############################################################################

# This is a sample makefile.
# The purpose of makefiles is to make sure that after running "make" your project is read for execution.
# Usually, Java projects need to compile all java source files and give execution permissions for your
# run file executable to run.
# Obviously, your project may be more complicated and require a different makefile.
# For this file (Makefile-java) to run when you call "make", rename it to "Makefile".

# The following line is a declaration of a variable named JAVAC:
JAVAC=javac
# As you can see, the variable only holds the name of the java compiler.

# The JAVACFLAGS variable should include any special flags your program needs for compilation:
JAVACFLAGS=-encoding ISO-8859-1

# The SRCS variable should include the filenames of all .java source files relevant for your project:
SRCS=*.java

# The EXEC variable should include the name of the run file executable only.
# In the case of Project 7, it is "VMtranslator":
EXEC=JackCompiler

TAR=tar
TARFLAGS=cvf
TARNAME=projXXX.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

# The following line is a rule declaration. A makefile rule is a list of prerequisites (other rules that
# need to be run before this rule) and commands that are run one after the other. The "all" rule is what
# runs when you call "make":
all: compile

# As you can see, the "all" rule requires the "compile" rule to be run. Meaning, that when you call the
# "all" rule, the "compile" rule is called too.
# A general rule looks like this:
# rule_name: prerequisite1 prerequisite2 prerequisite3 prerequisite4 ...
#	command1
#	command2
#	command3
#	...
# Where each preqrequisite is a rule name, and each command is a command-line command (for example chmod,
# javac, echo, etc').

# The "compile" rule performs a compilation of all java files specified in the SRCS variable
# and then gives execution permissions to the run file called VMtranslator
compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)

# The "compile" rule simply runs two commands one after the other:
# 1. "$(JAVAC) $(JAVACFLAGS) $(SRCS)" - this command is constructed from 3 variables, and if you use
# the defaults specified here it simply translated to "javac *.java", meaning that it compiles all
# java files in the directory.
# 2. "chmod +x $(EXEC)" - this command is constructed from "chmod +x", which is the terminal command
# that grants execution permissions, and "$(EXEC)", which should contain the name of your run file.
# This command simply gives execution permissions for your run file, so the graders could run it on their
# computers.

# This is what runs when you call "make tar" and will put all the files specified
# in the TARSRCS variable in a tar. This is for your convenience only and you
# don't have to support it.
tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)

# This is what runs when you call "make clean" and will remove all compiled class files.
# This is for your convenience only and you don't have to support it.
clean:
	rm -f *.class *~
