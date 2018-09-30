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

# Initialize project
RUN cd /var && git clone -b distributed-tests -v https://github.com/DSoko2/i3QL.git
RUN cd /var/i3QL && sbt remote-playground/compile

# Run SSH Deamon
EXPOSE 22
CMD /usr/sbin/sshd -D