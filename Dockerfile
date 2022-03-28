# Dockerfile that generates the nginx image to host both the frontend and backend.

# Stage 1: Build frontend's static files.
FROM node:16 AS front

WORKDIR /frontend

COPY ./frontend/yarn.lock /frontend
COPY ./frontend/package.json /frontend
RUN yarn install

COPY ./frontend /frontend
RUN yarn run next build
RUN yarn run next export


# Stage 2: Build the nginx image.
FROM nginx:1
COPY ./nginx.conf.template /etc/nginx/templates/nginx.conf.template
COPY --from=front /frontend/out /frontend
