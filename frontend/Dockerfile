FROM node:latest
WORKDIR /frontend
COPY ./package.json /frontend
COPY ./package-lock.json /frontend
COPY ./public /frontend/public
COPY ./src /frontend/src
RUN npm install
EXPOSE 3000
CMD ["npm", "start"]