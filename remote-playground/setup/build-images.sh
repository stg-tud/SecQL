#!/bin/bash

# Generate server key
yes y | ssh-keygen -t rsa -b 4096 -N "" -C "i3ql-test-server" -q -f test-server.key
cp test-server.key* test-server
cp test-server.key* test-client

# Build the images
docker build -t i3ql-test-server test-server
docker build -t i3ql-test-client test-client

# Cleanup keys from image directories
rm test-server/test-server.key*
rm test-client/test-server.key*