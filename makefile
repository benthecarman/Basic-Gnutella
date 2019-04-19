JC = javac
JFLAGS = -g

default: Gnutella.class Ping.class Peer.class Query.class

Ping.class: Ping.java
	$(JC) $(JFLAGS) Ping.java

Peer.class: Peer.java
	$(JC) $(JFLAGS) Peer.java

Gnutella.class: Gnutella.java
	$(JC) $(JFLAGS) Gnutella.java

Query.class: Query.java
	$(JC) $(JFLAGS) Query.java

clean:
	$(RM) *.class