import { NxWelcome } from './nx-welcome';
import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: 'details',
    loadChildren: () => import('details/Routes').then((m) => m!.remoteRoutes),
  },
  {
    path: 'nav',
    loadChildren: () => import('nav/Routes').then((m) => m!.remoteRoutes),
  },
  {
    path: '',
    component: NxWelcome,
  },
];
