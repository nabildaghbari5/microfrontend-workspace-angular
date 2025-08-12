import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-new-job',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './newJob.html',
  styleUrl: './newJob.scss',
})
export class NewJob implements OnInit {
  jobForm!: FormGroup;

  employmentTypes = [
    'Full Time',
    'Part Time',
    'Contract',
    'Freelance',
    'Internship',
    'Remote'
  ];

  experienceLevels = [
    'Entry Level',
    '1-2 years',
    '3-5 years',
    '5+ years',
    '10+ years'
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.initializeForm();
  }

  initializeForm() {
    this.jobForm = this.fb.group({
      jobTitle: ['', [Validators.required, Validators.minLength(3)]],
      jobDescription: ['', [Validators.required, Validators.minLength(50)]],
      location: ['', Validators.required],
      salaryRange: [''],
      requiredExperience: ['', Validators.required],
      employmentType: ['Full Time', Validators.required],
      skills: this.fb.array([])
    });
  } 

  get skills(): FormArray {
    return this.jobForm.get('skills') as FormArray;
  }

  addSkill() {
    const skillControl = this.fb.control('', Validators.required);
    this.skills.push(skillControl);
  }

  removeSkill(index: number) {
    this.skills.removeAt(index);
  }

  onSubmit() {
    if (this.jobForm.valid) {
      console.log('Job posted:', this.jobForm.value);
      // Handle form submission here
    } else {
      console.log('Form is invalid');
      this.markFormGroupTouched();
    }
  }

  onCancel() {
    this.jobForm.reset();
    this.initializeForm();
  }

  private markFormGroupTouched() {
    Object.keys(this.jobForm.controls).forEach(key => {
      const control = this.jobForm.get(key);
      control?.markAsTouched();
    });
    
    // Mark skills array controls as touched
    this.skills.controls.forEach(control => {
      control.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.jobForm.get(fieldName);
    if (field?.hasError('required') && field?.touched) {
      return 'This field is required';
    }
    if (field?.hasError('minlength') && field?.touched) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} characters required`;
    }
    return '';
  }

  getSkillError(index: number): string {
    const skillControl = this.skills.at(index);
    if (skillControl?.hasError('required') && skillControl?.touched) {
      return 'Skill name is required';
    }
    return '';
  }
}
