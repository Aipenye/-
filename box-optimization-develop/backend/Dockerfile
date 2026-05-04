FROM maven:3.9.9-eclipse-temurin-21

# Create working directory
RUN mkdir -p /home/backend
WORKDIR /home/backend

# Debug: List what's in the current build context
RUN ls -la

# Copy directories - making sure they exist in your build context
COPY ./timefold /home/backend/timefold/
COPY ./box-optimizer-api /home/backend/box-optimizer-api/
COPY ./skjolber /home/backend/skjolber/

# Debug: List what was copied
RUN ls -la /home/backend/
RUN find /home/backend -type d | sort

WORKDIR /home/backend/timefold
# RUN mvn clean install -DskipTests
RUN mvn clean install 

WORKDIR /home/backend/skjolber
# RUN mvn clean install -DskipTests
RUN mvn clean install 

WORKDIR /home/backend/box-optimizer-api
# RUN mvn clean install -DskipTests
RUN mvn clean install

WORKDIR /home/backend/box-optimizer-api

EXPOSE 8080 

CMD ["mvn", "spring-boot:run"]