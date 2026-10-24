FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY lib/ lib/
COPY Main.java .
COPY framework/ framework/
COPY growth_tracker/ growth_tracker/

RUN find . -name "*.java" > sources.txt \
 && javac -encoding UTF-8 -cp "lib/*" -d out @sources.txt

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /src/out/ classes/
COPY lib/ lib/
COPY frontend/ frontend/

EXPOSE 8080

CMD ["java", "-cp", "classes:lib/*", "Main"]