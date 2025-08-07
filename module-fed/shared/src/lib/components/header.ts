import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'lib-header',
  standalone: true, // ✅ nécessaire pour un composant sans module
  imports: [CommonModule],
  templateUrl: './header.html',
  styleUrls: ['./header.scss'], // ✅ attention au "s"
})
export class Header {}
