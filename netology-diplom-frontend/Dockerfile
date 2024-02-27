FROM node:10
WORKDIR /usr/src/app
COPY package.json ./
RUN npm install
COPY . .
EXPOSE 8081
CMD ["npm", "run", "serve"]