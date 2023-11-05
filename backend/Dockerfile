FROM node:latest
WORKDIR /backend
COPY ./package.json /backend
COPY ./package-lock.json /backend
COPY ./server.js /backend
COPY ./keep-alive.sh /backend
# ENTRYPOINT [/backend/keep-alive.sh]
RUN echo 'Starting...'
RUN npm install
EXPOSE 8080
CMD ["npm", "start"]