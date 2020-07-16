package main;

import com.google.gson.Gson;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.GameHandler;
import main.games.tictactoe.TicTacToe;
import main.skeletons.*;

import java.sql.*;
import java.util.*;

public class ResponseHandler implements Runnable {
    private ClientSession clientSession;
    private String message;
    private Gson gson = new Gson();

    public ResponseHandler(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public void run() {
        message = dequeueClientSessionMessage();
        if(message == null)
            return;
        ChameMessage chameMessage = gson.fromJson(message, ChameMessage.class);

        switch (chameMessage.getHeader()){
            case ChameProtocol.REGISTER:{
                registerUser(chameMessage);
                break;
            }
            case ChameProtocol.LOGIN:{
                loginUser(chameMessage);
                break;
            }
            case ChameProtocol.FORGOT_PASSWORD:{
                recoverPassword(chameMessage);
                break;
            }
            case ChameProtocol.FRIEND_REQUEST:{
                sendFriendRequest(chameMessage);
                break;
            }
            case ChameProtocol.FRIEND_REQUEST_DENY:{
                deleteRelation(chameMessage);
                break;
            }
            case ChameProtocol.GET_CONTACTS:{
                sendContactList();
                break;
            }
            case ChameProtocol.CREATE_ROOM:{
                createRoomIfNotExists(chameMessage);
                break;
            }
            case ChameProtocol.JOIN_ROOM:{
                joinRoomIfExists(chameMessage);
                break;
            }
            case ChameProtocol.GET_ROOMS:{
                sendRoomsList();
                break;
            }
            case ChameProtocol.GET_ROOM_MESSAGES:{
                sendRoomMessages(chameMessage);
                break;
            }
            case ChameProtocol.CHAT_MESSAGE:{
                insertMessage(chameMessage);
                break;
            }
            case ChameProtocol.DELETE_MESSAGE:{
                deleteMessage(chameMessage);
                break;
            }
            case ChameProtocol.CREATE_GAME:{
                createGameRoom(chameMessage);
                break;
            }
            case ChameProtocol.GET_ROOM_GAME_LOBBIES:{
                sendRoomGameLobbies(chameMessage);
                break;
            }
            case ChameProtocol.JOIN_GAME:{
                joinGameRoom(chameMessage);;
                break;
            }
            case ChameProtocol.GAME_ACTION:{
                handleGameAction(chameMessage);;
                break;
            }
        }

        //return the worker to the pool
    }




    ///////////////////////////////////////////////////////////////
    ////////////////////////// GAME ///////////////////////////////
    ///////////////////////////////////////////////////////////////

    private void handleGameAction(ChameMessage chameMessage) {
        ChameGameAction gameAction = gson.fromJson(chameMessage.getBody(), ChameGameAction.class);
        if(gameAction == null)
            return;
        if(gameAction.getGameName().equalsIgnoreCase(clientSession.getCurrentGame().getGameName())){
            clientSession.getCurrentGame().handleResponse(gameAction, clientSession);
        }
    }

    private void createGameRoom(ChameMessage chameMessage){
        GameRoomSkeleton grs = gson.fromJson(chameMessage.getBody(), GameRoomSkeleton.class);
        grs.setCapacity(0);
        grs.setPlayersInside(1);
        switch (grs.getGameName()){
            case "tictactoe":{
                TicTacToe ticTacToe = new TicTacToe(grs);
                ticTacToe.joinUser(clientSession);
                grs.setChameGame(ticTacToe);
                break;
            }
        }
        GameHandler.getInstance().addGame(grs);
    }

    private void joinGameRoom(ChameMessage chameMessage) {
        GameRoomSkeleton gameRoom = gson.fromJson(chameMessage.getBody(), GameRoomSkeleton.class);
        if(gameRoom == null)
            return;

        GameRoomSkeleton foundRoom = GameHandler.getInstance().gameList.search(1, (k, v) ->{
           if(k == gameRoom.getRoomID()){
               synchronized (v){
                   Iterator<GameRoomSkeleton> it = v.iterator();
                   while(it.hasNext()){
                       GameRoomSkeleton grs = it.next();
                       if(gameRoom.getGameRoomID() == grs.getGameRoomID()){
                           return grs;
                       }
                   }
               }
           }
           return null;
        });

        if(foundRoom == null)
            return;

        foundRoom.getChameGame().joinUser(clientSession);
    }


    ///////////////////////////////////////////////////////////////
    ////////////////////////// CHAT ///////////////////////////////
    ///////////////////////////////////////////////////////////////

    private void createRoomIfNotExists(ChameMessage chameMessage){

        RoomSkeleton room = gson.fromJson(chameMessage.getBody(), RoomSkeleton.class);
        String sqlQuery = "INSERT INTO rooms (name, create_date, is_private, owner_id, private_room_key) " +
                " VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)){

            if(room.isPrivate())
                pst.setString(1, room.getName() + "," + clientSession.getUsername());
            else
                pst.setString(1, room.getName());

            pst.setLong(2, System.currentTimeMillis());
            pst.setBoolean(3, room.isPrivate());
            pst.setInt(4, clientSession.getId());

            if(room.isPrivate()){
                String privateRoomKey = null;
                //as a key sort the usernames alphabetically and store them as username1,username2
                if(clientSession.getUsername().compareToIgnoreCase(room.getName()) >= 0)
                    privateRoomKey = (clientSession.getUsername() + "," + room.getName()).toLowerCase();
                else
                    privateRoomKey = (room.getName() + "," + clientSession.getUsername()).toLowerCase();
                pst.setString(5, privateRoomKey);
            }else
                pst.setString(5, null);

            if(pst.executeUpdate() > 0){

                try(ResultSet rs = pst.getGeneratedKeys()){
                    if(rs.next()){
                        room.setId(rs.getInt(1));
                    }
                    createParticipants(room, cn);

                }catch (SQLException e){
                    e.printStackTrace();
                }
            }


        } catch(SQLIntegrityConstraintViolationException e){
            e.printStackTrace();
            sendError("It seems like this room already exists");
            System.out.println("Error creating room because it already exists");

        }catch (SQLException throwables) {
            System.out.println("Error creating room");
            throwables.printStackTrace();
        }

        sendRoomsList();
    }

