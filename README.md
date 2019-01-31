# qlik-engine-api

How to run?
Checkout the project
src/test/java/EngineApiTest.java uses placeholders in <>. Replace those with your own values.
Run 'mvn install' to compile and run the test.

What does the testcase do?
Create a websocket using host/userid.
Open a Qlik app using its appid.
Get hold of a Field and select values for filtering.
Get hold of a straight table object using its object-id
Export data from the table to C:/ProgramData/Qlik/Sense/Repository/TempContent

Assumptions:
A Virtual Proxy named 'iv' is availabe and uses header authentication. 
EngineSession.java uses following uri to create socket which includes the virtual proxy 'iv' in it.
    String uri = "ws://" + host + "/iv/app/%3Ftransient%3D?Xrfkey=" + xrfKey;
