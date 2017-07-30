Building
==========================================================================

To build, test and install whole suite run following command from rps dir:
    
    mvn clean install

To build runnable server, follow this steps:
1. cd server
2. mvn package -Dbuild=full
3. mvn docker:build



Running
==========================================================================
To run server, execute following command:

    docker run -p 8080:8080 --name rps sevteen/rps:latest

To run CLI client:

    java -jar cli/target/cli-0.1.0.jar localhost 8080
