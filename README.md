# Spring Boot 3 reactive services with Micrometer and Logbook 3.0.0

## Structure

This is a maven multimodule project. There are two modules:

* `logbook-configuration`: Load the default logbook configuration defined in `src/main/resources/logbook-config.yml` and
  include logbook-dependencies.
* `observability-configuration`: This module contains the micrometer dependencies for obersavility.

And two services that includes these two modules:

* `service1`: include a webclient to make a call to `service2`.
* `service2`: exposes a single endpoint used by `service1`.

## Build

```shell
mvn clean package
```

## Problem

TraceId and spanId are not used in logbook logs. SpanId is used in service1 (but not the traceId, except in the last log
of the first call) and there is no
traceId or spanId in service2.

I have tried different configurations for the webclient:

```java

@Configuration
@Slf4j
public class Service2WebClientConfiguration {

    @Bean
    public WebClient service2WebClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8081").build();
    }
}

```

and:

```java

@Configuration
@Slf4j
public class Service2WebClientConfiguration {

    @Bean
    public WebClient service2WebClient(Logbook logbook, WebClient.Builder builder) {

        HttpClient httpClient =
                HttpClient.create()
                        .doOnConnected(
                                (connection -> connection.addHandlerLast(new LogbookClientHandler(logbook))));

        return builder
                .baseUrl("http://localhost:8081")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

but both configurations give the same result.

The latest spring boot version at the moment is 3.1.0. I have tried previous 3.x versions, and with some I had the same
results and with others there was no spandId in the logbook logs.

## How to reproduce

```
java -jar service1/target/service1-1.0.0-SNAPSHOT.jar
java -jar service2/target/service2-1.0.0-SNAPSHOT.jar
```

And then:

```
curl http://localhost:8080/service1
```

### Logs for service1

```
.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/
:: Spring Boot ::                (v3.1.0)

