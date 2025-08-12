import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-edit-profil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editProfil.html',
  styleUrl: './editProfil.scss',
})
export class EditProfil implements OnInit {
  companyForm!: FormGroup;

  industries = [
    'Technology',
    'Healthcare',
    'Finance',
    'Education',
    'Manufacturing',
    'Retail',
    'Real Estate',
    'Consulting',
    'Media & Entertainment',
    'Non-Profit'
  ];

  countries = [
    'United States',
    'Canada',
    'United Kingdom',
    'France',
    'Germany',
    'Spain',
    'Italy',
    'Netherlands',
    'Australia',
    'Japan'
  ];

  constructor( 
    private fb: FormBuilder ,
    private router:Router
  ) {}

  ngOnInit() {
    this.initializeForm();
  }

  initializeForm() {
    this.companyForm = this.fb.group({
      companyName: ['', [Validators.required, Validators.minLength(2)]],
      industry: ['', Validators.required],
      website: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      address: ['', Validators.required],
      country: ['', Validators.required],
      companyDescription: ['', Validators.required],
      linkedinUrl: ['', Validators.pattern(/^https?:\/\/.+/)]
    });
  }

  onSubmit() {
    if (this.companyForm.valid) {
      console.log('Form submitted:', this.companyForm.value);
      // Handle form submission here
    } else {
      console.log('Form is invalid');
      this.markFormGroupTouched();
    }
  }

  onCancel() {
    this.companyForm.reset();
    this.initializeForm();
     this.router.navigate(['/company/dashboard']);
  }

  onGoBack() {
   this.router.navigate(['/company/dashboard']);
  }

 

  private markFormGroupTouched() {
    Object.keys(this.companyForm.controls).forEach(key => {
      const control = this.companyForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.companyForm.get(fieldName);
    if (field?.hasError('required') && field?.touched) {
      return 'This field is required';
    }
    if (field?.hasError('email') && field?.touched) {
      return 'Please enter a valid email address';
    }
    if (field?.hasError('pattern') && field?.touched) {
      return 'Please enter a valid URL';
    }
    if (field?.hasError('minlength') && field?.touched) {
      return 'Minimum length not met';
    }
    return '';
  }
}
