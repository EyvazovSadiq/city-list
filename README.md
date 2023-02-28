# City List App

City List app mono repository consists of following modules:
- [`city-list-ui/ui`](city-list-ui/ui/README.md)
- [`city-list-service/service`](city-list-service/service/README.md)

Click on each module to see the respective documentation.

## How to start the application

In order to start the application, run [docker-compose.yml](docker-compose.yml) file by executing `$ docker-compose up` command in parent folder.

Afterwards, open http://localhost:3000 in a browser to surf through the Cities.

### Applicaiton Behaviour

On application start-up you can see the app is loading for a couple of seconds, which means the Database is being populated with City List Data.


### Future Improments
Store images, for example, on Amazon S3 for high scalability.