2023-05-28T20:37:35.921+02:00  INFO [service1,,] 4322 --- [           main] org.lzh.logbook.service1.Application     : Starting Application v1.0.0-SNAPSHOT using Java 17.0.6 with PID 4322
2023-05-28T20:37:35.922+02:00  INFO [service1,,] 4322 --- [           main] org.lzh.logbook.service1.Application     : No active profile set, falling back to 1 default profile: "default"
2023-05-28T20:37:36.913+02:00  INFO [service1,,] 4322 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
2023-05-28T20:37:37.061+02:00  INFO [service1,,] 4322 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080
2023-05-28T20:37:37.077+02:00  INFO [service1,,] 4322 --- [           main] org.lzh.logbook.service1.Application     : Started Application in 1.359 seconds (process running for 1.555)
2023-05-28T20:38:08.083+02:00  INFO [service1,64739f90c9e13ec3a7048d5dfba4c499,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] o.l.l.s.controller.Service1Controller    : Service calling to service2...
2023-05-28T20:38:08.139+02:00 TRACE [service1,,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"remote","type":"request","correlation":"cb3e0b95acf8eee5","protocol":"HTTP/1.1","remote":"/127.0.0.1:50990","method":"GET","uri":"http://localhost:8080/service1","host":"localhost","path":"/service1","scheme":"http","port":"8080","headers":{"Accept":["*/*"],"Host":["localhost:8080"],"User-Agent":["curl/7.88.1"]}}
2023-05-28T20:38:08.147+02:00 TRACE [service1,,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"local","type":"request","correlation":"c3bfc83aec2fc445","protocol":"HTTP/1.1","remote":"localhost/127.0.0.1:8081","method":"GET","uri":"http://localhost:8081/service2","host":"localhost","path":"/service2","scheme":"http","port":"8081","headers":{"accept":["*/*"],"host":["localhost:8081"],"traceparent":["00-64739f90c9e13ec3a7048d5dfba4c499-2f50f2a1fa218f10-00"],"user-agent":["ReactorNetty/1.1.7"]}}
2023-05-28T20:38:08.240+02:00 TRACE [service1,,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"remote","type":"response","correlation":"c3bfc83aec2fc445","duration":93,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
2023-05-28T20:38:08.246+02:00 TRACE [service1,64739f90c9e13ec3a7048d5dfba4c499,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"local","type":"response","correlation":"cb3e0b95acf8eee5","duration":189,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
```

If we do a second call:

```
2023-05-28T20:41:13.159+02:00  INFO [service1,6473a04995d2cc36150a89851129d53c,150a89851129d53c] 4322 --- [ctor-http-nio-4] o.l.l.s.controller.Service1Controller    : Service calling to service2...
2023-05-28T20:41:13.161+02:00 TRACE [service1,,150a89851129d53c] 4322 --- [ctor-http-nio-4] org.zalando.logbook.Logbook              : {"origin":"remote","type":"request","correlation":"95d2b06b914bd534","protocol":"HTTP/1.1","remote":"/127.0.0.1:51038","method":"GET","uri":"http://localhost:8080/service1","host":"localhost","path":"/service1","scheme":"http","port":"8080","headers":{"Accept":["*/*"],"Host":["localhost:8080"],"User-Agent":["curl/7.88.1"]}}
2023-05-28T20:41:13.162+02:00 TRACE [service1,,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"local","type":"request","correlation":"c24287b3deacee02","protocol":"HTTP/1.1","remote":"localhost/127.0.0.1:8081","method":"GET","uri":"http://localhost:8081/service2","host":"localhost","path":"/service2","scheme":"http","port":"8081","headers":{"accept":["*/*"],"host":["localhost:8081"],"traceparent":["00-6473a04995d2cc36150a89851129d53c-682941df7443c0bb-00"],"user-agent":["ReactorNetty/1.1.7"]}}
2023-05-28T20:41:13.167+02:00 TRACE [service1,,a7048d5dfba4c499] 4322 --- [ctor-http-nio-3] org.zalando.logbook.Logbook              : {"origin":"remote","type":"response","correlation":"c24287b3deacee02","duration":5,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
2023-05-28T20:41:13.169+02:00 TRACE [service1,,150a89851129d53c] 4322 --- [ctor-http-nio-4] org.zalando.logbook.Logbook              : {"origin":"local","type":"response","correlation":"95d2b06b914bd534","duration":11,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
```

### Logs for service2:

```
.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/
:: Spring Boot ::                (v3.1.0)

2023-05-28T20:38:00.131+02:00  INFO [service2,,] 4353 --- [           main] org.lzh.logbook.service2.Application     : Starting Application v1.0.0-SNAPSHOT using Java 17.0.6 with PID 4353
2023-05-28T20:38:00.132+02:00  INFO [service2,,] 4353 --- [           main] org.lzh.logbook.service2.Application     : No active profile set, falling back to 1 default profile: "default"
2023-05-28T20:38:00.829+02:00  INFO [service2,,] 4353 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
2023-05-28T20:38:01.042+02:00  INFO [service2,,] 4353 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8081
2023-05-28T20:38:01.057+02:00  INFO [service2,,] 4353 --- [           main] org.lzh.logbook.service2.Application     : Started Application in 1.131 seconds (process running for 1.331)
2023-05-28T20:38:08.217+02:00  INFO [service2,64739f90c9e13ec3a7048d5dfba4c499,61c824a2d63bbe97] 4353 --- [ctor-http-nio-2] o.l.l.s.controller.Service2Controller    : Received
2023-05-28T20:38:08.251+02:00 TRACE [service2,,] 4353 --- [ctor-http-nio-2] org.zalando.logbook.Logbook              : {"origin":"remote","type":"request","correlation":"face2ef92c7f5d52","protocol":"HTTP/1.1","remote":"/127.0.0.1:50991","method":"GET","uri":"http://localhost:8081/service2","host":"localhost","path":"/service2","scheme":"http","port":"8081","headers":{"accept":["*/*"],"host":["localhost:8081"],"traceparent":["00-64739f90c9e13ec3a7048d5dfba4c499-2f50f2a1fa218f10-00"],"user-agent":["ReactorNetty/1.1.7"]}}
2023-05-28T20:38:08.254+02:00 TRACE [service2,,] 4353 --- [ctor-http-nio-2] org.zalando.logbook.Logbook              : {"origin":"local","type":"response","correlation":"face2ef92c7f5d52","duration":63,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
```

Second call:

```
2023-05-28T20:41:13.164+02:00  INFO [service2,6473a04995d2cc36150a89851129d53c,d4652e7aa56841a6] 4353 --- [ctor-http-nio-2] o.l.l.s.controller.Service2Controller    : Received
2023-05-28T20:41:13.168+02:00 TRACE [service2,,] 4353 --- [ctor-http-nio-2] org.zalando.logbook.Logbook              : {"origin":"remote","type":"request","correlation":"9f6db60def6cd630","protocol":"HTTP/1.1","remote":"/127.0.0.1:50991","method":"GET","uri":"http://localhost:8081/service2","host":"localhost","path":"/service2","scheme":"http","port":"8081","headers":{"accept":["*/*"],"host":["localhost:8081"],"traceparent":["00-6473a04995d2cc36150a89851129d53c-682941df7443c0bb-00"],"user-agent":["ReactorNetty/1.1.7"]}}
2023-05-28T20:41:13.168+02:00 TRACE [service2,,] 4353 --- [ctor-http-nio-2] org.zalando.logbook.Logbook              : {"origin":"local","type":"response","correlation":"9f6db60def6cd630","duration":5,"protocol":"HTTP/1.1","status":200,"headers":{"Content-Length":["5"],"Content-Type":["text/plain;charset=UTF-8"]},"body":"done!"}
```
