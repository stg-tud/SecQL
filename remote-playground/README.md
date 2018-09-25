# Distributed Benchmarks

In these projects benchmarks for the distributed execution of i3QL are implemented. They base on the AKKA multi node testkit.

## Setup and Execution

The benchmarks can be run without any adjustment either on a single host in multiple JVMs or distributed in the cloud. The first execution option is especially during development and debugging more convenient, while the second enables more complex and bigger resource distributions.

### Single Host/Multi-JVM

The sbt-multi-jvm plugin is used to orchestrate automatically the JVMs. No further setup is required.

Execute all tests:

```bash
sbt remote-workspace/multi-jvm:test
```

Execute a specific test:

```bash
sbt remote-workspace/multi-jvm:"testOnly package.Class"
```


### Multi Host/Cloud

The sbt-multi-jvm plugin is used to orchestrate automatically the JVMs. However, previously the cloud setup is necessary, which is mostly automatized using bash scripts and the AWS CLI.

#### AWS Execution Environment

For the cloud testing, each node is executed in a separate docker container (called client containers) and the tests are started and monitored via another container, the server. The server container holds the i3QL repo. The user logs into it via SSH in order to build and execute the tests. Using the akka multi node test kit, the server container builds the tests and deploys and executes them automatically on the client containers. These are simple Java, SSH and Rsync enabled containers which get managed by the SbtMultiJvm Plugin.

Used AWS Services:

* EC2: Network Management
* ECR: Container Image Repository
* ECS: Cluster administration
* Fargate: Container execution for ECS
* Service Discovery & Route53: Automatic local DNS management

#### Requirements

Local administration machine:

* Should run a bash with installed AWS CLI in an up-to-date version
* AWS CLI should be logged into a AWS root account or a restricted account, who has administrative privileges for all the required operations
* A current version of Docker and the Docker CLI must be installed in order to manage the Docker images

AWS Cloud:

* A VPC should be created in the AWS VPC service with IPv4 CIDR 10.0.0.0/16 and name "i3ql"
	* The test setups will setup their clusters in subnets of this VPC
* An internet gateway has to be added to the i3ql VPC
* The default route table of the i3ql VPC should contain these entries:
	1. Destination: 10.0.0.0/16, Target: local
	2. Destination: 0.0.0.0/0, Target: igw-\[THE INTERNET GATEWAY OF THE VPC]
* The default security group of the i3ql VPC should have these inbound rules:
	1. Type: ALL TRAFIC, Protocol: ALL, Port Range: ALL, Source: sg-\[THE SECURITY GROUP ITSELF]
	2. Type: SSH (22), Protocol: TCP (6), Port Range: 22, Source: 0.0.0.0/0
* The default security group of the i3ql VPC should have these outbound rules:
	1. Type: ALL TRAFIC, Protocol: ALL, Port Range: ALL, Destination: 0.0.0.0/0
* Apart from this basic VPC configuration all setup and destruction is automatized

#### Setup and Destruction

##### Container Images

Initially the server and client docker images have to be built and deployed to AWS ECR. To do so, change to the setup directory and execute the build-images and push-images scripts:

```bash
cd remote-playground/setup
./build-images.sh
./push-images.sh
```

In order to remove the repositories and images again from ECR, run the `./remove-images.sh` script in the same directory.

##### Clusters

Afterwards the benchmark clusters can be setup using these images. For each benchmark a setup script is provided. To use it change to the benchmarks setup directory and simply run it:

```bash
cd remote-playground/setup/[BENCHMARK NAME]
./setup.sh
```

These scripts create the cluster, register all tasks, start all required services in the cluster from the tasks and configure the network and service discovery. The services spin up and control the tasks/containers automatically, which can take up to 5 minutes until the entire environment is deployed and active.

In order to cleanup the environment completely, run from the same benchmark directory the `destroy.sh` script. It stops all running containers and removes all the created resources again.

#### Execution

Log into the AWS web console and get the public IP of the running server task in the benchmark cluster. Use the test-server.key identity generated while building the images to log into that container as root user:

```bash
ssh-add [PATH TO]/test-server.key
ssh root@[PUBLIC IP OF SERVER]
```

Inside the container change to the repository project directory /var/i3QL and run from there:

Execute all tests:

```bash
sbt remote-workspace/multi-jvm:multiNodeTest
```

Execute a specific test:

```bash
sbt remote-workspace/multi-jvm:"multiNodeTestOnly package.Class"
```