# Custom Jenkins image with Docker CLI
# Allows Jenkins pipeline to build/run Docker containers via the host socket.
# See docs/ai/deployment/README.md for setup.

FROM jenkins/jenkins:lts

USER root

# Install Docker CLI so Jenkins can manage app containers
RUN apt-get update && \
    apt-get install -y --no-install-recommends docker.io curl && \
    rm -rf /var/lib/apt/lists/*

# Default back to jenkins user (docker-compose overrides to root for socket access)
USER jenkins
