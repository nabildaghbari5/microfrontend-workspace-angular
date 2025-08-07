import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { Profile } from '../model/profile';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;
  private _profile: Profile | undefined;

  get profile(): Profile | undefined {
    return this._profile;  
  }


  get keycloak() {
    if (!this._keycloak) {
      this._keycloak = new Keycloak({
        url: 'http://localhost:8080',
        realm: 'talent-cloud',
        clientId: 'talentId'
      });
    }
    return this._keycloak;
  }


  constructor() { }

  async init() {
      const authenticated = await this.keycloak.init({
        onLoad: 'login-required',
      });
      if (authenticated) {
          this._profile = (await this.keycloak.loadUserProfile()) as Profile;
          this._profile.token = this.keycloak.token || '';
          console.log( this._profile)
      }
  }
  

  
}
