# fbworkload
This project generates a social networking workload based on published stats by Facebook [1]. 

[1] Berk Atikoglu, Yuehai Xu, Eitan Frachtenberg, Song Jiang, and Mike Paleczny. 2012. Workload analysis of a large-scale key-value store. In Proceedings of the 12th ACM SIGMETRICS/PERFORMANCE joint international conference on Measurement and Modeling of Computer Systems (SIGMETRICS '12). ACM, New York, NY, USA, 53-64. DOI=http://dx.doi.org/10.1145/2254756.2254766

## Example command to generate a trace. 
trace_specification.properties specifies input parameters. The program writes generated traces to /tmp/fb-trace.
```
java -jar tracegen.jar trace_specification.properties /tmp/fb-trace
```
## Input
<table>
  <tr>
    <td><strong>Parameter</strong></td>
    <td><strong>Value</strong></td>
    <td><strong>Description</strong></td>
  </tr>
  <tr>
    <td>read</td>
    <td>A value between 0.0 to 1.0</td>
    <td>The percentage of read requests in the generated trace</td>
  </tr>
  <tr>
    <td>replace</td>
    <td>A value between 0.0 to 1.0</td>
    <td>The percentage of replace requests in the generated trace</td>
  </tr>
  <tr>
    <td>update</td>
    <td>A value between 0.0 to 1.0</td>
    <td>The percentage of update requests in the generated trace</td>
  </tr>
  <tr>
    <td>items</td>
    <td>A positive integer</td>
    <td>The total number of keys</td>
  </tr>
  <tr>
    <td>requests</td>
    <td>A positive integer</td>
    <td>The total number of requests</td>
  </tr>
</table>

## Output
The generated trace is in the following format. 
<table>
  <tr>
    <td>Operation</td>
        <td>Key</td>
        <td>Key size (bytes)</td>
        <td>Value size (bytes)</td>
        <td>Timestamp (micro seconds)</td>
  </tr>
</table>
This is an example output. It represents a read request on key 10499 at 12 micro seconds relative to the first request. The key size is 58 bytes and the value size is 9 bytes. 

```
READ,10499,58,9,12
```

