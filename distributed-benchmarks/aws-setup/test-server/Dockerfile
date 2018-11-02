FROM dsoko2/i3ql-lms:0.3.0-8u181-jdk

# Install SSH and RSYNC
RUN apt-get update && apt-get install -y openssh-server rsync git-core
RUN mkdir /var/run/sshd
# Disable known host checking: Makes vulnurable towards man-in-the-middle attacks!
RUN sed -i 's/#   StrictHostKeyChecking ask/   StrictHostKeyChecking no/' /etc/ssh/ssh_config

# Configure ssh identity
COPY test-server.key /root/.ssh/id_rsa
# Configure login for the same identity
COPY test-server.key.pub /root/.ssh/authorized_keys

# Install mongodb
RUN cd /opt && curl https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-debian92-4.0.3.tgz | tar -xzv
RUN mv /opt/mongodb-* /opt/mongodb
RUN mkdir -p /var/lib/mongodb
RUN echo "export PATH=/opt/mongodb/bin:$PATH" >> ~/.bashrc

# Initialize project
RUN cd /var && git clone -b distributed-tests -v https://github.com/DSoko2/i3QL.git
RUN cd /var/i3QL && sbt company-benchmark/multi-jvm:compile
RUN cd /var/i3QL && sbt hospital-benchmark/multi-jvm:compile
RUN cd /var/i3QL && sbt tpch-benchmark/multi-jvm:compile

# Add additional hosts file, which need to be added to /etc/hosts in CMD, since docker
# reinitalizes /etc/hosts on every boot
COPY hosts /opt/hosts

# Run mongodb and SSH Deamon
EXPOSE 27017
EXPOSE 22
CMD cat /opt/hosts >> /etc/hosts && \
	/opt/mongodb/bin/mongod --fork --bind_ip_all --dbpath /var/lib/mongodb --logpath /var/log/mongodb && \
	/usr/sbin/sshd -D