# Deployment

## Target Platform

Project Pulse is deployed to Azure with the following topology:

- Frontend: Azure Static Web Apps
- Backend: Azure App Service (Linux, Java 21)
- Database: Azure Database for MySQL Flexible Server
- Secrets: App Service application settings or Azure Key Vault references
- Monitoring: Application Insights

This keeps the Vue frontend and Spring Boot backend deployed separately while allowing the backend to connect to a managed MySQL database.

## Required Backend App Settings

Set these in Azure App Service under Configuration > Application settings:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SECURITY_JWT_SECRET_KEY`
- `SECURITY_JWT_TOKEN_EXPIRY_IN_HOURS=24`
- `FRONTEND_URL`
- `APP_CORS_ALLOWED_ORIGINS`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

Example values:

- `SPRING_DATASOURCE_URL=jdbc:mysql://<mysql-host>:3306/project-pulse?useSSL=true&requireSSL=true`
- `FRONTEND_URL=https://<frontend-app>.azurestaticapps.net`
- `APP_CORS_ALLOWED_ORIGINS=https://<frontend-app>.azurestaticapps.net`

For the first Azure deployment, email can remain effectively disabled by supplying placeholder SMTP values that are not used in normal flows.

## Required Frontend Build Setting

Set this in Azure Static Web Apps build configuration:

- `VITE_API_BASE_URL=https://<backend-app>.azurewebsites.net/api/v1`

The frontend keeps using `VITE_SERVER_URL` in local development and switches to `VITE_API_BASE_URL` for hosted builds.

## Azure Setup Order

### 1. Resource group

Create a single resource group, for example:

- `rg-project-pulse-prod`

Keep the frontend, backend, and database in the same Azure region.

### 2. MySQL Flexible Server

Create an Azure Database for MySQL Flexible Server with:

- MySQL 8.x
- Public access (allowed IP addresses)
- Database name: `project-pulse`

Allow your current development IP temporarily so you can verify connectivity. Do not import your local Flyway history table. Let Flyway migrate a clean Azure database.

### 3. App Service

Create an Azure App Service Web App with:

- Linux
- Java 21
- Application Insights enabled

After creation, add the backend app settings listed above.

### 4. MySQL firewall rules

Once App Service exists, collect its outbound IP addresses and add them to the MySQL firewall. Remove temporary wide-open or troubleshooting rules after deployment is stable.

### 5. Static Web App

Create an Azure Static Web App connected to this repository.

Monorepo build settings:

- App location: `src/frontend`
- Output location: `dist`

The frontend includes a `staticwebapp.config.json` file so Vue Router history-mode routes rewrite to `index.html`.

## Deployment Workflow

### Backend

Manual backend deployment flow:

1. Build the Spring Boot JAR from `src/backend`
2. Deploy the JAR to Azure App Service
3. Watch startup logs
4. Confirm:
   - Spring profile is `prod`
   - datasource connects successfully
   - Flyway runs successfully
   - `GET /actuator/health` returns healthy

The repository also includes a GitHub Actions workflow for backend deployment that can be triggered manually after Azure resources and repository secrets are ready.

Required GitHub repository secrets for the backend deployment workflow:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`
- `AZURE_WEBAPP_NAME`

### Frontend

Azure Static Web Apps can generate its own deployment workflow when linked through the Azure portal. If you use that flow, make sure the workflow uses:

- `app_location: src/frontend`
- `output_location: dist`

## Validation Checklist

### Backend

- `GET /actuator/health` returns healthy
- `POST /api/v1/auth/login` succeeds
- protected endpoints accept valid JWTs
- Flyway schema is created successfully in Azure MySQL

### Frontend

- the app loads from Azure Static Web Apps
- login succeeds
- requests go to the Azure backend URL, not localhost
- route refresh works on:
  - `/peer-evaluations`
  - `/peer-evaluation-report`

### Integration

- WAR create/update works
- peer evaluation create/update works
- student peer evaluation report loads correctly
- browser console shows no CORS failures

## Hardening After First Deploy

After the first stable release:

1. Remove your local IP from the MySQL firewall if no longer needed
2. Keep only required Azure IP rules
3. Move secrets to Key Vault references
4. Add alerting for backend health and 5xx responses
5. Add real SMTP credentials if invite and password reset email flows need to work in production
