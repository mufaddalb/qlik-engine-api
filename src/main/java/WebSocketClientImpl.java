import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebSocketClientImpl extends WebSocketClient
{
  private List<JSONObject> messages = Collections.synchronizedList(new ArrayList<JSONObject>());
  public String cookie = null;

  public WebSocketClientImpl (URI serverUri, Draft draft, Map<String, String> headers, int connecttimeout)
  {
    super(serverUri, draft, headers, connecttimeout);
  }

  public String getCookie () {
    return cookie;
  }

  @Override
  public void onOpen (ServerHandshake handshakedata)
  {
    System.out.println("Connected");
    cookie = handshakedata.getFieldValue("Set-Cookie");
    System.out.println("Access-Control-Allow-Origin: " + handshakedata.getFieldValue("Access-Control-Allow-Origin"));
    System.out.println("Cookie: " + cookie);
    if (cookie != null)
      cookie = cookie.replaceAll("(.*?);.*", "$1");
  }

  @Override
  public synchronized void onMessage (String message)
  {
    JSONParser jsonParser = new JSONParser();
    JSONObject json = null;
    try {
      json = (JSONObject) jsonParser.parse(message);
      messages.clear();
      messages.add(json);
    }
    catch (ParseException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClose (int code, String reason, boolean remote)
  {
    System.out.println( "Disconnected.. code: " + code + ", reason: " + reason);
  }

  @Override
  public void onError (Exception ex)
  {
    System.out.println("Error occurred:" + ex);
  }

  public synchronized long getHandle ()
  {
    for (JSONObject json : getMessages())
    {
      System.out.println("gethandle: json: " + json);
      JSONObject result = (JSONObject)json.get("result");
      JSONObject qReturn = (JSONObject)result.get("qReturn");
      Long handle = (Long)qReturn.get("qHandle");

      if (handle != null) {
        return handle.longValue();
      }
    }
    return -1;
  }

  public synchronized String getUrl ()
  {
    String url = null;
    for (JSONObject json : getMessages())
    {
      JSONObject result = (JSONObject)json.get("result");
      url = (String)result.get("qUrl");
      if (url != null)
        return url;
    }

    return url;
  }

  public synchronized boolean isConnected ()
  {
    boolean flag = false;
    for (JSONObject json : getMessages())
    {
      String method= (String) json.get("method");
      if (method != null && (method.equals("OnConnected") || method.equals("OnLicenseAccessDenied")) )
      {
        flag = true;
        break;
      }
    }
    return flag;
  }

  public synchronized boolean find (String id)
  {
    boolean flag = false;
    for (JSONObject json : getMessages())
    {
      Long resultId = (Long) json.get("id");
      if (resultId != null)
      {
        String idStr = resultId.toString();
        if (idStr.equals(id))
        {
          flag = true;
          break;
        }
      }
      else
      {
        if (json.toString().contains("OnLicenseAccessDenied"))
        {
          flag = true;
          break;
        }
      }
    }

    return flag;
  }

  public synchronized List<JSONObject> getMessages() {
    return messages;
  }
}
