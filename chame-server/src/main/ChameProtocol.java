package main;

public class ChameProtocol {
    //General
    public static final String ERROR = "ERROR";

    //Authentication
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String RECOVERED_PASSWORD = "RECOVERED_PASSWORD";

    //Contacts and friend requests
    public static final String FRIEND_REQUEST = "FRIEND_REQUEST";
    public static final String FRIEND_REQUEST_DENY = "FRIEND_REQUEST_DENY";

    public static final String GET_CONTACTS = "GET_CONTACTS";
    public static final String CONTACTS_LIST = "CONTACTS_LIST";


    //Chat
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String JOIN_ROOM = "JOIN_ROOM";

    public static final String GET_ROOMS = "GET_ROOMS";
    public static final String ROOMS_LIST = "ROOMS_LIST";

    public static final String GET_ROOM_MESSAGES = "GET_ROOM_MESSAGES";
    public static final String ROOM_MESSAGES = "ROOM_MESSAGES";

    public static final String CHAT_MESSAGE = "CHAT_MESSAGE";

    public static final String DELETE_MESSAGE = "DELETE_MESSAGE";

    public static final String GET_ROOM_GAME_LOBBIES = "GET_ROOM_GAME_LOBBIES";
    public static final String ROOM_GAME_LOBBIES = "ROOM_GAME_LOBBIES";

    //Game
    public static final String CREATE_GAME = "CREATE_GAME";
    public static final String JOIN_GAME = "JOIN_GAME";
    public static final String GAME_ACTION = "GAME_ACTION";
    public static final String START_GAME = "START_GAME";
    public static final String END_GAME = "END_GAME";
    public static final String TIE = "~tie";
    public static final String ABRUPT_LEAVE = "ABRUPT_LEAVE";

    //Games
    public static final String TICTACTOE = "tictactoe";


}
