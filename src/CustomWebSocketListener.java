import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

public class CustomWebSocketListener implements WebSocketListener{

	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		System.out.print("Message: Socket closoed");
		
	}

	@Override
	public void onError(Throwable th) {
		// TODO Auto-generated method stub
		System.out.print("Message: " + th.getMessage());
		
	}

	@Override
	public void onOpen(WebSocket ws) {
		// TODO Auto-generated method stub
		System.out.print("Message: " + ws.toString());
	}
	
	
	public void onMessage(WebSocket webSocket, String message) {
        // Handle WebSocket message received event
		System.out.print("Message: " + message);
    }

}
