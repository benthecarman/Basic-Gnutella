# Basic Gnutella

## User Guide

To build you can use the `make` command.

To run you can use `java Gnutella`.

Using the program with no arguments will start a node with no peers and will wait for a connection.

There are 4 options you can specify when starting the program:

    --port <port>                           This option will specify the port the program is listening on.
    --connect <ip:port>                     This option will specify to attempt to connect to a node at the given ip and port, if you do not specify the port the default (6345) will be used.
    --dir <dir>                             This option will specify what folder the program will be using to save and service queries, the default is `~/.gnutella-dir/`
    --query <search-string> <time-to-live>  This option will specify a query to create and be sent at startup.  This option can be specified multiple times.

---

## Protocol

All pings, pongs, and queries are sent using UDP.

All queried files are sent using TCP/IP.

A ping or a pong message will be the same message.

A ping should be sent every minute, if 5 cycles are missed then a peer should be dropped.

The message's format is as follows: (All in Big Endian)

- bytes 0-1: port number
- bytes 2-5: IP address
- bytes 6-9: number of files
- bytes 10-13: total bytes of files

A query message's format is as follows: (All in Big Endian)

- bytes 0-1: port number
- bytes 2-5: IP address
- bytes 6-9: query id number
- bytes 10-17: time to live (milliseconds)
- bytes 18-25: created timestamp (milliseconds)
- bytes 26-29: search query size
- bytes 30+: search query

The search query should just be a file name.

When a query is being serviced the 2 nodes will open a socket connect between eachother on the query's given port, then the
node with the file will first send, in 4 bytes, the size of the file.  Then the file will be sent.

---

## Modules / Pseudocode

The program has 3 main threads, plus an extra thread for every query specified at startup

### PingListener Thread

The PingListener thread handles incoming pings and queries.  It will forward the messages to peers and will attempt to serve the queries.

#### PingListener Pseudocode

    Setup server socket

    while (true) {

        Recieve data

        if data is a ping {

            If ping is from a peer, update the peer's latest message timestamp

            Otherwise, add peer to our list of peers, if there is room

            Then forward ping to rest of peers
        }

        else (data is a query) {

            If we have serviced this query or it's time run out, exit

            Add query id to list of serviced queries

            If we can service the query, open connection and send the file

            Otherwise, forward query to peers
        }

    }

### PingSender Thread

The PingSender thread periodically will send a ping to our peers, it also will drop peers if no message has been recieved from them in 5 ping cycles.

#### PingSender Pseudocode

    Setup client socket

    while (true) {

        foreach Peers as peer {

            If peer has missed 5 cycles, queue them to be dropped

            Otherwise, send a ping to the peer.

        }

        Remove queued peers

        Sleep 1 minute

    }

### FileSystem Thread

The FileSystem thread keeps track of the folder the node is watching, every minute it will search the folder and track new files.

#### FileSystem Pseudocode

    If directory not available, create it.

    while (true) {

        Get all files in directory, add them to file list
        Update saved ping to have correct number of files and correct size of files

        Sleep 1 minute

    }

### QuerySender Thread

The QuerySender thread will send out a query at startup and wait to recieve the requested file.

#### QuerySender Pseudocode

    Add query id to serviced query list

    Send query to peers

    Open server socket to accept connections

    Once connection established, recieve 4 bytes to determine file size

    Recieve the file in bytes.

    Write file to disk

    close the socket
