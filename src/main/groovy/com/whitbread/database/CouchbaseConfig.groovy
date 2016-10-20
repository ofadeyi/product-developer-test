package com.whitbread.database

import groovy.transform.CompileStatic

@CompileStatic
class CouchbaseConfig {
    List<String> seedNodes;
    String bucketName;
    String bucketPassword;
}