    private void createParticipants(RoomSkeleton room, Connection cn) {
        String sqlQuery = "INSERT INTO participants (user_id, room_id, create_date) " +
                "SELECT users.id, ?, ? FROM users WHERE users.username = ?";

        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            int count = 0;
            for(UserSkeleton user : room.getMemberList()){
                pst.setInt(1, room.getId());
                pst.setLong(2, System.currentTimeMillis());
                pst.setString(3, user.getUsername());
                pst.addBatch();
                count += 1;
                // execute every 50 rows or less
                if (count % 50 == 0 || count == room.getMemberList().size()) {
                    pst.executeBatch();
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void joinRoomIfExists(ChameMessage chameMessage){

        int roomID = -1;
        try {
            roomID = Integer.parseInt(chameMessage.getBody());
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String sqlQuery = "INSERT INTO participants (user_id, room_id, create_date) " +
                "SELECT ?, rooms.id, ? FROM rooms WHERE rooms.id = ? AND rooms.is_private=0 " +
                "AND NOT EXISTS (SELECT * FROM participants WHERE participants.user_id = ? AND participants.room_id = ?)";

        try(    Connection cn = DatabaseConnectionPool.getConnection();
                PreparedStatement pst = cn.prepareStatement(sqlQuery)){

                pst.setInt(1, clientSession.getId());
                pst.setLong(2, System.currentTimeMillis());
                pst.setInt(3, roomID);
                pst.setInt(4, clientSession.getId());
                pst.setInt(5, roomID);

                int rowsAffected = pst.executeUpdate();

                if(rowsAffected <= 0)
                    sendError("This room does not exist or you're already a member or it's private");
                else{
                    insertMessageFootPrintForNewlyJoinedRecipient(cn, roomID);
                    sendRoomsList();
                }


        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void insertMessageFootPrintForNewlyJoinedRecipient(Connection cn, int roomID) {
        String sqlQuery = "INSERT INTO message_footprints (recipient_id, room_id, message_id, is_read) " +
                "SELECT ?, ?, messages.id, ? FROM messages WHERE messages.room_id = ?";

        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setInt(1, clientSession.getId());
            pst.setInt(2, roomID);
            pst.setBoolean(3, false);
            pst.setInt(4, roomID);
            pst.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void sendRoomsList() {
        String userExistsQuery = "SELECT rooms.id, rooms.name, rooms.create_date, rooms.is_private, users.username AS owner_username, " +
                "(SELECT COUNT(message_footprints.is_read) FROM message_footprints WHERE message_footprints.recipient_id = ? AND message_footprints.room_id = rooms.id AND message_footprints.is_read = 0) AS unread_messages " +
                "FROM rooms " +
                "INNER JOIN participants ON rooms.id = participants.room_id AND participants.user_id = ? " +
                "INNER JOIN users ON  rooms.owner_id = users.id";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(userExistsQuery)){
            pst.setInt(1, clientSession.getId());
            pst.setInt(2, clientSession.getId());
            ArrayList<RoomSkeleton> roomList = new ArrayList<>();
            try(ResultSet rs = pst.executeQuery()){

                while(rs.next()){
                    RoomSkeleton roomSkeleton = new RoomSkeleton();
                    roomSkeleton.setId(rs.getInt("id"));
                    roomSkeleton.setName(rs.getString("name"));
                    roomSkeleton.setCreateDate(rs.getLong("create_date"));
                    roomSkeleton.setPrivate(rs.getBoolean("is_private"));
                    roomSkeleton.setOwner(rs.getString("owner_username"));
                    roomSkeleton.setUnreadMessages(rs.getInt("unread_messages"));
                    roomList.add(roomSkeleton);
                }


                ChameMessage chameMessage = new ChameMessage(
                        ChameProtocol.ROOMS_LIST,
                        gson.toJson(roomList));
                clientSession.sendMessage(gson.toJson(chameMessage));

                OnlineRoomsManager.getInstance().addOnlineUser(clientSession, roomList);

            }catch (SQLException e){
                e.printStackTrace();
                System.out.println("something went wrong while sending roomlist");

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void sendRoomMessages(ChameMessage chameMessage) {
        RoomMessageRequestSkeleton rmrk = gson.fromJson(chameMessage.getBody(), RoomMessageRequestSkeleton.class);
        RoomSkeleton roomSkeleton = rmrk.getRoomSkeleton();
        long afterTime = rmrk.getAfterTime();

        String userExistsQuery = "SELECT message_footprints.id AS footprintID, messages.id, users.username AS sender_username, messages.message_body, messages.create_date, messages.type, message_footprints.is_read " +
                "FROM messages " +
                "INNER JOIN message_footprints ON message_footprints.message_id = messages.id  " +
                "INNER JOIN users ON messages.sender_id = users.id " +
                "WHERE message_footprints.recipient_id = ? AND message_footprints.room_id = ? AND messages.create_date > ? ";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(userExistsQuery)){
            pst.setInt(1, clientSession.getId());
            pst.setInt(2, roomSkeleton.getId());
            pst.setLong(3, afterTime);
            ArrayList<ChatMessageSkeleton> roomMessageList = new ArrayList<>();
            ArrayList<Integer> messageFootPrintIds = new ArrayList<>();
            try(ResultSet rs = pst.executeQuery()){

                while(rs.next()){
                    ChatMessageSkeleton chatMessageSkeleton = new ChatMessageSkeleton();
                    messageFootPrintIds.add(rs.getInt("footprintID"));
                    chatMessageSkeleton.setId(rs.getInt("id"));
                    chatMessageSkeleton.setSenderUsername(rs.getString("sender_username"));
                    chatMessageSkeleton.setBody(rs.getString("message_body"));
                    chatMessageSkeleton.setCreateDate(rs.getLong("create_date"));
                    chatMessageSkeleton.setType(rs.getString("type"));
                    chatMessageSkeleton.setRead(rs.getBoolean("is_read"));
                    chatMessageSkeleton.setRoom_id(roomSkeleton.getId());
                    roomMessageList.add(chatMessageSkeleton);

                }
                RoomMessageResponseSkeleton rmrs = new RoomMessageResponseSkeleton();
                rmrs.setRoom_id(roomSkeleton.getId());
                rmrs.setRoomMessageList(roomMessageList);
                ChameMessage ch = new ChameMessage(
                        ChameProtocol.ROOM_MESSAGES,
                        gson.toJson(rmrs));
                clientSession.sendMessage(gson.toJson(ch));

                setMessagesAsRead(cn, messageFootPrintIds);

            }catch (SQLException e){
                e.printStackTrace();
                System.out.println("something went wrong while sending room message list");

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void setMessagesAsRead(Connection cn, ArrayList<Integer> messageFootPrintIds){
        String sqlQuery = "UPDATE message_footprints SET is_read=1 WHERE id=?";
        try (PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            int count = 0;
            for(int id : messageFootPrintIds){
                pst.setInt(1, id);
                pst.addBatch();
                count += 1;
                // execute every 50 rows or less
                if (count % 50 == 0 || count == messageFootPrintIds.size()) {
                    pst.executeBatch();
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    private void insertMessage(ChameMessage chameMessage) {
        ChatMessageSkeleton cs = gson.fromJson(chameMessage.getBody(), ChatMessageSkeleton.class);
        String sqlQuery = "INSERT INTO messages (sender_id, room_id, message_body, create_date, type) " +
                "VALUES(?, ?, ?, ?, ?)";

        try(Connection cn = DatabaseConnectionPool.getConnection();
            PreparedStatement pst = cn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)){
                pst.setInt(1, clientSession.getId());
                cs.setSenderUsername(clientSession.getUsername());
                pst.setInt(2, cs.getRoom_id());
                pst.setString(3, cs.getBody());
                cs.setCreateDate(System.currentTimeMillis());
                pst.setLong(4, cs.getCreateDate());
                pst.setString(5, "text");

                if(pst.executeUpdate() > 0){

                    try(ResultSet rs = pst.getGeneratedKeys()){
                        if(rs.next()){
                            cs.setId(rs.getInt(1));
                            sendMessageToAllRecipients(cs, cn);
                        }

                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }

                broadcastMessage(cs);

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void sendMessageToAllRecipients(ChatMessageSkeleton cs, Connection cn) {
        String sqlQuery = "INSERT INTO message_footprints (recipient_id, room_id, message_id, is_read) " +
                "SELECT participants.user_id, ?, ?, ? FROM participants WHERE participants.room_id = ?";

        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setInt(1, cs.getRoom_id());
            pst.setInt(2, cs.getId());
            pst.setBoolean(3, false);
            pst.setInt(4, cs.getRoom_id());
            pst.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void sendRoomGameLobbies(ChameMessage chameMessage) {
        ChameMessage ch = new ChameMessage(
                ChameProtocol.ROOM_GAME_LOBBIES,
                gson.toJson(GameHandler.getInstance().gameList.get(Integer.valueOf(chameMessage.getBody()))));
        clientSession.sendMessage(gson.toJson(ch));
    }


    public void broadcastMessage(ChatMessageSkeleton chatMessage){
        RoomMessageResponseSkeleton rmrs = new RoomMessageResponseSkeleton();
        rmrs.setRoom_id(chatMessage.getRoom_id());
        ArrayList<ChatMessageSkeleton> roomMessageList = new ArrayList<>(Arrays.asList(chatMessage));
        rmrs.setRoomMessageList(roomMessageList);
        ChameMessage ch = new ChameMessage(
                ChameProtocol.ROOM_MESSAGES,
                gson.toJson(rmrs));
        String messageJson = gson.toJson(ch);

        Set<ClientSession> usersInRoom = OnlineRoomsManager.getInstance().roomsList.get(chatMessage.getRoom_id());

        if(usersInRoom == null)
            return;

        synchronized (usersInRoom){
            Iterator<ClientSession> it = usersInRoom.iterator();
            while(it.hasNext()){
                ClientSession onlineClientSession = it.next();
                onlineClientSession.sendMessage(gson.toJson(ch));
            }
        }
    }

    private void deleteMessage(ChameMessage chameMessage) {
        ChatMessageSkeleton cs = gson.fromJson(chameMessage.getBody(), ChatMessageSkeleton.class);

        String sqlQuery = "DELETE FROM messages WHERE messages.id = ? ";

        try(Connection cn = DatabaseConnectionPool.getConnection();
            PreparedStatement pst = cn.prepareStatement(sqlQuery)){

            pst.setInt(1, cs.getId());

            pst.executeUpdate();

            String sqlQuery2 = "DELETE FROM message_footprints WHERE message_id = ?";
            try(PreparedStatement pst2 = cn.prepareStatement(sqlQuery2)){

                pst2.setInt(1, cs.getId());

            }catch (SQLException e){
                e.printStackTrace();
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////
    ///////////// SEND CONTACT LIST ///////////////////////////////
    ///////////////////////////////////////////////////////////////

    private void sendContactList() {
        String userExistsQuery = "SELECT relations.id, relations.is_accepted, users1.is_online AS sender_is_online, users1.username AS sender, users2.is_online AS receiver_is_online, users2.username AS receiver " +
                "FROM relations " +
                "LEFT JOIN users users1 " +
                "    ON users1.id = relations.sender_id " +
                "LEFT JOIN users users2 " +
                "    ON users2.id = relations.recipient_id " +
                "WHERE recipient_id=?  OR sender_id=?";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(userExistsQuery)){
            pst.setInt(1, clientSession.getId());
            pst.setInt(2, clientSession.getId());
            ArrayList<ContactSkeleton> contactList = new ArrayList<>();
            try(ResultSet rs = pst.executeQuery()){

                while(rs.next()){
                    ContactSkeleton ck = new ContactSkeleton();
                    int relationID = rs.getInt("id");
                    String sender = rs.getString("sender");
                    boolean senderOnline = rs.getBoolean("sender_is_online");
                    String receiver = rs.getString("receiver");
                    boolean receiverOnline = rs.getBoolean("receiver_is_online");
                    boolean accepted = rs.getBoolean("is_accepted");

                    if(sender.equals(clientSession.getUsername())){
                        ck.setUsername(receiver);
                        ck.setOnline(receiverOnline);
                        ck.setRelationID(relationID);
                        if(accepted)
                            ck.setRelationType("friend");
                        else
                            ck.setRelationType("waiting");
                    }else{
                        ck.setUsername(sender);
                        ck.setOnline(senderOnline);
                        ck.setRelationID(relationID);
                        if(accepted)
                            ck.setRelationType("friend");
                        else
                            ck.setRelationType("pending");
                    }
                    contactList.add(ck);
                }

                ChameMessage chameMessage = new ChameMessage(
                        ChameProtocol.CONTACTS_LIST,
                        gson.toJson(contactList));
                clientSession.sendMessage(gson.toJson(chameMessage));

            }catch (SQLException e){
                e.printStackTrace();
                System.out.println("something went wrong while sending friend request");

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////
    ///////////// FRIEND REQUEST //////////////////////////////////
    ///////////////////////////////////////////////////////////////

    private void sendFriendRequest(ChameMessage chameMessage) {

        String userExistsQuery = "SELECT id FROM users WHERE username=?";
        try (Connection cn = DatabaseConnectionPool.getConnection();
            PreparedStatement pst = cn.prepareStatement(userExistsQuery)){
            pst.setString(1, chameMessage.getBody());
            try(ResultSet rs = pst.executeQuery()){

                //check whether a user with this username exists
                if(rs.next()){
                    int recipientID = rs.getInt("id");
                    bidirectionCheck(clientSession.getId(), recipientID, cn);

                }else{
                    sendError("Could not find a someone with username: " + chameMessage.getBody());
                }

            }catch (SQLException e){
                e.printStackTrace();
                System.out.println("something went wrong while sending friend request");

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        sendContactList();
    }

    private void bidirectionCheck(int senderID, int recipientID, Connection cn){
        //this query checks whether there's a pending request the other way around
        String bidirectioncheck = "SELECT id FROM relations WHERE sender_id=? AND recipient_id=?";
        try(PreparedStatement pst = cn.prepareStatement(bidirectioncheck);){
            pst.setInt(1, recipientID);
            pst.setInt(2, senderID);

            try(ResultSet rs = pst.executeQuery()){

                //the other direction exists then set it as accepted
                if(rs.next()){
                    int relationID = rs.getInt("id");
                    updateRelation(relationID, true, cn);

                }else {
                //otherwise insert a new pending relation
                    insertRelation(senderID, recipientID, cn);
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("something went wrong while checking bidirection in friend request");
        }
    }

    private void updateRelation(int relationID, boolean is_accepted, Connection cn){
        String bidirectioncheck = "UPDATE relations SET is_accepted=? WHERE id=?";
        try(PreparedStatement pst = cn.prepareStatement(bidirectioncheck);){
            pst.setBoolean(1, is_accepted);
            pst.setInt(2, relationID);
            pst.execute();

        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("something went wrong while processing the friend request");
        }
    }

    private void insertRelation(int senderID, int recipientID, Connection cn){
        String sqlQuery = "INSERT INTO relations (sender_id, recipient_id, is_accepted) " +
                "VALUES (?, ?, ?)";
        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setInt(1, senderID);
            pst.setInt(2, recipientID);
            pst.setBoolean(3, false);

            pst.execute();
        }catch (SQLException e){
            System.out.println("Failed to insert relation");
            e.printStackTrace();
        }
    }

    private void deleteRelation(ChameMessage chameMessage){
        String toBeDeniedUsername = chameMessage.getBody();
        String sqlQuery = "DELETE FROM relations WHERE EXISTS " +
                "(SELECT 1 FROM users " +
                "WHERE(users.username = ? AND " +
                "((relations.sender_id=users.id AND relations.recipient_id=?)  OR (relations.sender_id=? AND relations.recipient_id=users.id))))";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(sqlQuery)){

            pst.setString(1, toBeDeniedUsername);
            pst.setInt(2, clientSession.getId());
            pst.setInt(3, clientSession.getId());
            pst.execute();
        }catch (SQLException e){
            System.out.println("Failed to delete relation");
            e.printStackTrace();
        }
        sendContactList();
    }







    ///////////////////////////////////////////////////////////////
    ///////////// LOGIN ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    private void loginUser(ChameMessage chameMessage) {
        LoginSkeleton loginSkeleton = gson.fromJson(chameMessage.getBody(), LoginSkeleton.class);
        String sqlQuery = "SELECT id FROM users WHERE username=? AND password=?";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setString(1, loginSkeleton.getUsername());
            pst.setString(2, loginSkeleton.getPassword());
            try(ResultSet rs = pst.executeQuery();){
                //if we have a result then the login was succesfull
                if(rs.next()){
                    int id = rs.getInt("id");
                    System.out.println("User with id: " + id + " logged in.");
                    String updateOnlineStatus = "UPDATE users SET is_online=true WHERE id=?";
                    //set the status to online
                    try(PreparedStatement pst2 = cn.prepareStatement(updateOnlineStatus)){
                        pst2.setInt(1, id);
                        pst2.execute();
                        clientSession.setId(id);
                        clientSession.setUsername(loginSkeleton.getUsername());
                        clientSession.setOnline(true);
                    }catch (SQLException e){
                        e.printStackTrace();
                        System.out.println("something went wrong while changing the user status to online");
                    }
                    sendSuccessfulLogin();
                }else{
                    //invalid credentials
                    sendError("Username of password is incorrect.");
                }
            }catch (SQLException e){
                System.out.println("something went wrong while checking whether the user already exists or not");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //sends a message to user indicating that the login was successful
    public void sendSuccessfulLogin(){
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.LOGIN_SUCCESS,
                "successfuly loggedin"
        );
        clientSession.sendMessage(gson.toJson(chameMessage));
    }

    private void recoverPassword(ChameMessage chameMessage) {
        ForgotPasswordSkeleton forgotPasswordSkeleton = gson.fromJson(chameMessage.getBody(), ForgotPasswordSkeleton.class);
        String sqlQuery = "SELECT password FROM users WHERE email=? AND question=? AND answer=?";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setString(1, forgotPasswordSkeleton.getEmail());
            pst.setString(2, forgotPasswordSkeleton.getRecoveryQuestion());
            pst.setString(3, forgotPasswordSkeleton.getAnswer());
            try(ResultSet rs = pst.executeQuery();){
                //if we have a result then the login was succesfull
                if(rs.next()){
                    String password = rs.getString("password");
                    ChameMessage ch = new ChameMessage(
                            ChameProtocol.RECOVERED_PASSWORD,
                            gson.toJson(new RecoveredPasswordSkeleton(forgotPasswordSkeleton.getEmail(), password))
                    );
                    clientSession.sendMessage(gson.toJson(ch));

                }else{
                    //invalid answer
                    sendError("Could not find any user with this combination of question and answer");
                }
            }catch (SQLException e){
                System.out.println("something went wrong while recovering");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    ///////////////////////////////////////////////////////////////
    //////////// REGISTER /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    //tries to register the user
    private void registerUser(ChameMessage chameMessage){
        RegisterSkeleton registerSkeleton = gson.fromJson(chameMessage.getBody(), RegisterSkeleton.class);

        try (Connection cn = DatabaseConnectionPool.getConnection();){
            if(userAlreadyExists(cn, registerSkeleton.getUsername(), registerSkeleton.getEmail())){
                //send error
                sendError("username or email already in use");

            }else{
                insertRegisteredUser(cn, registerSkeleton);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //checks whether the user already exists in he database or not
    private boolean userAlreadyExists(Connection cn, String username, String email){
        String sqlQuery = "SELECT EXISTS(SELECT username FROM users WHERE username=? OR email=?)";
        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setString(1, username);
            pst.setString(2, email);

            try(ResultSet rs = pst.executeQuery();){

                return rs.getBoolean(1);

            }catch (SQLException e){
                System.out.println("something went rong while checking whether the user already exists or not");
            }


        }catch (SQLException e){
            System.out.println("Failed to insert registered user");
            e.printStackTrace();
        }
        //return true by default
        return true;
    }

    private void insertRegisteredUser(Connection cn, RegisterSkeleton registerSkeleton){

        String sqlQuery = "INSERT INTO USERS (username, password, email, question, answer, is_online, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement pst = cn.prepareStatement(sqlQuery)){
            pst.setString(1, registerSkeleton.getUsername());
            pst.setString(2, registerSkeleton.getPassword());
            pst.setString(3, registerSkeleton.getEmail());
            pst.setString(4, registerSkeleton.getQuestion());
            pst.setString(5, registerSkeleton.getAnswer());
            pst.setBoolean(6, false);
            pst.setLong(7, System.currentTimeMillis());
            pst.execute();
        }catch (SQLException e){
            System.out.println("Failed to insert registered user");
            e.printStackTrace();
        }
    }

    private void sendError(String errorMessage){
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.ERROR,
                errorMessage
        );

        clientSession.sendMessage(gson.toJson(chameMessage));

    }





    public String dequeueClientSessionMessage(){
        return clientSession.getChameMessageReader().getFullMessage();
    }
}
