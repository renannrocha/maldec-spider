# Building the project

## Using Docker (Recommended):
```sh
git clone git@github.com:maldeclabs/spider.git
cd spider
docker build -t spider .
docker run -dt -p 8080:8080 --name spider spider mvn spring-boot:run
```

The service will be available at http://localhost:8080.

## Building from source

### Dependencies

\-


1. Clone the repository
```
git clone git@github.com:maldeclabs/spider.git && cd spider
```
