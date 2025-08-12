import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls:  ['./dashboard.scss'], 
})
export class Dashboard implements OnInit{
     
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
  
  constructor(
    private router:Router
  ){

  }

  ngOnInit(): void {
  }

  onChangeLogoClick(): void {
    console.log('Change logo clicked');
    // Logique pour changer le logo
  }

  onPostJobClick(): void {
    console.log('Post new job clicked');
    // Logique pour poster un nouveau job
  }

  onCompleteProfileClick(): void {
    this.router.navigate(['/company/edit-profil'])
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