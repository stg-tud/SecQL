FROM openjdk:8u181-jdk

# Install SSH
RUN apt-get update && apt-get install -y openssh-server rsync
RUN mkdir /var/run/sshd

# Configure login for the identity of the server container
COPY test-server.key.pub /root/.ssh/authorized_keys

# Run SSH Deamon
EXPOSE 22
CMD /usr/sbin/sshd -D