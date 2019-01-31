import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;

public class EngineSession
{
  public static final String OPEN_DOC = "OpenDoc";
  public static final String X_QLIK_XRFKEY = "x-Qlik-Xrfkey";
  public static final String QLIK_XRFKEY = "abcdefghijklmnop";
  public static final String IV_USER = "iv-user";
  public static final String SLASH_IV = "/iv";
  public static final String EQUAL = "=";
  public static final String QUESTION_MARK = "?";
  public static final String XRFKEY = "Xrfkey";

  // This map stores the <userid, cookie> pair
  private static Map<String, String> userCookieMap;
  static {
    userCookieMap = new HashMap<String, String>();
  }

  private String userid;
  private WebSocketClientImpl ws;
  
  public EngineSession (String host, String userid) 
    throws URISyntaxException, InterruptedException
  {
    this.userid = userid;
    this.ws = createSocket(host, userid, userCookieMap.get(userid));

    System.out.println("Printing userCookieMap");
    Iterator<String> itr = userCookieMap.keySet().iterator();
    while (itr.hasNext())
    {
      String key = itr.next();
      System.out.println("\tuserCookieMap => " + key + ": " + userCookieMap.get(key));
    }

    // Even though socket is created, the onOpen method may not have been called yet. So, sleep till the cookie is generated or it fails
    int tries = 0;
    while (null == ws.getCookie() && tries++ < 10)
    {
      try {
        Thread.sleep(500);
      }
      catch (Exception e) {}
    }

    if (ws.getCookie() != null && !ws.getCookie().isEmpty()) 
    {
      System.out.println("before putting into cookiemap: [" + ws.getCookie() + "]");
      userCookieMap.put(userid, ws.getCookie());
    }
  }

  public WebSocketClientImpl createSocket (String host, String userid, String cookie)
    throws URISyntaxException, InterruptedException
  {
    WebSocketClientImpl webSocketClient = null;

    String xrfKey = QLIK_XRFKEY;

    Map<String, String> headers = new HashMap<String, String>();
    headers.put(IV_USER, userid);
    headers.put(X_QLIK_XRFKEY,  xrfKey);
    if (cookie != null)
      headers.put("Cookie", cookie);

    System.out.println("OpenConnection: headers: " + headers);

    String uri = "ws://" + host + SLASH_IV + "/app/%3Ftransient%3D" + QUESTION_MARK + XRFKEY + EQUAL + xrfKey;
    System.out.println("OpenConnection: uri: " + uri);

    // Connect to QlikSense using WebSocket connections
    webSocketClient = new WebSocketClientImpl(new URI(uri),new Draft_17(), headers, 5000); // The Draft object needs to be Draft_17 for this to work.
    webSocketClient.connectBlocking();

    return webSocketClient;
  }

  private long invokeMethodAndReturnHandle (String jsonStr, long id) 
  {
    ws.send(jsonStr);

    // Wait for response with id
    while (!ws.find(id+"")) {
      try { Thread.sleep(100); } catch (Exception e) {}
    }
    
    return ws.getHandle();
  }

  public long openDoc (String appId, long id)
  {
    // Prepare the OpenDoc method for the appId
    String jsonStr = "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", \"method\": \"OpenDoc\", \"handle\": -1, \"params\": [\"" + appId + "\"] }";

    System.out.println("OpenDoc: " + jsonStr);

    // Invoke the OpenDoc method for the appId
    return invokeMethodAndReturnHandle(jsonStr, 1);
  }

  public long getField (long handle, long id, String fieldName)
  {
    // Prepare and invoke the GetField method
    String jsonStr = "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", \"method\": \"GetField\", \"handle\": " + handle + ", \"params\": { \"qFieldName\": \"" + fieldName + "\", \"qStateName\": \"\" } }";
    System.out.println("Sending GetField: " + jsonStr);

    return invokeMethodAndReturnHandle(jsonStr, id);
  }

  public void selectValues (long handle, long id, List<String> values)
  {
    // Prepare and invoke the GetField method
    String filters ="";
    int ctr = 1;
    for (String value: values) {
      filters += "{\"qText\":\"" + value + "\"}";
      if (ctr++ < values.size())
        filters += ",";
    }
    String jsonStr = "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", \"method\": \"SelectValues\", \"handle\": " + handle + ", \"params\": [ [" + filters + "], false, false] }";
    System.out.println("Sending SelectValues: " + jsonStr);

    ws.send(jsonStr);

    // Wait for response with id
    while (!ws.find(id+"")) {
      try { Thread.sleep(100); } catch (Exception e) {}
    }
  }

  public long getObject (long handle, long id, String objectId)
  {
    // Prepare and invoke the GetObject method
    String jsonStr = "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", \"method\": \"GetObject\", \"handle\": " + handle + ", \"params\": { \"qId\": \"" + objectId + "\" } }";
    System.out.println("Sending: " + jsonStr);
    return invokeMethodAndReturnHandle(jsonStr, id);
  }

  public String exportData (long handle, long id)
  {
    // Prepare and invoke the GetObject method
    String jsonStr = "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", \"method\": \"ExportData\", \"handle\": " + handle + ", \"params\": [ \"CSV_C\", \"/qHyperCubeDef\", \"CsvUTF8.csv\" ] }";
    System.out.println("Sending: " + jsonStr);
    ws.send(jsonStr);

    // Wait for response with id
    while (!ws.find(id+"")) {
      try { Thread.sleep(100); } catch (Exception e) {}
    }
    
    return ws.getUrl();
  }

  public void close ()
  {
    if (ws != null) 
    {
      try {
        ws.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

}
