# Development Dockerfile for React application
FROM node:23.11-alpine

# Create app directory and set ownership
RUN mkdir -p /home/react-app && chown -R node:node /home/react-app
WORKDIR /home/react-app

# Copy package files for dependency installation
COPY --chown=node:node package*.json ./

# Switch to non-root user for security
USER node

# Install dependencies
RUN npm install

# Copy application files
COPY --chown=node:node src ./src
COPY --chown=node:node mock ./mock
COPY --chown=node:node index.html ./
COPY --chown=node:node *.js ./

# Expose port for the application - crucial for host access
EXPOSE 5173

# Start the development server with the host flag to allow external connections
CMD [ "npm", "run", "dev", "--", "--host", "0.0.0.0" ]