package mutfia.client.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import mutfia.global.response.CustomProtocolMessage;

public class ClientMessageHandler {
    private static final Map<String, Consumer<CustomProtocolMessage>> handlers = new HashMap<>();

    public static void register(String type, Consumer<CustomProtocolMessage> handler){
        handlers.put(type, handler);
    }

    public static void unregister(String type){
        handlers.remove(type);
    }

    public static void dispatch(CustomProtocolMessage customProtocolMessage){
        Consumer<CustomProtocolMessage> handler = handlers.get(customProtocolMessage.type);

        if(handler != null){
            handler.accept(customProtocolMessage);
        } else {
            System.out.println("[Error] 해당 타입에 대한 핸들러 없음 : " + customProtocolMessage.type);
        }
    }
}
