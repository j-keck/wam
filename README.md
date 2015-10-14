# wam: web app mirror for single page applications (SPA)

to observe / instruct a web app user. the user actions / user view (driver) are mirrored (co-driver).


#### usage
   
  * compile / package it: `sbt assembly`
  * startup the app: `java -jar server/target/scala-2.11/wam-<VERSION>.jar -h <TARGET_WEBAPP_HOST>`
  * the web app which runs under 'http://&lt;TARGET_WEBAPP_HOST&gt;:80' are wrapped
  * driver connects to 'http://localhost:8000'
  * the co-driver connect to 'http://localhost:8001'
  * all input / actions from the driver are mirrored to the co-drivers view
  * all request from the driver are served from the wrapped web app
  * the request from the co-driver are served by this application - so the wrapped
    application serves only the request for the driver
   
   

   
## dev

    ~;client/fastOptJS; re-start -h localhost