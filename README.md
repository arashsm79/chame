# Chame! Chat and Game software written in Java using NIO

Chame! Chat and Game software written in Java using NIO


An online chat and game Java application using Java's Non-blocking New I/O.

The networking is done using Java's NIO package. Everything is handled asynchronously; Since we're using channels, selectors, messages are sent/received partially therefore we need a logic that queues the bytes read and when ever a full message is received dispatches a worker for proccessing received data. The messages that are sent/received from the clients are null terminated in order to be notified when a full message is received.

For the database I chose sqlite(even though it's not really suitable for chat servers). I tried to come up with a decent and thought through schema (through research) the schema is shown below.

 ![Alt text](/screenshots/db-schema.png?raw=true "Database Schema")


Tic Tac Toe can be played againts AI(minimax) and played with others.

Snakes and ladders is only offline and under development.

- Each room has it's own game lobbies.

- Contacts System (friend request/accept)

- Chats and group chats




Dependencies:

- HikariCP

- Gson

- xerial / sqlite-jdbc 



Screenshots:
 ![Alt text](/screenshots/1.png?raw=true "Chat")
 ![Alt text](/screenshots/2.png?raw=true "Contacts")
 ![Alt text](/screenshots/3.png?raw=true "Games")
 ![Alt text](/screenshots/5.png?raw=true "Tic Tac Toe")
 ![Alt text](/screenshots/1.png?raw=true "Chat Game Lobby")
 
 
 
 
 

