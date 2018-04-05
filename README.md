**How to build**
1. Execute `mvn clean package`
2. You can find build file in `target/event-processor-1.0-jar-with-dependencies.jar`

**How to run server**
1. Build server
2. execute java -jar `target/event-processor-1.0-jar-with-dependencies.jar`
 If you change _maxEventSourceBatchSize_ in follower-maze-2.0.jar you should change this
 value  when you run this server 

**How to configure application**

You can configure the following environment variables:

1. _eventListenerPort_ - Default: 9090

2. _clientListenerPort_ - Default: 9099

3. _maxEventSourceBatchSize_ - Default: 100

4. _hostName_ - Default:127.0.0.1


