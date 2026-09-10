# CDOC2 Auth Server Administration Guide

This document describes how to configure and run `cdoc2-auth-server`.

## Monitoring

### Tracing

`cdoc2-auth-server` supports distributed tracing using OpenTelemetry Protocol (OTLP) through Spring Boot's
Micrometer integration. To visualize traces, you need to run a compatible trace backend.

#### Configuration
To enable tracing, add the following configuration parameters to `application.properties`:
```
# Tracing configuration
# Probability of requests to sample (1.0 = 100%, 0.1 = 10%)
management.tracing.sampling.probability=1.0

# OTLP endpoint for trace export
# Default endpoint for OpenTelemetry Collector or compatible backends
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
```

#### Trace Propagation
`cdoc2-auth-server` supports W3C Trace Context propagation, allowing distributed traces to span across
multiple services and components.

To propagate traces, include the `traceparent` header in your HTTP requests:
```
traceparent: 00-<trace-id>-<span-id>-01
```
Format breakdown:
* `00` - Version (currently always "00")
* `<trace-id>` - 32-character hexadecimal trace identifier (16 bytes)
* `<span-id>` - 16-character hexadecimal span identifier (8 bytes)
* `01` - Trace flags (01 = sampled, 00 = not sampled)
