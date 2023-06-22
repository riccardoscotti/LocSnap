# Pulling base image
FROM alpine:latest

# Specifing env type
ENV NODE_ENV=production

# Setting working directory
RUN mkdir /app
WORKDIR /app

# Copy project inside the container
COPY app/ /app/

# Installing nodejs and npm
RUN apk add --update npm

# Initialize project
RUN npm install