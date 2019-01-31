import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

public class EngineApiTest
{
  @Test
  public void exportDataTest () 
  {
    for (int i = 0; i < 3; i++)
    {
      System.out.println("###########################################################################################");
      try {
        EngineSession session = new EngineSession("<myhost>", "<userdirectory>\\<userid>");
        long docHandle = session.openDoc("<qlik app-id>", 1);
          
        // Get field and provide filter values
        long fieldHandle = session.getField(docHandle, 2, "<field to select>"); // e.g. "OrderNumber"
        List<String> values = new ArrayList<String>();
        values.add("P1");// Change these
        values.add("P3");
        session.selectValues(fieldHandle, 3, values);
          
        long objectHandle = session.getObject(docHandle, 4, "<qlik object-id of a straight table>"); // e.g. "lAawW"
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
