JC = javac
JFLAGS = -g

default: Gnutella.class Ping.class Peer.class

Ping.class: Ping.java
	$(JC) $(JFLAGS) Ping.java

Peer.class: Peer.java
	$(JC) $(JFLAGS) Peer.java

Gnutella.class: Gnutella.java
	$(JC) $(JFLAGS) Gnutella.java

clean:
	$(RM) *.class