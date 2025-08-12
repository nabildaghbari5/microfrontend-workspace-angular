import { KeycloakService } from './../../../../shared/src/lib/auth/keycloak';
import {
  APP_INITIALIZER,
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { appRoutes } from './app.routes';

// ➡️ Cette fonction d'initialisation démarre Keycloak avant le chargement de l'app
function initializeKeycloak(keycloakService: KeycloakService) {   
  return () => keycloakService.init();
}
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(appRoutes),
    /*
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,  
      deps: [KeycloakService],
      multi: true
    }*/ 
  ],
};
