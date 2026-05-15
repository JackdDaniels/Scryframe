/**
 * Frontend → backend integration.
 * Spring Boot runs on http://localhost:8080 by default.
 * If you change `server.port` in application.properties, update this too.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
};
