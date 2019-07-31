#!/bin/bash
set -e

CLUSTER_NAME=i3ql-hospital-benchmark
NAMESPACE_NAME=hospital.i3ql

../lib/aws-delete-service.sh $CLUSTER_NAME server $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME patient $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME person $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME knowledge $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME client $NAMESPACE_NAME

../lib/aws-ecs-deregister-task-definitions.sh i3ql-hospital-server
../lib/aws-ecs-deregister-task-definitions.sh i3ql-hospital-patient
../lib/aws-ecs-deregister-task-definitions.sh i3ql-hospital-person
../lib/aws-ecs-deregister-task-definitions.sh i3ql-hospital-knowledge
../lib/aws-ecs-deregister-task-definitions.sh i3ql-hospital-client

../lib/aws-ecs-delete-cluster.sh $CLUSTER_NAME
../lib/aws-ec2-delete-subnet.sh $CLUSTER_NAME
../lib/aws-sd-delete-namespace.sh $NAMESPACE_NAME