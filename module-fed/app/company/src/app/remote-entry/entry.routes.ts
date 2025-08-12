import { Route } from '@angular/router';
import { RemoteEntry } from './entry';
import { Dashboard } from '../page/dashboard/dashboard';

export const remoteRoutes: Route[] = [
    { path: '', component: RemoteEntry } ,
    {
        path:'dashboard',
        component:Dashboard
    }
];
