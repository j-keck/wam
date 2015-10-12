# wam: web app mirror

to observe / instruct a web app user. the user actions / user view (driver) are mirrored under a different port (co-driver).


#### usage
   
  * startup the app: "sbt ';compile; server/run'"
  * driver connects to 'http://localhost:8000'
  * the web app which runs under 'http://localhost:80' are wrapped
  * the co-driver connect to 'http://localhost:8001'
  * mouse actions from the driver are mirrored to the co-drivers view
  * all request from the driver are served from the wrapped web app
  * the request from the co-driver are served by this application - so the wrapped
    application serves only the request for the driver
   
   