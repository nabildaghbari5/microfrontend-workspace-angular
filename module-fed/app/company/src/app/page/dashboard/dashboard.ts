import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls:  ['./dashboard.scss'], 
})
export class Dashboard {
  
  companyProfile = {
    companyName: 'Not provided',
    contactEmail: 'Not provided',
    aboutCompany: 'Not provided',
    contactPhoneNumber: 'Not provided',
    industry: 'Not provided',
    contactLinkedInURL: 'Not provided',
    address: 'Not provided',
    contactWebsite: 'Not provided',
    country: 'Not provided',
    profileStatus: 'PENDING'
  };

  stats = {
    totalJobs: 0,
    totalApplications: 0
  };
  
  onChangeLogoClick(): void {
    console.log('Change logo clicked');
    // Logique pour changer le logo
  }

  onPostJobClick(): void {
    console.log('Post new job clicked');
    // Logique pour poster un nouveau job
  }

  onCompleteProfileClick(): void {
    console.log('Complete company profile clicked');
    // Logique pour compléter le profil de l'entreprise
  }

  onPostFirstJobClick(): void {
    console.log('Post your first job clicked');
    // Logique pour poster le premier job
  }

  onNewJobClick(): void {
    console.log('New job clicked');
    // Logique pour créer un nouveau job
  }
}