FROM couchbase/server:4.5.0

COPY scripts/setup-cb.sh /opt/couchbase

CMD ["/opt/couchbase/setup-cb.sh"]