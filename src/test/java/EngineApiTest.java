import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
// import org.java_websocket.client.WebSocketClient;
// import org.java_websocket.drafts.Draft;
// import org.java_websocket.drafts.Draft_17;
// import com.e2open.qlik.util.Constants;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

public class EngineApiTest
{
  private static final Logger LOGGER = Logger.getLogger(EngineApiTest.class);

  @Test
  public void exportDataTest () 
  {
    for (int i = 0; i < 3; i++)
    {
      System.out.println("###########################################################################################");
      try {
        EngineSession session = new EngineSession("dev1473", "dev1473\\qlikadmin");
        long docHandle = session.openDoc("316985fa-ec29-489c-8148-5691df222176", 1);
          
        // Get field and provide filter values
        long fieldHandle = session.getField(docHandle, 2, "PoNum");
        List<String> values = new ArrayList<String>();
        values.add("P1");
        values.add("P3");
        session.selectValues(fieldHandle, 3, values);
          
        long objectHandle = session.getObject(docHandle, 4, "mAWqV");
        String url = session.exportData(objectHandle, 5);
        System.out.println("exported file location: " + url);
        session.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
