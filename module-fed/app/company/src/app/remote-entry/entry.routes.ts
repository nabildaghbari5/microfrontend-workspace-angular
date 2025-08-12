import { Route } from '@angular/router';
import { RemoteEntry } from './entry';
import { Dashboard } from '../page/dashboard/dashboard';
import { EditProfil } from '../page/editProfilCompany/editProfil';
import { NewJob } from '../page/Job/newJob';

export const remoteRoutes: Route[] = [
    { path: '', component: RemoteEntry } ,
    {
        path:'dashboard',
        component:Dashboard
    },
    {
        path:'edit-profil',
        component:EditProfil
    },
    {
        path:'new-job',
        component:NewJob
    }
];
