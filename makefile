JC = javac
JFLAGS = -g

default: Gnutella.class Pong.class Peer.class

Pong.class: Pong.java
	$(JC) $(JFLAGS) Pong.java

Peer.class: Peer.java
	$(JC) $(JFLAGS) Peer.java

Gnutella.class: Gnutella.java
	$(JC) $(JFLAGS) Gnutella.java
