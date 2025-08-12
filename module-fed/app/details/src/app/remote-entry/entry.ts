import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NxWelcome } from './nx-welcome';

@Component({
  imports: [CommonModule, NxWelcome],
  selector: 'app-details-entry',
  template: `<app-nx-welcome></app-nx-welcome>`,
})
export class RemoteEntry {}
