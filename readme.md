### Pre-reqs
* [CICS Transaction Gateway V9.1](http://www.ibm.com/software/products/en/cics-ctg/)
* [Apache Commons Codec](http://commons.apache.org/proper/commons-codec/)
* [json.org Java library](http://www.json.org/java/index.html)

### Introduction

This CICS Transaction Gateway Request Monitoring exit is designed to store the request monitoring information in a cloudant database as described in the blog post [Analysis of mobile workload using IBM Bluemix](https://developer.ibm.com/cics/2015/03/09/analysis-of-mobile-workload-using-ibm-bluemix).

### Usage

1. Add the `ctgcloudmonitor.jar` to the classpath of your Gateway daemon along with the commons codec and json jar files.
2. Add the following line to the Gateway daemon configuration file `requestexits=com.ibm.ctg.samples.CloudMonitor`
3. Start the CICS Transaction Gateway with the startup parameter `-j-Dcloudmonitor.dburl=<url of cloudant database>` and optionally `-j-Dcloudmonitor.logfile=<path to log file>`

