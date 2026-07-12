FROM gradle:8.12-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/server/build/install/server/ ./
ENV PORT=8080 DATA_DIR=/data APP_ENV=production ALLOW_INSECURE_DEV_AUTH=false
EXPOSE 8080
CMD ["./bin/server"]
