# Stage 1: build Angular app
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
RUN npm run build -- --configuration production

# Stage 2: serve with nginx
FROM nginx:1.27-alpine
COPY --from=build /app/dist/guesthost-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
